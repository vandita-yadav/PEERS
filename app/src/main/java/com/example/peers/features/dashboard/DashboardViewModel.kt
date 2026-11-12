package com.example.peers.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.features.community.ConnectionRepository
import com.example.peers.features.community.ConnectionState
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
class DashboardViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val connections = ConnectionRepository()

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

    /** Real in-app notifications; falls back to the mock list when there are none yet. */
    private val _liveNotifications = MutableStateFlow<List<PeerNotification>>(emptyList())
    private val _notifications = MutableStateFlow<List<PeerNotification>>(DashboardMockData.notifications)
    val notifications: StateFlow<List<PeerNotification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(DashboardMockData.notifications.count { it.isUnread })
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var connectionListener: ListenerRegistration? = null
    private var notificationListener: ListenerRegistration? = null

    init {
        loadProfile()
        startLiveListeners()
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
            notificationListener = connections.observeNotifications { docs ->
                val mapped = docs.mapNotNull { data ->
                    val title = data["title"] as? String ?: return@mapNotNull null
                    val createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)
                        ?.toDate()?.time ?: System.currentTimeMillis()
                    PeerNotification(
                        id = "$title-$createdAt-${data["body"].hashCode()}",
                        title = title,
                        body = data["body"] as? String ?: "",
                        emoji = data["emoji"] as? String ?: "🔔",
                        timestampMillis = createdAt,
                        isUnread = data["read"] != true,
                        type = when (data["type"] as? String) {
                            "connection_request" -> NotificationType.CONNECTION_REQUEST
                            "connection_accepted" -> NotificationType.CONNECTION_ACCEPTED
                            "request_sent" -> NotificationType.REQUEST_SENT
                            else -> NotificationType.INFO
                        },
                    )
                }
                _liveNotifications.value = mapped
                _notifications.value =
                    if (mapped.isEmpty()) DashboardMockData.notifications else mapped
                _unreadCount.value = _notifications.value.count { it.isUnread }
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
            runCatching { connections.markAllNotificationsRead() }
            // Instantly reflect read state in the UI (Firestore listener confirms shortly).
            _notifications.value = _notifications.value.map { it.copy(isUnread = false) }
            _liveNotifications.value = _liveNotifications.value.map { it.copy(isUnread = false) }
            _unreadCount.value = 0
        }
    }

    override fun onCleared() {
        connectionListener?.remove()
        notificationListener?.remove()
        super.onCleared()
    }
}
