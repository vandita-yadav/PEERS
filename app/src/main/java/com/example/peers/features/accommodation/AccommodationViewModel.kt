package com.example.peers.features.accommodation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AccommodationViewModel : ViewModel() {
    private val repository = AccommodationRepository()

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
    private var requestsListeners: List<ListenerRegistration> = emptyList()

    init {
        runCatching {
            listingsListener = repository.observeListings { remote ->
                remoteListings = remote
                refreshListings()
            }
        }
        runCatching {
            wishlistListener = repository.observeWishlist { ids -> _wishlist.value = ids }
        }
        runCatching {
            requestsListeners = repository.observeRoommateRequests { remote ->
                remoteRequests = remote
                refreshRequests()
            }
        }
    }

    private fun refreshRequests() {
        // Local edits win over the demo/remote copies so accepted or declined
        // state survives a listener refresh.
        _roommateRequests.value = (localRequests + remoteRequests + demoIncomingRequests)
            .distinctBy { it.id }
    }

    private fun refreshListings() {
        val base = if (remoteListings.isNotEmpty()) remoteListings else demoListings
        _listings.value = (localListings + base).distinctBy { it.id }
    }

    fun setQuery(query: String) = updateFilters { it.copy(query = query) }

    fun applyFilters(filters: AccommodationFilters) {
        _filters.value = filters
    }

    fun clearFilters() {
        _filters.value = AccommodationFilters(query = _filters.value.query)
    }

    private fun updateFilters(block: (AccommodationFilters) -> AccommodationFilters) {
        _filters.value = block(_filters.value)
    }

    fun toggleWishlist(listingId: String) {
        val nowWishlisted = listingId !in _wishlist.value
        _wishlist.value = if (nowWishlisted) _wishlist.value + listingId else _wishlist.value - listingId
        viewModelScope.launch {
            runCatching { repository.setWishlisted(listingId, nowWishlisted) }
                .onFailure {
                    _wishlist.value =
                        if (nowWishlisted) _wishlist.value - listingId else _wishlist.value + listingId
                    _message.value = "Wishlist couldn't sync. Check your connection."
                }
        }
    }

    fun addListing(
        title: String,
        type: String,
        rentPerMonth: Int,
        address: String,
        distanceKm: Double,
        genderPreference: String,
        furnishing: String,
        amenities: List<String>,
        availability: String,
        photoUrls: List<String>,
        emoji: String,
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val listing = AccommodationListing(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            type = type,
            rentPerMonth = rentPerMonth,
            address = address.trim(),
            distanceKm = distanceKm,
            genderPreference = genderPreference,
            furnishing = furnishing,
            amenities = amenities,
            availability = availability,
            photoUrls = photoUrls,
            emoji = emoji,
            contributorName = user?.displayName ?: "You",
            contributorUid = user?.uid.orEmpty(),
        )

        // Optimistic local add so the listing appears even if Firestore rules
        // haven't been published yet.
        localListings = listOf(listing) + localListings
        refreshListings()
        _message.value = "Listing added — pending review"

        viewModelScope.launch {
            runCatching { repository.addListing(listing) }
                .onFailure {
                    _message.value = "Saved on this device. Publish accommodation rules to sync."
                }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    /** Marks a listing genuine for the signed-in student (one per student). */
    fun markGenuine(listing: AccommodationListing) {
        if (listing.id in _verifiedIds.value) return
        _verifiedIds.value = _verifiedIds.value + listing.id
        updateListing(listing.id) { it.copy(verifiedCount = it.verifiedCount + 1) }
        _message.value = "Thanks! You marked this stay as genuine."

        viewModelScope.launch {
            runCatching { repository.markGenuine(listing.id) }
                .onFailure {
                    _message.value = "Verification saved locally. Publish the accommodation rules to sync."
                }
        }
    }

    /** Removes the signed-in student's verification. */
    fun unmarkGenuine(listing: AccommodationListing) {
        if (listing.id !in _verifiedIds.value) return
        _verifiedIds.value = _verifiedIds.value - listing.id
        updateListing(listing.id) {
            it.copy(verifiedCount = (it.verifiedCount - 1).coerceAtLeast(0))
        }
        _message.value = "Your verification was removed."

        viewModelScope.launch {
            runCatching { repository.unmarkGenuine(listing.id) }
                .onFailure {
                    _message.value = "Verification removed on this device. Publish the rules to sync."
                }
        }
    }

    /** Adds a single review per student and refreshes the visible aggregate. */
    fun addReview(listing: AccommodationListing, rating: Int, text: String) {
        if (listing.id in _reviewedIds.value) return
        val user = FirebaseAuth.getInstance().currentUser
        val review = AccommodationReview(
            id = UUID.randomUUID().toString(),
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
        updateListing(listing.id) {
            it.copy(
                rating = (newRating * 10).toInt() / 10.0,
                reviewCount = newCount,
            )
        }
        _message.value = "Review posted. Thanks for helping other students!"

        viewModelScope.launch {
            runCatching { repository.addReview(listing.id, review, newRating, newCount) }
                .onFailure {
                    _message.value = "Review saved on this device. Publish the accommodation rules to sync."
                }
        }
    }

    fun reportReview(listingId: String, reviewId: String) {
        _reviews.value = _reviews.value.mapValues { (id, list) ->
            if (id == listingId) list.map { if (it.id == reviewId) it.copy(reported = true) else it } else list
        }
        _message.value = "Review reported. Our team will take a look."
    }

    /** Sends a roommate request with only the contact fields the student chose. */
    fun sendRoommateRequest(
        listing: AccommodationListing,
        contact: ContactInfo,
        message: String,
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val request = RoommateRequest(
            id = UUID.randomUUID().toString(),
            listingId = listing.id,
            listingTitle = listing.title,
            fromName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You",
            fromUid = user?.uid.orEmpty(),
            message = message.trim(),
            contact = contact,
            incoming = false,
            toUid = listing.contributorUid,
        )
        localRequests = listOf(request) + localRequests
        refreshRequests()
        _message.value = "Roommate request sent to ${listing.contributorName.ifBlank { "the contributor" }}."

        viewModelScope.launch {
            runCatching { repository.sendRoommateRequest(request) }
                .onFailure {
                    _message.value = "Request saved locally. Publish the accommodation rules to sync."
                }
        }
    }

    /** Sends a request to a student listed in Find a Roommate. */
    fun sendRoommateRequestToSeeker(seeker: RoommateSeeker) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (_roommateRequests.value.any { !it.incoming && it.toUid == seeker.uid }) {
            _message.value = "You already sent ${seeker.name} a request."
            return
        }
        val user = FirebaseAuth.getInstance().currentUser
        val request = RoommateRequest(
            id = UUID.randomUUID().toString(),
            listingId = "",
            listingTitle = "Roommate in ${seeker.area}",
            fromName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You",
            fromUid = uid,
            message = "Hi ${seeker.name}! I'm also looking for a roommate in ${seeker.area}.",
            contact = ContactInfo(),
            incoming = false,
            toUid = seeker.uid,
        )
        localRequests = listOf(request) + localRequests
        refreshRequests()
        _message.value = "Request sent to ${seeker.name}."

        viewModelScope.launch {
            runCatching { repository.sendRoommateRequest(request) }
                .onFailure {
                    _message.value = "Request saved locally. Publish the accommodation rules to sync."
                }
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
            runCatching { repository.updateRoommateStatus(request.id, accept) }
        }
    }

    private fun updateListing(id: String, transform: (AccommodationListing) -> AccommodationListing) {
        _listings.value = _listings.value.map { if (it.id == id) transform(it) else it }
        remoteListings = remoteListings.map { if (it.id == id) transform(it) else it }
        localListings = localListings.map { if (it.id == id) transform(it) else it }
    }

    override fun onCleared() {
        listingsListener?.remove()
        wishlistListener?.remove()
        requestsListeners.forEach { it.remove() }
        super.onCleared()
    }
}
