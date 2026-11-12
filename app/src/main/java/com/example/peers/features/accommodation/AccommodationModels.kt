package com.example.peers.features.accommodation

import androidx.compose.ui.graphics.Color
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersSky

/** A student-friendly rental listing shown in the Nearby Accommodation section. */
data class AccommodationListing(
    val id: String,
    val title: String,
    val type: String,
    val rentPerMonth: Int,
    val address: String,
    val distanceKm: Double,
    val genderPreference: String,
    val furnishing: String,
    val amenities: List<String>,
    val availability: String,
    val photoUrls: List<String> = emptyList(),
    val emoji: String = "🏠",
    val accent: Color = PeersLavender,
    val contributorName: String = "",
    val contributorUid: String = "",
    val verifiedCount: Int = 0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val status: String = "active",
    val createdAt: Long = 0L,
    val isDemo: Boolean = false,
) {
    val isVerified: Boolean get() = verifiedCount >= 3
    val rentLabel: String get() = "₹${"%,d".format(rentPerMonth)}/mo"
    val distanceLabel: String get() = "%.1f km".format(distanceKm)
}

/** Filter state for the listings screen. */
data class AccommodationFilters(
    val query: String = "",
    val maxRent: Int? = null,
    val maxDistanceKm: Double? = null,
    val types: Set<String> = emptySet(),
    val gender: String? = null,
    val furnishing: String? = null,
    val amenities: Set<String> = emptySet(),
) {
    val activeCount: Int
        get() = listOf(
            maxRent != null,
            maxDistanceKm != null,
            types.isNotEmpty(),
            gender != null,
            furnishing != null,
            amenities.isNotEmpty(),
        ).count { it }

    fun matches(listing: AccommodationListing): Boolean {
        if (query.isNotBlank()) {
            val haystack = "${listing.title} ${listing.address} ${listing.type}".lowercase()
            if (!haystack.contains(query.trim().lowercase())) return false
        }
        if (maxRent != null && listing.rentPerMonth > maxRent) return false
        if (maxDistanceKm != null && listing.distanceKm > maxDistanceKm) return false
        if (types.isNotEmpty() && listing.type !in types) return false
        if (gender != null && listing.genderPreference != gender && listing.genderPreference != "Any") return false
        if (furnishing != null && listing.furnishing != furnishing) return false
        if (amenities.isNotEmpty() && !listing.amenities.containsAll(amenities)) return false
        return true
    }
}

val accommodationTypes = listOf("PG", "Apartment", "Hostel", "Shared Flat")
val genderOptions = listOf("Any", "Girls", "Boys")
val furnishingOptions = listOf("Furnished", "Semi-furnished", "Unfurnished")
val amenityOptions = listOf(
    "WiFi", "AC", "Kitchen", "Laundry", "Parking", "Power Backup", "Water", "Gym",
)
val listingEmojis = listOf("🏠", "🏢", "🛏️", "🏡", "🏘️", "🛋️")

private val demoAccents = listOf(PeersLavender, PeersSky, PeersMint)

/** Demo inventory so the experience is usable before Firestore listings are seeded. */
val demoListings = listOf(
    AccommodationListing(
        id = "demo-1",
        title = "Sunrise Girls PG",
        type = "PG",
        rentPerMonth = 7500,
        address = "Law Gate, Phagwara",
        distanceKm = 1.2,
        genderPreference = "Girls",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "AC", "Laundry", "Water"),
        availability = "Available now",
        emoji = "🛏️",
        accent = demoAccents[0],
        contributorName = "Meera Nair",
        verifiedCount = 6,
        rating = 4.6,
        reviewCount = 12,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-2",
        title = "Green Valley Apartments",
        type = "Apartment",
        rentPerMonth = 12000,
        address = "Urban Estate, Phagwara",
        distanceKm = 3.4,
        genderPreference = "Any",
        furnishing = "Semi-furnished",
        amenities = listOf("WiFi", "Kitchen", "Parking", "Power Backup"),
        availability = "From 1 Dec",
        emoji = "🏢",
        accent = demoAccents[1],
        contributorName = "Aisha Verma",
        verifiedCount = 2,
        rating = 4.2,
        reviewCount = 5,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-3",
        title = "Campus Edge Boys Hostel",
        type = "Hostel",
        rentPerMonth = 6000,
        address = "Jalandhar Road, Phagwara",
        distanceKm = 0.8,
        genderPreference = "Boys",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "Water", "Power Backup"),
        availability = "Available now",
        emoji = "🏘️",
        accent = demoAccents[2],
        contributorName = "Rohan Mehta",
        verifiedCount = 4,
        rating = 4.0,
        reviewCount = 9,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-4",
        title = "The Loft Shared Flat",
        type = "Shared Flat",
        rentPerMonth = 9500,
        address = "Model Town, Phagwara",
        distanceKm = 2.6,
        genderPreference = "Any",
        furnishing = "Unfurnished",
        amenities = listOf("Kitchen", "Parking", "Gym"),
        availability = "From 20 Nov",
        emoji = "🛋️",
        accent = demoAccents[0],
        contributorName = "Kabir Singh",
        verifiedCount = 1,
        rating = 3.9,
        reviewCount = 3,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-5",
        title = "Maple Residency PG",
        type = "PG",
        rentPerMonth = 8200,
        address = "Banga Road, Phagwara",
        distanceKm = 4.1,
        genderPreference = "Any",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "AC", "Laundry", "Kitchen", "Water"),
        availability = "Available now",
        emoji = "🏡",
        accent = demoAccents[1],
        contributorName = "Sana Khan",
        verifiedCount = 5,
        rating = 4.7,
        reviewCount = 18,
        isDemo = true,
    ),
)
