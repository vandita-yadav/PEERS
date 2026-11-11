package com.example.peers.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Loads the signed-in user's profile from Firestore and exposes dashboard state.
 * Community content (events, suggestions) is mocked for Phase 2 and replaced by
 * real queries in the Community phase.
 */
class DashboardViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableStateFlow<DashboardUser?>(null)
    val user: StateFlow<DashboardUser?> = _user

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _unreadCount = MutableStateFlow(DashboardMockData.notifications.count { it.isUnread })
    val unreadCount: StateFlow<Int> = _unreadCount

    init {
        loadProfile()
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

    fun refreshNotifications() {
        _unreadCount.value = DashboardMockData.notifications.count { it.isUnread }
    }

    fun markAllRead() {
        _unreadCount.value = 0
    }
}
