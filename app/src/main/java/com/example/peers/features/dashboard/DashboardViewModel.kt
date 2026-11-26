package com.example.peers.features.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.features.events.CommunityEvent
import com.example.peers.features.events.EventOrganizerType
import com.example.peers.features.events.EventsRepository
import com.example.peers.features.events.demoEvents
import com.example.peers.features.notifications.PeersNotificationHelper
import com.example.peers.features.offline.OfflineCacheRepository
import com.example.peers.features.offline.SavedEventCacheEntity
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersSky
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray

/**
 * Loads the signed-in user's profile from Firestore, exposes unified campus events
 * matching the Events tab, and keeps in-app notifications synced offline-first.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val eventsRepo = EventsRepository(auth, firestore)
    private val cache = OfflineCacheRepository.get(application)
    private val eventsPrefs = application.getSharedPreferences("peers_events_local", Context.MODE_PRIVATE)

    private val _user = MutableStateFlow<DashboardUser?>(null)
    val user: StateFlow<DashboardUser?> = _user.asStateFlow()

    private val _events = MutableStateFlow<List<DashboardEvent>>(emptyList())
    val events: StateFlow<List<DashboardEvent>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** One-off UI messages (snackbars/toasts). */
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    /** Cached in-app notifications; Room paints first, Firestore hydrates. */
    private val _notifications = MutableStateFlow<List<PeerNotification>>(emptyList())
    val notifications: StateFlow<List<PeerNotification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    /** Event ids saved for later, available offline. */
    private val _savedEventIds = MutableStateFlow<Set<String>>(emptySet())
    val savedEventIds: StateFlow<Set<String>> = _savedEventIds.asStateFlow()

    private var notificationListener: ListenerRegistration? = null
    private var savedEventsListener: ListenerRegistration? = null
    private var userProfileListener: ListenerRegistration? = null
    private var eventsListener: ListenerRegistration? = null
    private var seededNotifIds: MutableSet<String> = mutableSetOf()

    init {
        _events.value = mergeDashboardEvents(emptyList())
        loadProfile()
        observeCachedState()
        startLiveListeners()
    }

    private fun loadLocalCreatedEvents(): List<CommunityEvent> {
        val jsonStr = eventsPrefs.getString("created_events", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(jsonStr)
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
                    emoji = obj.optString("emoji", ""),
                    capacity = obj.getInt("capacity"),
                    attendeeCount = obj.getInt("attendeeCount"),
                    createdBy = obj.optString("createdBy", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun mergeDashboardEvents(remote: List<CommunityEvent>): List<DashboardEvent> {
        val combined = (loadLocalCreatedEvents() + remote + demoEvents())
            .distinctBy { it.id }
            .sortedBy { it.startMillis }
        return combined.mapIndexed { idx, ev ->
            val accent = when (idx % 3) {
                0 -> PeersLavender
                1 -> PeersSky
                else -> PeersMint
            }
            DashboardEvent(
                id = ev.id,
                title = ev.title,
                organizer = ev.organizerName,
                date = ev.date,
                time = ev.time,
                location = "${ev.venue}, ${ev.city}",
                attendees = ev.attendeeCount,
                emoji = "",
                accent = accent,
            )
        }
    }

    fun refreshEvents() {
        _events.value = mergeDashboardEvents(emptyList())
    }

    private fun observeCachedState() {
        viewModelScope.launch {
            runCatching {
                cache.observeNotifications().collect { rows ->
                    _notifications.value = rows.map { row ->
                        PeerNotification(
                            id = row.id,
                            title = row.title,
                            body = row.body,
                            emoji = row.emoji,
                            timestampMillis = row.timestampMillis,
                            isUnread = row.isUnread,
                            type = when (row.type) {
                                "roommate_request" -> NotificationType.ROOMMATE_REQUEST
                                "roommate_accepted" -> NotificationType.ROOMMATE_ACCEPTED
                                "request_sent" -> NotificationType.REQUEST_SENT
                                "accommodation_enquiry" -> NotificationType.ACCOMMODATION_ENQUIRY
                                else -> NotificationType.INFO
                            },
                            actionData = row.actionData,
                            resolvedState = row.resolvedState,
                        )
                    }
                    _unreadCount.value = rows.count { it.isUnread }
                }
            }
        }
        viewModelScope.launch {
            runCatching {
                cache.observeSavedEventIds().collect { _savedEventIds.value = it }
            }
        }
    }

    private fun snapshotToUser(uid: String, snap: DocumentSnapshot?, authUser: FirebaseUser?): DashboardUser {
        // Respect explicit Firestore avatar/photo edits: if user picked an avatar (photoUrl = ""),
        // do NOT overwrite it with Google authUser?.photoUrl.
        val hasStoredPhotoField = snap?.contains("photoUrl") == true
        val storedPhoto = snap?.getString("photoUrl")?.takeIf { it.isNotBlank() }
        val storedAvatar = snap?.get("avatarIndex")
        val resolvedPhoto = when {
            storedPhoto != null -> storedPhoto
            hasStoredPhotoField || storedAvatar != null -> null
            else -> authUser?.photoUrl?.toString()
        }

        return DashboardUser(
            uid = uid,
            name = snap?.getString("name").orEmpty().ifEmpty { authUser?.displayName ?: "Peer" },
            email = snap?.getString("email").orEmpty().ifEmpty { authUser?.email.orEmpty() },
            university = snap?.getString("university").orEmpty(),
            course = snap?.getString("course").orEmpty(),
            year = snap?.getString("yearOfStudy").orEmpty(),
            city = snap?.getString("currentCity").orEmpty(),
            currentState = snap?.getString("currentState").orEmpty(),
            photoUrl = resolvedPhoto,
            interests = (snap?.get("interests") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            needs = (snap?.get("needs") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            countryOfOrigin = snap?.getString("countryOfOrigin").orEmpty(),
            currentCountry = snap?.getString("currentCountry").orEmpty(),
            degreeLevel = snap?.getString("degreeLevel").orEmpty(),
            commencingMonth = snap?.getString("commencingMonth").orEmpty(),
            commencingYear = snap?.getString("commencingYear").orEmpty(),
            degreeDurationYears = (snap?.getLong("degreeDurationYears") ?: 0L).toInt(),
            expectedGraduationYear = snap?.getString("expectedGraduationYear").orEmpty(),
            phoneCountryCode = snap?.getString("phoneCountryCode").orEmpty(),
            phoneNumber = snap?.getString("phoneNumber").orEmpty(),
            bio = snap?.getString("bio").orEmpty(),
            languagesKnown = (snap?.get("languagesKnown") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            avatarIndex = storedAvatar,
        )
    }

    fun loadProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _isLoading.value = false
            _error.value = "Not signed in."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val snap = firestore.collection("users").document(uid).get().await()
                _user.value = snapshotToUser(uid, snap, auth.currentUser)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profile."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startLiveListeners() {
        val uid = auth.currentUser?.uid ?: return
        runCatching {
            userProfileListener?.remove()
            userProfileListener = firestore.collection("users").document(uid).addSnapshotListener { snap, err ->
                if (err != null || snap == null || !snap.exists()) return@addSnapshotListener
                _user.value = snapshotToUser(uid, snap, auth.currentUser)
            }
        }
        runCatching {
            eventsListener?.remove()
            eventsListener = eventsRepo.observeEvents { remoteEvents ->
                _events.value = mergeDashboardEvents(remoteEvents)
            }
        }
        runCatching {
            notificationListener?.remove()
            notificationListener = cache.observeRemoteNotifications { remote ->
                remote.filter { it.isUnread && it.id !in seededNotifIds }.forEach { notif ->
                    seededNotifIds.add(notif.id)
                    val prefKey = when (notif.type.lowercase()) {
                        "roommate", "roommate_request", "roommate_accepted", "accommodation_enquiry" -> "notif_room"
                        "forum" -> "notif_forum"
                        else -> "notif_events"
                    }
                    PeersNotificationHelper.showSystemNotification(
                        context = getApplication(),
                        id = notif.id.hashCode(),
                        title = notif.title,
                        body = notif.body,
                        prefKey = prefKey,
                    )
                }
                viewModelScope.launch { runCatching { cache.hydrateNotifications(remote) } }
            }
        }
        runCatching {
            savedEventsListener?.remove()
            savedEventsListener = cache.observeRemoteSavedEvents { remote ->
                viewModelScope.launch { runCatching { cache.hydrateSavedEvents(remote) } }
            }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }

    /** Pull-to-refresh resync across profile, events, saved events, and notifications. */
    fun refreshAll(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                runCatching {
                    val snap = firestore.collection("users").document(uid).get().await()
                    _user.value = snapshotToUser(uid, snap, auth.currentUser)
                }
            }
            refreshEvents()
            runCatching { cache.syncPending() }
            runCatching { cache.refreshNotificationsNow() }
            onComplete()
        }
    }

    /** Responds to an accommodation availability enquiry from in-app notifications. */
    fun respondToEnquiryNotification(notification: PeerNotification, isAvailable: Boolean) {
        viewModelScope.launch {
            val statusLabel = if (isAvailable) "Available now" else "Currently unavailable"
            val resolvedKey = if (isAvailable) "available" else "unavailable"
            cache.resolveNotificationAction(notification.id, resolvedKey)

            // Action format: listingId|listingTitle|ownerPhone|enquirerUid
            val parts = notification.actionData.split("|")
            val listingId = parts.getOrNull(0).orEmpty()
            val listingTitle = parts.getOrNull(1).orEmpty().ifBlank { "Accommodation" }
            val ownerPhone = parts.getOrNull(2).orEmpty().ifBlank { "Call via listing card" }
            val enquirerUid = parts.getOrNull(3).orEmpty()

            if (listingId.isNotBlank()) {
                runCatching {
                    firestore.collection("accommodations").document(listingId)
                        .update("availability", statusLabel)
                        .await()
                }
            }

            val replyTitle = "$listingTitle: ${if (isAvailable) "Available" else "Unavailable"}"
            val replyBody = if (isAvailable) {
                "Good news! $listingTitle is marked Available. Contact owner at $ownerPhone to schedule a visit."
            } else {
                "$listingTitle was marked Unavailable right now. Contact owner ($ownerPhone) for upcoming vacancies."
            }
            cache.emitUnifiedNotification(
                title = replyTitle,
                body = replyBody,
                type = "info",
                prefKey = "notif_room",
                targetUid = enquirerUid.takeIf { it.isNotBlank() && it != auth.currentUser?.uid },
            )
            // Also record confirmation locally so current user sees the feedback immediately
            if (enquirerUid.isNotBlank() && enquirerUid != auth.currentUser?.uid) {
                cache.emitUnifiedNotification(
                    title = "Availability Updated: $listingTitle",
                    body = "You marked $listingTitle as $statusLabel and notified the enquirer.",
                    type = "info",
                    prefKey = "notif_room",
                )
            }
            _actionMessage.value = "Marked $listingTitle as $statusLabel & notified student."
        }
    }

    /** Marks all notifications read in Room immediately, then syncs to Firestore. */
    fun markAllRead() {
        viewModelScope.launch {
            val synced = runCatching { cache.markAllNotificationsRead() }.getOrDefault(false)
            if (!synced) {
                _actionMessage.value =
                    "Marked read on this device — read receipts will sync when you're online."
            }
        }
    }

    /** Saves or removes an event locally first, then syncs to Firestore. */
    fun toggleSavedEvent(event: DashboardEvent) {
        val saved = event.id !in _savedEventIds.value
        viewModelScope.launch {
            val synced = runCatching {
                cache.setSavedEvent(event.toCacheEntity(), saved)
            }.getOrDefault(false)
            _actionMessage.value = when {
                saved && synced -> "Saved ${event.title} for later."
                saved -> "Saved offline — ${event.title} will sync when you're online."
                synced -> "Removed ${event.title} from saved events."
                else -> "Removed offline — the change will sync when you're online."
            }
        }
    }

    private fun DashboardEvent.toCacheEntity() = SavedEventCacheEntity(
        eventId = id,
        title = title,
        organizer = organizer,
        epochDay = date.toEpochDay(),
        time = time.toString(),
        location = location,
        attendees = attendees,
        emoji = "",
        savedAt = System.currentTimeMillis(),
    )

    override fun onCleared() {
        userProfileListener?.remove()
        eventsListener?.remove()
        notificationListener?.remove()
        savedEventsListener?.remove()
        super.onCleared()
    }
}
