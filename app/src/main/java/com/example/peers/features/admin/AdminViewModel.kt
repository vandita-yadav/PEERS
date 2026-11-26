package com.example.peers.features.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.features.accommodation.AccommodationListing
import com.example.peers.features.events.CommunityEvent
import com.example.peers.features.events.EventOrganizerType
import com.example.peers.features.forum.ForumPost
import com.example.peers.features.offline.OfflineCacheRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminUserRecord(
    val uid: String,
    val name: String,
    val email: String,
    val university: String,
    val degreeLevel: String,
    val course: String,
    val city: String,
)

data class AdminReportRecord(
    val id: String,
    val targetType: String,
    val targetId: String,
    val reason: String,
    val reportedBy: String,
    val createdAt: Long,
)

data class AdminRentalSummary(
    val id: String,
    val title: String,
    val category: String,
    val postedBy: String,
    val rateLabel: String,
)

data class AdminUiState(
    val users: List<AdminUserRecord> = emptyList(),
    val accommodations: List<AccommodationListing> = emptyList(),
    val events: List<CommunityEvent> = emptyList(),
    val posts: List<ForumPost> = emptyList(),
    val reports: List<AdminReportRecord> = emptyList(),
    val rentals: List<AdminRentalSummary> = emptyList(),
    val admins: Set<String> = AdminConfig.rootAdminEmails,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val cache = OfflineCacheRepository.get(application)

    private val _state = MutableStateFlow(
        AdminUiState(admins = AdminConfig.allAdmins(application))
    )
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    val isCurrentUserAdmin: Boolean
        get() = AdminConfig.isAdmin(auth.currentUser?.email, getApplication())

    init {
        refreshAll()
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(statusMessage = null)
    }

    fun refreshAll(onDone: () -> Unit = {}) {
        if (!isCurrentUserAdmin) {
            onDone()
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val syncedAdmins = AdminConfig.syncAdminsFromFirestore(getApplication(), firestore)

            val users = runCatching {
                firestore.collection("users").limit(100).get().await().documents.map { doc ->
                    AdminUserRecord(
                        uid = doc.id,
                        name = doc.getString("name").orEmpty().ifBlank { "Student" },
                        email = doc.getString("email").orEmpty(),
                        university = doc.getString("university").orEmpty(),
                        degreeLevel = doc.getString("degreeLevel").orEmpty(),
                        course = doc.getString("course").orEmpty(),
                        city = doc.getString("currentCity").orEmpty(),
                    )
                }
            }.getOrDefault(_state.value.users)

            val accommodations = runCatching {
                firestore.collection("accommodations")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(80)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        AccommodationListing(
                            id = doc.id,
                            title = title,
                            type = doc.getString("type") ?: "PG",
                            rentPerMonth = (doc.getLong("rentPerMonth") ?: 0L).toInt(),
                            address = doc.getString("address").orEmpty(),
                            distanceKm = doc.getDouble("distanceKm") ?: 0.0,
                            genderPreference = doc.getString("genderPreference") ?: "Any",
                            furnishing = doc.getString("furnishing") ?: "Furnished",
                            amenities = (doc.get("amenities") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                            availability = doc.getString("availability") ?: "Available now",
                            contributorName = doc.getString("contributorName").orEmpty(),
                            contributorUid = doc.getString("contributorUid").orEmpty(),
                            contactPhone = doc.getString("contactPhone").orEmpty(),
                            verifiedCount = (doc.getLong("verifiedCount") ?: 0L).toInt(),
                            rating = doc.getDouble("rating") ?: 0.0,
                            reviewCount = (doc.getLong("reviewCount") ?: 0L).toInt(),
                            status = doc.getString("status") ?: "active",
                        )
                    }
            }.getOrDefault(_state.value.accommodations)

            val events = runCatching {
                firestore.collection("events")
                    .orderBy("startMillis", Query.Direction.ASCENDING)
                    .limit(80)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        CommunityEvent(
                            id = doc.id,
                            title = title,
                            description = doc.getString("description").orEmpty(),
                            organizerName = doc.getString("organizerName").orEmpty(),
                            organizerType = runCatching {
                                EventOrganizerType.valueOf(doc.getString("organizerType").orEmpty())
                            }.getOrDefault(EventOrganizerType.COMMUNITY),
                            venue = doc.getString("venue").orEmpty(),
                            city = doc.getString("city").orEmpty(),
                            startMillis = doc.getLong("startMillis") ?: 0L,
                            emoji = "",
                            capacity = (doc.getLong("capacity") ?: 50L).toInt(),
                            attendeeCount = (doc.getLong("attendeeCount") ?: 0L).toInt(),
                            createdBy = doc.getString("createdBy").orEmpty(),
                            createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        )
                    }
            }.getOrDefault(_state.value.events)

            val posts = runCatching {
                firestore.collection("forumPosts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(80)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        ForumPost(
                            id = doc.id,
                            title = title,
                            body = doc.getString("body").orEmpty(),
                            authorUid = doc.getString("authorUid").orEmpty(),
                            authorName = doc.getString("authorName").orEmpty().ifBlank { "Student" },
                            authorEmoji = "",
                            communityId = doc.getString("communityId").orEmpty(),
                            communityName = doc.getString("communityName").orEmpty().ifBlank { "General" },
                            university = doc.getString("university").orEmpty(),
                            upvoteCount = (doc.getLong("upvoteCount") ?: 0L).toInt(),
                            commentCount = (doc.getLong("commentCount") ?: 0L).toInt(),
                            createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        )
                    }
            }.getOrDefault(_state.value.posts)

            val reports = runCatching {
                firestore.collection("forumReports")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()
                    .documents
                    .map { doc ->
                        AdminReportRecord(
                            id = doc.id,
                            targetType = doc.getString("targetType").orEmpty(),
                            targetId = doc.getString("targetId").orEmpty(),
                            reason = doc.getString("reason").orEmpty(),
                            reportedBy = doc.getString("reportedBy").orEmpty(),
                            createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        )
                    }
            }.getOrDefault(_state.value.reports)

            val rentals = runCatching {
                firestore.collection("rentalListings").limit(80).get().await().documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    AdminRentalSummary(
                        id = doc.id,
                        title = title,
                        category = doc.getString("category").orEmpty(),
                        postedBy = doc.getString("postedBy").orEmpty(),
                        rateLabel = doc.getString("rateLabel").orEmpty(),
                    )
                }
            }.getOrDefault(_state.value.rentals)

            _state.value = _state.value.copy(
                users = users,
                accommodations = accommodations,
                events = events,
                posts = posts,
                reports = reports,
                rentals = rentals,
                admins = syncedAdmins,
                isLoading = false,
            )
            onDone()
        }
    }

    fun sendBroadcast(title: String, message: String) {
        val t = title.trim()
        val m = message.trim()
        if (t.isBlank() || m.isBlank() || !isCurrentUserAdmin) return
        viewModelScope.launch {
            cache.emitUnifiedNotification(
                title = "Admin Alert: $t",
                body = m,
                type = "info",
                prefKey = "notif_events",
            )
            val myUid = auth.currentUser?.uid
            _state.value.users.forEach { user ->
                if (user.uid != myUid) {
                    runCatching {
                        cache.emitUnifiedNotification(
                            title = "Admin Alert: $t",
                            body = m,
                            type = "info",
                            prefKey = "notif_events",
                            targetUid = user.uid,
                        )
                    }
                }
            }
            _state.value = _state.value.copy(
                statusMessage = "Broadcast sent to ${_state.value.users.size.coerceAtLeast(1)} student accounts."
            )
        }
    }

    fun addCoAdmin(emailInput: String) {
        val email = emailInput.trim().lowercase()
        if (!email.contains("@") || !email.contains(".") || !isCurrentUserAdmin) return
        val next = (_state.value.admins + email)
        val extras = next - AdminConfig.rootAdminEmails
        AdminConfig.saveExtraAdminsLocal(getApplication(), extras)
        _state.value = _state.value.copy(
            admins = next,
            statusMessage = "Added $email as PEERS Admin.",
        )
        viewModelScope.launch {
            runCatching {
                firestore.collection("config").document("admins")
                    .set(mapOf("emails" to extras.toList(), "updatedAt" to FieldValue.serverTimestamp()))
                    .await()
            }
        }
    }

    fun removeCoAdmin(email: String) {
        if (AdminConfig.isRootAdmin(email) || !isCurrentUserAdmin) return
        val next = _state.value.admins - email.lowercase()
        val extras = next - AdminConfig.rootAdminEmails
        AdminConfig.saveExtraAdminsLocal(getApplication(), extras)
        _state.value = _state.value.copy(
            admins = next,
            statusMessage = "Removed admin access for $email.",
        )
        viewModelScope.launch {
            runCatching {
                firestore.collection("config").document("admins")
                    .set(mapOf("emails" to extras.toList(), "updatedAt" to FieldValue.serverTimestamp()))
                    .await()
            }
        }
    }

    fun dismissReport(reportId: String) {
        _state.value = _state.value.copy(
            reports = _state.value.reports.filterNot { it.id == reportId },
            statusMessage = "Report dismissed.",
        )
        viewModelScope.launch {
            runCatching { firestore.collection("forumReports").document(reportId).delete().await() }
        }
    }

    fun deletePost(postId: String) {
        _state.value = _state.value.copy(
            posts = _state.value.posts.filterNot { it.id == postId },
            reports = _state.value.reports.filterNot { it.targetId == postId },
            statusMessage = "Discussion deleted by admin.",
        )
        viewModelScope.launch {
            runCatching { firestore.collection("forumPosts").document(postId).delete().await() }
        }
    }

    fun toggleAccommodationAvailability(listing: AccommodationListing) {
        val nextAvail = if (listing.availability.contains("unavail", ignoreCase = true)) {
            "Available now"
        } else {
            "Currently unavailable"
        }
        _state.value = _state.value.copy(
            accommodations = _state.value.accommodations.map {
                if (it.id == listing.id) it.copy(availability = nextAvail) else it
            },
            statusMessage = "${listing.title} marked $nextAvail.",
        )
        viewModelScope.launch {
            runCatching {
                firestore.collection("accommodations").document(listing.id)
                    .update("availability", nextAvail)
                    .await()
            }
        }
    }

    fun verifyAccommodation(listing: AccommodationListing) {
        val nextCount = listing.verifiedCount + 1
        _state.value = _state.value.copy(
            accommodations = _state.value.accommodations.map {
                if (it.id == listing.id) it.copy(verifiedCount = nextCount, status = "active") else it
            },
            statusMessage = "Verified ${listing.title}.",
        )
        viewModelScope.launch {
            runCatching {
                firestore.collection("accommodations").document(listing.id)
                    .update(mapOf("verifiedCount" to nextCount, "status" to "active"))
                    .await()
            }
        }
    }

    fun deleteAccommodation(listingId: String) {
        _state.value = _state.value.copy(
            accommodations = _state.value.accommodations.filterNot { it.id == listingId },
            statusMessage = "Accommodation deleted by admin.",
        )
        viewModelScope.launch {
            runCatching { firestore.collection("accommodations").document(listingId).delete().await() }
        }
    }

    fun deleteEvent(eventId: String) {
        _state.value = _state.value.copy(
            events = _state.value.events.filterNot { it.id == eventId },
            statusMessage = "Event deleted by admin.",
        )
        viewModelScope.launch {
            runCatching { firestore.collection("events").document(eventId).delete().await() }
        }
    }

    fun deleteRental(rentalId: String) {
        _state.value = _state.value.copy(
            rentals = _state.value.rentals.filterNot { it.id == rentalId },
            statusMessage = "Rental/Exchange listing deleted by admin.",
        )
        viewModelScope.launch {
            runCatching { firestore.collection("rentalListings").document(rentalId).delete().await() }
        }
    }

    fun deleteUserProfile(record: AdminUserRecord) {
        if (AdminConfig.isRootAdmin(record.email)) {
            _state.value = _state.value.copy(statusMessage = "Root admins cannot be removed.")
            return
        }
        _state.value = _state.value.copy(
            users = _state.value.users.filterNot { it.uid == record.uid },
            statusMessage = "Removed profile record for ${record.name}.",
        )
        viewModelScope.launch {
            runCatching { firestore.collection("users").document(record.uid).delete().await() }
        }
    }
}
