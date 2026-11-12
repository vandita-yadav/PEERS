package com.example.peers.features.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.features.community.ConnectionRepository
import com.example.peers.features.community.ConnectionState
import com.example.peers.features.offline.OfflineCacheRepository
import com.example.peers.features.offline.SavedEventCacheEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Loads the signed-in user's profile from Firestore and exposes dashboard state,
 * live connection states and in-app notifications.
 *
 * Community content (events, suggestions) is mocked for Phase 2 and replaced by
 * real queries in the Community phase.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val connections = ConnectionRepository()
    private val cache = OfflineCacheRepository.get(application)

    private val _user = MutableStateFlow<DashboardUser?>(null)
    val user: StateFlow<DashboardUser?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** peerId → connection state (live from Firestore). */
    private val _connectionStates = MutableStateFlow<Map<String, ConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, ConnectionState>> = _connectionStates.asStateFlow()

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

    private var connectionListener: ListenerRegistration? = null
    private var notificationListener: ListenerRegistration? = null
    private var savedEventsListener: ListenerRegistration? = null

    init {
        loadProfile()
        observeCachedState()
        startLiveListeners()
    }

    private fun observeCachedState() {
        viewModelScope.launch {
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
                            "connection_request" -> NotificationType.CONNECTION_REQUEST
                            "connection_accepted" -> NotificationType.CONNECTION_ACCEPTED
                            "request_sent" -> NotificationType.REQUEST_SENT
                            else -> NotificationType.INFO
                        },
                    )
                }
                _unreadCount.value = rows.count { it.isUnread }
            }
        }
        viewModelScope.launch {
            cache.observeSavedEventIds().collect { _savedEventIds.value = it }
        }
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
                val authUser = auth.currentUser
                _user.value = DashboardUser(
                    uid = uid,
                    name = snap.getString("name").orEmpty().ifEmpty { authUser?.displayName ?: "Peer" },
                    email = snap.getString("email").orEmpty().ifEmpty { authUser?.email.orEmpty() },
                    university = snap.getString("university").orEmpty(),
                    course = snap.getString("course").orEmpty(),
                    year = snap.getString("yearOfStudy").orEmpty(),
                    city = snap.getString("currentCity").orEmpty(),
                    photoUrl = snap.getString("photoUrl")?.takeIf { it.isNotBlank() }
                        ?: authUser?.photoUrl?.toString(),
                    interests = snap.get("interests") as? List<String> ?: emptyList(),
                    needs = snap.get("needs") as? List<String> ?: emptyList(),
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profile."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startLiveListeners() {
        if (auth.currentUser == null) return
        runCatching {
            connectionListener?.remove()
            connectionListener = connections.observeConnections { states ->
                _connectionStates.value = states
            }
        }
        runCatching {
            notificationListener?.remove()
            notificationListener = cache.observeRemoteNotifications { remote ->
                viewModelScope.launch { cache.hydrateNotifications(remote) }
            }
        }
        runCatching {
            savedEventsListener?.remove()
            savedEventsListener = cache.observeRemoteSavedEvents { remote ->
                viewModelScope.launch { cache.hydrateSavedEvents(remote) }
            }
        }
    }

    /** Sends a connection request; demo peers accept shortly after. */
    fun connect(peer: SuggestedPeer) {
        val current = _connectionStates.value[peer.uid] ?: ConnectionState.NONE
        if (current != ConnectionState.NONE) return

        _connectionStates.value = _connectionStates.value + (peer.uid to ConnectionState.SENDING)
        viewModelScope.launch {
            try {
                connections.sendRequest(peer.uid, peer.name)
                _actionMessage.value = "Request sent to ${peer.name.split(" ").first()} ✨"
                if (peer.isDemoPeer) {
                    delay(3500)
                    connections.simulateAcceptance(peer.uid, peer.name)
                    _actionMessage.value = "${peer.name.split(" ").first()} accepted! You can chat soon 🎉"
                }
            } catch (e: Exception) {
                _connectionStates.value = _connectionStates.value + (peer.uid to ConnectionState.NONE)
                _actionMessage.value = "Couldn't send request: ${e.message}"
            }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }

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
        emoji = emoji,
        savedAt = System.currentTimeMillis(),
    )

    override fun onCleared() {
        connectionListener?.remove()
        notificationListener?.remove()
        savedEventsListener?.remove()
        super.onCleared()
    }
}
