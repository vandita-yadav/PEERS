package com.example.peers.features.events

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.features.offline.OfflineCacheRepository
import com.example.peers.features.offline.SavedEventCacheEntity
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EventsUiState(
    val events: List<CommunityEvent> = emptyList(),
    val myAttendance: Set<String> = emptySet(),
    val savedEventIds: Set<String> = emptySet(),
    val selectedDate: LocalDate? = null,
    val selectedOrganizer: EventOrganizerType? = null,
    val query: String = "",
    val viewingEvent: CommunityEvent? = null,
    val activeAttendees: List<EventAttendee> = emptyList(),
    val isCreatingEvent: Boolean = false,
    val isLoading: Boolean = false,
)

class EventsViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: EventsRepository = EventsRepository(),
) : AndroidViewModel(application) {

    private val cache = OfflineCacheRepository.get(application)
    private val prefs = application.getSharedPreferences("peers_events_local", android.content.Context.MODE_PRIVATE)

    private fun loadLocalCreatedEvents(): List<CommunityEvent> {
        val jsonStr = prefs.getString("created_events", null) ?: return emptyList()
        return runCatching {
            val array = org.json.JSONArray(jsonStr)
            (0 until array.length()).map { idx ->
                val obj = array.getJSONObject(idx)
                CommunityEvent(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.getString("description"),
                    organizerName = obj.getString("organizerName"),
                    organizerType = EventOrganizerType.valueOf(obj.getString("organizerType")),
                    venue = obj.getString("venue"),
                    city = obj.getString("city"),
                    startMillis = obj.getLong("startMillis"),
                    emoji = obj.getString("emoji"),
                    capacity = obj.getInt("capacity"),
                    attendeeCount = obj.getInt("attendeeCount"),
                    createdBy = obj.optString("createdBy", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveLocalCreatedEvents(events: List<CommunityEvent>) {
        runCatching {
            val array = org.json.JSONArray()
            events.forEach { e ->
                val obj = org.json.JSONObject().apply {
                    put("id", e.id)
                    put("title", e.title)
                    put("description", e.description)
                    put("organizerName", e.organizerName)
                    put("organizerType", e.organizerType.name)
                    put("venue", e.venue)
                    put("city", e.city)
                    put("startMillis", e.startMillis)
                    put("emoji", e.emoji)
                    put("capacity", e.capacity)
                    put("attendeeCount", e.attendeeCount)
                    put("createdBy", e.createdBy)
                    put("createdAt", e.createdAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("created_events", array.toString()).apply()
        }
    }

    private val _rawEvents = MutableStateFlow<List<CommunityEvent>>(emptyList())
    private val _myAttendance = MutableStateFlow<Set<String>>(emptySet())
    private val _savedEventIds = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _selectedOrganizer = MutableStateFlow<EventOrganizerType?>(null)
    private val _query = MutableStateFlow("")
    private val _viewingEvent = MutableStateFlow<CommunityEvent?>(null)
    private val _activeAttendees = MutableStateFlow<List<EventAttendee>>(emptyList())
    private val _isCreatingEvent = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)

    private val _uiEvents = MutableSharedFlow<EventUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<EventUiEvent> = _uiEvents.asSharedFlow()

    private var eventsListener: ListenerRegistration? = null
    private var attendanceListener: ListenerRegistration? = null
    private var attendeesListener: ListenerRegistration? = null
    private var remoteSavedEventsListener: ListenerRegistration? = null

    init {
        val localCreated = loadLocalCreatedEvents()
        _rawEvents.value = (localCreated + demoEvents()).distinctBy { it.id }
        _myAttendance.value = prefs.getStringSet("attendance_ids", emptySet()) ?: emptySet()

        eventsListener = repository.observeEvents { remoteEvents ->
            val base = if (remoteEvents.isNotEmpty()) remoteEvents else demoEvents()
            _rawEvents.value = (loadLocalCreatedEvents() + base).distinctBy { it.id }
            _isLoading.value = false
        }

        attendanceListener = repository.observeMyAttendance { attendance ->
            if (attendance.isNotEmpty()) {
                _myAttendance.value = _myAttendance.value + attendance
                prefs.edit().putStringSet("attendance_ids", _myAttendance.value).apply()
            }
        }

        viewModelScope.launch {
            cache.observeSavedEventIds().collect { _savedEventIds.value = it }
        }
        remoteSavedEventsListener = cache.observeRemoteSavedEvents { remote ->
            viewModelScope.launch { cache.hydrateSavedEvents(remote) }
        }
    }

    private val filterState = combine(
        _selectedDate,
        _selectedOrganizer,
        _query,
    ) { date, organizer, query ->
        Triple(date, organizer, query)
    }

    private val viewState = combine(
        _viewingEvent,
        _activeAttendees,
        _isCreatingEvent,
        _isLoading,
    ) { viewing, attendees, isCreating, loading ->
        Quad(viewing, attendees, isCreating, loading)
    }

    val uiState: StateFlow<EventsUiState> = combine(
        _rawEvents,
        _myAttendance,
        _savedEventIds,
        filterState,
        viewState,
    ) { raw, attendance, savedIds, filter, view ->
        val (date, organizer, query) = filter
        val (viewing, attendees, isCreating, loading) = view

        val filtered = raw.filter { event ->
            val matchesDate = date == null || event.date == date
            val matchesOrganizer = organizer == null || event.organizerType == organizer
            val matchesQuery = query.isBlank() ||
                event.title.contains(query, ignoreCase = true) ||
                event.description.contains(query, ignoreCase = true) ||
                event.venue.contains(query, ignoreCase = true) ||
                event.city.contains(query, ignoreCase = true)
            matchesDate && matchesOrganizer && matchesQuery
        }

        EventsUiState(
            events = filtered,
            myAttendance = attendance,
            savedEventIds = savedIds,
            selectedDate = date,
            selectedOrganizer = organizer,
            query = query,
            viewingEvent = viewing,
            activeAttendees = attendees,
            isCreatingEvent = isCreating,
            isLoading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventsUiState(events = demoEvents(), isLoading = true)
    )

    fun onDateSelected(date: LocalDate?) {
        _selectedDate.value = date
    }

    fun onOrganizerSelected(type: EventOrganizerType?) {
        _selectedOrganizer.value = type
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun openEventDetails(event: CommunityEvent) {
        _viewingEvent.value = event
        attendeesListener?.remove()
        attendeesListener = repository.observeAttendees(event.id) { list ->
            _activeAttendees.value = list
        }
    }

    fun closeEventDetails() {
        _viewingEvent.value = null
        _activeAttendees.value = emptyList()
        attendeesListener?.remove()
        attendeesListener = null
    }

    fun setCreatingEvent(show: Boolean) {
        _isCreatingEvent.value = show
    }

    fun toggleRsvp(event: CommunityEvent) {
        val willAttend = event.id !in _myAttendance.value
        // 1. Optimistically update local state immediately
        _myAttendance.value = if (willAttend) {
            _myAttendance.value + event.id
        } else {
            _myAttendance.value - event.id
        }
        val delta = if (willAttend) 1 else -1
        _rawEvents.value = _rawEvents.value.map {
            if (it.id == event.id) it.copy(attendeeCount = (it.attendeeCount + delta).coerceAtLeast(0)) else it
        }
        if (_viewingEvent.value?.id == event.id) {
            _viewingEvent.value = _viewingEvent.value?.copy(
                attendeeCount = (_viewingEvent.value!!.attendeeCount + delta).coerceAtLeast(0)
            )
        }
        if (willAttend) {
            _uiEvents.tryEmit(EventUiEvent.Message("You're attending ${event.title}! 🎉"))
        } else {
            _uiEvents.tryEmit(EventUiEvent.Message("RSVP cancelled for ${event.title}."))
        }
        prefs.edit().putStringSet("attendance_ids", _myAttendance.value).apply()

        // Update local created events if attendee count changed
        val currentCreated = loadLocalCreatedEvents()
        if (currentCreated.any { it.id == event.id }) {
            val updated = currentCreated.map {
                if (it.id == event.id) it.copy(attendeeCount = (it.attendeeCount + delta).coerceAtLeast(0)) else it
            }
            saveLocalCreatedEvents(updated)
        }

        // 2. Sync to Firestore in the background
        viewModelScope.launch {
            runCatching {
                repository.setAttending(event, willAttend)
            }.onFailure { err ->
                android.util.Log.w("PeersEvents", "Firestore RSVP sync failed, saved locally", err)
            }
        }
    }

    fun toggleSave(event: CommunityEvent) {
        val willSave = event.id !in _savedEventIds.value
        // Optimistically update saved IDs
        _savedEventIds.value = if (willSave) {
            _savedEventIds.value + event.id
        } else {
            _savedEventIds.value - event.id
        }
        val msg = if (willSave) {
            "Saved ${event.title} to bookmarks."
        } else {
            "Removed ${event.title} from bookmarks."
        }
        _uiEvents.tryEmit(EventUiEvent.Message(msg))

        viewModelScope.launch {
            runCatching {
                val entity = SavedEventCacheEntity(
                    eventId = event.id,
                    title = event.title,
                    organizer = event.organizerName,
                    epochDay = event.date.toEpochDay(),
                    time = "${event.time.hour}:${event.time.minute.toString().padStart(2, '0')}",
                    location = "${event.venue}, ${event.city}",
                    attendees = event.attendeeCount,
                    emoji = event.emoji,
                    savedAt = System.currentTimeMillis(),
                )
                cache.setSavedEvent(entity, willSave)
            }.onFailure {
                android.util.Log.w("PeersEvents", "Cache save error", it)
            }
        }
    }

    fun createEvent(
        title: String,
        description: String,
        organizerName: String,
        organizerType: EventOrganizerType,
        venue: String,
        city: String,
        startMillis: Long,
        emoji: String,
        capacity: Int,
    ) {
        val newEvent = CommunityEvent(
            id = "event-${System.currentTimeMillis()}",
            title = title.trim(),
            description = description.trim(),
            organizerName = organizerName.trim().ifBlank { "PEERS Community" },
            organizerType = organizerType,
            venue = venue.trim(),
            city = city.trim(),
            startMillis = startMillis,
            emoji = emoji.ifBlank { "🎪" },
            capacity = capacity.coerceAtLeast(10),
            attendeeCount = 1,
            createdBy = "",
            createdAt = System.currentTimeMillis(),
        )

        // Optimistically add to events list immediately
        val updatedCreated = listOf(newEvent) + loadLocalCreatedEvents()
        saveLocalCreatedEvents(updatedCreated)
        _rawEvents.value = listOf(newEvent) + _rawEvents.value
        _myAttendance.value = _myAttendance.value + newEvent.id
        prefs.edit().putStringSet("attendance_ids", _myAttendance.value).apply()
        _isCreatingEvent.value = false
        _uiEvents.tryEmit(EventUiEvent.Message("Event '${newEvent.title}' created and RSVP'd! 🚀"))

        viewModelScope.launch {
            runCatching {
                repository.createEvent(newEvent)
            }.onFailure { err ->
                android.util.Log.w("PeersEvents", "Event created locally; Firestore sync pending", err)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        eventsListener?.remove()
        attendanceListener?.remove()
        attendeesListener?.remove()
        remoteSavedEventsListener?.remove()
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
