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

    private var remoteListings: List<AccommodationListing> = emptyList()
    private var localListings: List<AccommodationListing> = emptyList()
    private var listingsListener: ListenerRegistration? = null
    private var wishlistListener: ListenerRegistration? = null

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

    override fun onCleared() {
        listingsListener?.remove()
        wishlistListener?.remove()
        super.onCleared()
    }
}
