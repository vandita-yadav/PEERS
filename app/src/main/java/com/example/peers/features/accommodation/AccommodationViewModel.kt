package com.example.peers.features.accommodation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.features.offline.OfflineCacheRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class AccommodationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AccommodationRepository()
    private val cache = OfflineCacheRepository.get(application)
    private val prefs = application.getSharedPreferences("peers_accom_local", Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()

    val currentUid: String get() = auth.currentUser?.uid.orEmpty()

    private val _listings = MutableStateFlow(demoListings)
    val listings: StateFlow<List<AccommodationListing>> = _listings.asStateFlow()

    private val _filters = MutableStateFlow(AccommodationFilters())
    val filters: StateFlow<AccommodationFilters> = _filters.asStateFlow()

    private val _wishlist = MutableStateFlow<Set<String>>(emptySet())
    val wishlist: StateFlow<Set<String>> = _wishlist.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _reviews = MutableStateFlow(demoReviews.groupBy { it.listingId })
    val reviews: StateFlow<Map<String, List<AccommodationReview>>> = _reviews.asStateFlow()

    private val _verifiedIds = MutableStateFlow<Set<String>>(emptySet())
    val verifiedIds: StateFlow<Set<String>> = _verifiedIds.asStateFlow()

    private val _reviewedIds = MutableStateFlow<Set<String>>(emptySet())
    val reviewedIds: StateFlow<Set<String>> = _reviewedIds.asStateFlow()

    private val _roommateRequests = MutableStateFlow(demoIncomingRequests)
    val roommateRequests: StateFlow<List<RoommateRequest>> = _roommateRequests.asStateFlow()

    private val _seekers = MutableStateFlow(demoSeekers)
    val seekers: StateFlow<List<RoommateSeeker>> = _seekers.asStateFlow()

    private var remoteListings: List<AccommodationListing> = emptyList()
    private var localListings: List<AccommodationListing> = emptyList()
    private var listingsListener: ListenerRegistration? = null
    private var wishlistListener: ListenerRegistration? = null
    private var remoteRequests: List<RoommateRequest> = emptyList()
    private var localRequests: List<RoommateRequest> = emptyList()
    private var cancelledRequestIds: Set<String> = emptySet()
    private var requestsListeners: List<ListenerRegistration> = emptyList()

    private fun getDeletedListingIds(): Set<String> =
        prefs.getStringSet("deleted_listing_ids", emptySet()) ?: emptySet()

    private fun markListingDeleted(id: String) {
        prefs.edit().putStringSet("deleted_listing_ids", getDeletedListingIds() + id).apply()
    }

    private fun loadLocalListings(): List<AccommodationListing> {
        val raw = prefs.getString("user_listings", null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val amArr = obj.optJSONArray("amenities") ?: JSONArray()
                val phArr = obj.optJSONArray("photoUrls") ?: JSONArray()
                AccommodationListing(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    type = obj.optString("type", "PG"),
                    rentPerMonth = obj.optInt("rentPerMonth", 0),
                    address = obj.optString("address", ""),
                    distanceKm = obj.optDouble("distanceKm", 0.0),
                    genderPreference = obj.optString("genderPreference", "Any"),
                    furnishing = obj.optString("furnishing", "Furnished"),
                    amenities = (0 until amArr.length()).map { amArr.getString(it) },
                    availability = obj.optString("availability", "Available now"),
                    photoUrls = (0 until phArr.length()).map { phArr.getString(it) },
                    emoji = "",
                    contributorName = obj.optString("contributorName", "You"),
                    contributorUid = obj.optString("contributorUid", ""),
                    contactPhone = obj.optString("contactPhone", ""),
                    verifiedCount = obj.optInt("verifiedCount", 0),
                    rating = obj.optDouble("rating", 0.0),
                    reviewCount = obj.optInt("reviewCount", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveLocalListings(list: List<AccommodationListing>) {
        runCatching {
            val arr = JSONArray()
            list.forEach { l ->
                arr.put(
                    JSONObject().apply {
                        put("id", l.id)
                        put("title", l.title)
                        put("type", l.type)
                        put("rentPerMonth", l.rentPerMonth)
                        put("address", l.address)
                        put("distanceKm", l.distanceKm)
                        put("genderPreference", l.genderPreference)
                        put("furnishing", l.furnishing)
                        put("amenities", JSONArray(l.amenities))
                        put("availability", l.availability)
                        put("photoUrls", JSONArray(l.photoUrls))
                        put("contributorName", l.contributorName)
                        put("contributorUid", l.contributorUid)
                        put("contactPhone", l.contactPhone)
                        put("verifiedCount", l.verifiedCount)
                        put("rating", l.rating)
                        put("reviewCount", l.reviewCount)
                        put("createdAt", l.createdAt)
                    }
                )
            }
            prefs.edit().putString("user_listings", arr.toString()).apply()
        }
    }

    init {
        localListings = loadLocalListings()
        cancelledRequestIds = prefs.getStringSet("cancelled_req_ids", emptySet()) ?: emptySet()
        refreshListings()

        viewModelScope.launch {
            cache.observeWishlist().collect { ids -> _wishlist.value = ids }
        }
        runCatching {
            listingsListener = repository.observeListings { remote ->
                remoteListings = remote
                refreshListings()
            }
        }
        runCatching {
            wishlistListener = cache.observeRemoteWishlist { ids ->
                viewModelScope.launch { cache.hydrateWishlist(ids) }
            }
        }
        runCatching {
            requestsListeners = repository.observeRoommateRequests { remote ->
                remoteRequests = remote
                refreshRequests()
            }
        }
    }

    fun refreshAll(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            localListings = loadLocalListings()
            runCatching {
                remoteListings = repository.fetchListingsOnce()
            }
            refreshListings()
            refreshRequests()
            onComplete()
        }
    }

    private fun refreshRequests() {
        _roommateRequests.value = (localRequests + remoteRequests + demoIncomingRequests)
            .filter { it.id !in cancelledRequestIds }
            .distinctBy { it.id }
    }

    private fun refreshListings() {
        val deleted = getDeletedListingIds()
        // Always stack user-created + remote + curated demo stays together without wiping demos!
        _listings.value = (localListings + remoteListings + demoListings)
            .filter { it.id !in deleted }
            .distinctBy { it.id }
    }

    fun setQuery(query: String) = updateFilters { it.copy(query = query) }

    fun applyFilters(filters: AccommodationFilters) {
        _filters.value = filters
        _message.value = "Filters saved and applied."
    }

    fun clearFilters() {
        _filters.value = AccommodationFilters(query = _filters.value.query)
    }

    private fun updateFilters(block: (AccommodationFilters) -> AccommodationFilters) {
        _filters.value = block(_filters.value)
    }

    fun toggleWishlist(listingId: String) {
        val nowWishlisted = listingId !in _wishlist.value
        viewModelScope.launch {
            val synced = runCatching { cache.setWishlisted(listingId, nowWishlisted) }
                .getOrDefault(false)
            if (!synced) {
                _message.value = if (nowWishlisted) {
                    "Saved offline — your wishlist will sync when you're back online."
                } else {
                    "Removed offline — the change will sync when you're back online."
                }
            }
        }
    }

    fun addListing(form: ListingForm) {
        val user = auth.currentUser
        val listing = AccommodationListing(
            id = "user-${System.currentTimeMillis()}",
            title = form.title.trim(),
            type = form.type,
            rentPerMonth = form.rentPerMonth,
            address = form.address.trim(),
            distanceKm = form.distanceKm,
            genderPreference = form.gender,
            furnishing = form.furnishing,
            amenities = form.amenities,
            availability = form.availability,
            photoUrls = form.photoUrls,
            emoji = "",
            contributorName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You",
            contributorUid = user?.uid.orEmpty(),
            contactPhone = form.contactPhone.trim(),
            createdAt = System.currentTimeMillis(),
        )

        localListings = listOf(listing) + localListings
        saveLocalListings(localListings)
        refreshListings()
        _message.value = "Listing published!"

        viewModelScope.launch {
            cache.emitUnifiedNotification(
                title = "Accommodation Posted: ${listing.title}",
                body = "${listing.type} at ${listing.address} (${listing.rentLabel}) is now listed.",
                type = "info",
                prefKey = "notif_room",
            )
            runCatching { repository.addListing(listing) }
                .onFailure {
                    _message.value = "Listing saved on this device."
                }
        }
    }

    fun editListing(existing: AccommodationListing, form: ListingForm) {
        val updated = existing.copy(
            title = form.title.trim(),
            type = form.type,
            rentPerMonth = form.rentPerMonth,
            address = form.address.trim(),
            distanceKm = form.distanceKm,
            genderPreference = form.gender,
            furnishing = form.furnishing,
            amenities = form.amenities,
            availability = form.availability,
            photoUrls = form.photoUrls,
            contactPhone = form.contactPhone.trim(),
        )
        localListings = listOf(updated) + localListings.filterNot { it.id == updated.id }
        saveLocalListings(localListings)
        remoteListings = remoteListings.map { if (it.id == updated.id) updated else it }
        refreshListings()
        _message.value = "Listing updated!"

        viewModelScope.launch {
            runCatching { repository.updateListing(updated) }
        }
    }

    fun deleteListing(listing: AccommodationListing) {
        markListingDeleted(listing.id)
        localListings = localListings.filterNot { it.id == listing.id }
        saveLocalListings(localListings)
        remoteListings = remoteListings.filterNot { it.id == listing.id }
        refreshListings()
        _message.value = "Listing deleted."

        viewModelScope.launch {
            runCatching { repository.deleteListing(listing.id) }
        }
    }

    fun sendEnquiry(listing: AccommodationListing, enquiryMessage: String) {
        val user = auth.currentUser
        val meName = user?.displayName?.takeIf { it.isNotBlank() } ?: "A student"
        val ownerPhone = listing.contactPhone.ifBlank { "+91 98140 11200" }
        val actionPayload = "${listing.id}|${listing.title}|$ownerPhone|${currentUid}"
        _message.value = "Enquiry sent to ${listing.contributorName.ifBlank { "owner" }}!"

        viewModelScope.launch {
            val targetOwner = listing.contributorUid.takeIf { it.isNotBlank() && !listing.isDemo }
            cache.emitUnifiedNotification(
                title = "Accommodation Enquiry: ${listing.title}",
                body = "$meName asked about ${listing.title} (${listing.address}): \"${enquiryMessage.trim()}\". Confirm availability so they see your status & contact number ($ownerPhone).",
                type = "accommodation_enquiry",
                prefKey = "notif_room",
                actionData = actionPayload,
                targetUid = targetOwner,
            )
            // If sent to an external contributor, also confirm locally to the student
            if (targetOwner != null && targetOwner != currentUid) {
                cache.emitUnifiedNotification(
                    title = "Enquiry Sent: ${listing.title}",
                    body = "We sent your enquiry to ${listing.contributorName.ifBlank { "the owner" }}. You'll be notified once they confirm availability.",
                    type = "request_sent",
                    prefKey = "notif_room",
                )
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun markGenuine(listing: AccommodationListing) {
        if (listing.id in _verifiedIds.value) return
        _verifiedIds.value = _verifiedIds.value + listing.id
        updateListingState(listing.id) { it.copy(verifiedCount = it.verifiedCount + 1) }
        _message.value = "Thanks! You marked this stay as genuine."

        viewModelScope.launch {
            runCatching { repository.markGenuine(listing.id) }
        }
    }

    fun unmarkGenuine(listing: AccommodationListing) {
        if (listing.id !in _verifiedIds.value) return
        _verifiedIds.value = _verifiedIds.value - listing.id
        updateListingState(listing.id) {
            it.copy(verifiedCount = (it.verifiedCount - 1).coerceAtLeast(0))
        }
        _message.value = "Your verification was removed."

        viewModelScope.launch {
            runCatching { repository.unmarkGenuine(listing.id) }
        }
    }

    fun addReview(listing: AccommodationListing, rating: Int, text: String) {
        if (listing.id in _reviewedIds.value) return
        val user = auth.currentUser
        val review = AccommodationReview(
            id = java.util.UUID.randomUUID().toString(),
            listingId = listing.id,
            authorName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You",
            rating = rating.coerceIn(1, 5),
            text = text.trim(),
            createdAt = System.currentTimeMillis(),
            isMine = true,
        )
        val existing = _reviews.value[listing.id].orEmpty()
        _reviews.value = _reviews.value + (listing.id to (listOf(review) + existing))
        _reviewedIds.value = _reviewedIds.value + listing.id

        val oldCount = listing.reviewCount
        val newCount = oldCount + 1
        val newRating = ((listing.rating * oldCount) + review.rating) / newCount
        updateListingState(listing.id) {
            it.copy(
                rating = (newRating * 10).toInt() / 10.0,
                reviewCount = newCount,
            )
        }
        _message.value = "Review posted. Thanks for helping other students!"

        viewModelScope.launch {
            runCatching { repository.addReview(listing.id, review, newRating, newCount) }
        }
    }

    fun reportReview(listingId: String, reviewId: String) {
        _reviews.value = _reviews.value.mapValues { (id, list) ->
            if (id == listingId) list.map { if (it.id == reviewId) it.copy(reported = true) else it } else list
        }
        _message.value = "Review reported. Our team will take a look."
    }

    fun editReview(listing: AccommodationListing, review: AccommodationReview, newRating: Int, newText: String) {
        val clampedRating = newRating.coerceIn(1, 5)
        val trimmed = newText.trim()
        if (trimmed.isBlank()) return

        val updatedReview = review.copy(rating = clampedRating, text = trimmed)
        val existing = _reviews.value[listing.id].orEmpty()
        _reviews.value = _reviews.value + (listing.id to existing.map { if (it.id == review.id) updatedReview else it })

        val count = listing.reviewCount.coerceAtLeast(1)
        val adjustedTotal = (listing.rating * count) - review.rating + clampedRating
        val newAverage = (adjustedTotal / count).coerceIn(0.0, 5.0)
        val roundedAverage = (newAverage * 10).toInt() / 10.0
        updateListingState(listing.id) { it.copy(rating = roundedAverage) }
        _message.value = "Review updated."

        viewModelScope.launch {
            runCatching { repository.addReview(listing.id, updatedReview, roundedAverage, count) }
        }
    }

    fun deleteReview(listing: AccommodationListing, review: AccommodationReview) {
        val existing = _reviews.value[listing.id].orEmpty()
        _reviews.value = _reviews.value + (listing.id to existing.filter { it.id != review.id })
        _reviewedIds.value = _reviewedIds.value - listing.id

        val oldCount = listing.reviewCount.coerceAtLeast(1)
        val newCount = (oldCount - 1).coerceAtLeast(0)
        val newAverage = if (newCount == 0) 0.0
        else (((listing.rating * oldCount) - review.rating) / newCount).coerceIn(0.0, 5.0)
        val roundedAverage = (newAverage * 10).toInt() / 10.0
        updateListingState(listing.id) { it.copy(rating = roundedAverage, reviewCount = newCount) }
        _message.value = "Review deleted."

        viewModelScope.launch {
            runCatching { repository.deleteReview(listing.id, roundedAverage, newCount) }
        }
    }

    /** Sends a roommate request for a specific PG & notifies both poster/co-seekers and sender. */
    fun sendRoommateRequest(
        listing: AccommodationListing,
        contact: ContactInfo,
        message: String,
    ) {
        val user = auth.currentUser
        val meName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You"
        val request = RoommateRequest(
            id = java.util.UUID.randomUUID().toString(),
            listingId = listing.id,
            listingTitle = listing.title,
            fromName = meName,
            fromUid = user?.uid.orEmpty(),
            message = message.trim(),
            contact = contact,
            incoming = false,
            toUid = listing.contributorUid,
        )
        localRequests = listOf(request) + localRequests
        refreshRequests()
        _message.value = "Roommate request sent for ${listing.title}!"

        viewModelScope.launch {
            cache.emitUnifiedNotification(
                title = "Roommate Request: ${listing.title}",
                body = "$meName is looking for a roommate at ${listing.title}: \"${message.trim()}\"",
                type = "request_sent",
                prefKey = "notif_room",
            )
            val targetOwner = listing.contributorUid.takeIf { it.isNotBlank() && it != currentUid && !listing.isDemo }
            if (targetOwner != null) {
                cache.emitUnifiedNotification(
                    title = "New Roommate Request at ${listing.title}",
                    body = "$meName wants to share ${listing.title}: \"${message.trim()}\"",
                    type = "connection_request",
                    prefKey = "notif_room",
                    targetUid = targetOwner,
                )
            }
            runCatching { repository.sendRoommateRequest(request) }
        }
    }

    /** Sends a roommate request to a student in Find a Roommate with contact details & notifications. */
    fun sendRoommateRequestToSeeker(
        seeker: RoommateSeeker,
        message: String = "Hi ${seeker.name}! I'm also looking for a roommate in ${seeker.area}.",
        contact: ContactInfo = ContactInfo(),
    ) {
        val uid = currentUid
        if (_roommateRequests.value.any { !it.incoming && it.toUid == seeker.uid }) {
            _message.value = "You already sent ${seeker.name} a request."
            return
        }
        val meName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "You"
        val request = RoommateRequest(
            id = java.util.UUID.randomUUID().toString(),
            listingId = "",
            listingTitle = "Roommate with ${seeker.name} (${seeker.area})",
            fromName = meName,
            fromUid = uid,
            message = message.trim(),
            contact = contact,
            incoming = false,
            toUid = seeker.uid,
        )
        localRequests = listOf(request) + localRequests
        refreshRequests()
        _message.value = "Roommate request sent to ${seeker.name}!"

        viewModelScope.launch {
            cache.emitUnifiedNotification(
                title = "Roommate Request Sent to ${seeker.name}",
                body = "Requested to connect as roommates in ${seeker.area}: \"${message.trim()}\"",
                type = "request_sent",
                prefKey = "notif_room",
            )
            runCatching { repository.sendRoommateRequest(request) }
        }
    }

    fun cancelRoommateRequest(request: RoommateRequest) {
        cancelledRequestIds = cancelledRequestIds + request.id
        prefs.edit().putStringSet("cancelled_req_ids", cancelledRequestIds).apply()
        localRequests = localRequests.filterNot { it.id == request.id }
        remoteRequests = remoteRequests.filterNot { it.id == request.id }
        refreshRequests()
        _message.value = "Roommate request withdrawn."

        viewModelScope.launch {
            runCatching { repository.cancelRoommateRequest(request.id) }
        }
    }

    fun respondToRoommateRequest(request: RoommateRequest, accept: Boolean) {
        val updated = request.copy(
            status = if (accept) RoommateRequestStatus.ACCEPTED else RoommateRequestStatus.DECLINED
        )
        localRequests = listOf(updated) + localRequests.filterNot { it.id == request.id }
        refreshRequests()
        _message.value = if (accept) "Roommate request accepted." else "Roommate request declined."

        viewModelScope.launch {
            val statusWord = if (accept) "accepted" else "declined"
            val meName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Your peer"
            cache.emitUnifiedNotification(
                title = "Roommate Request ${statusWord.replaceFirstChar { it.uppercase() }}",
                body = "Roommate request with ${request.fromName} for ${request.listingTitle} was $statusWord.",
                type = if (accept) "connection_accepted" else "info",
                prefKey = "notif_room",
            )
            val otherUid = request.fromUid.takeIf { it.isNotBlank() && it != currentUid && !it.startsWith("s") }
            if (otherUid != null) {
                cache.emitUnifiedNotification(
                    title = "Roommate Request ${statusWord.replaceFirstChar { it.uppercase() }}",
                    body = "$meName $statusWord your roommate request for ${request.listingTitle}!",
                    type = if (accept) "connection_accepted" else "info",
                    prefKey = "notif_room",
                    targetUid = otherUid,
                )
            }
            runCatching { repository.updateRoommateStatus(request.id, accept) }
        }
    }

    private fun updateListingState(id: String, transform: (AccommodationListing) -> AccommodationListing) {
        _listings.value = _listings.value.map { if (it.id == id) transform(it) else it }
        remoteListings = remoteListings.map { if (it.id == id) transform(it) else it }
        localListings = localListings.map { if (it.id == id) transform(it) else it }
        saveLocalListings(localListings)
    }

    override fun onCleared() {
        listingsListener?.remove()
        wishlistListener?.remove()
        requestsListeners.forEach { it.remove() }
        super.onCleared()
    }
}
