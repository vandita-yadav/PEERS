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

/** A community review for a stay. */
data class AccommodationReview(
    val id: String,
    val listingId: String,
    val authorName: String,
    val rating: Int,
    val text: String,
    val createdAt: Long = 0L,
    val isMine: Boolean = false,
    val reported: Boolean = false,
)

/** Contact details a student chooses to share with a roommate request. */
data class ContactInfo(
    val phone: String = "",
    val email: String = "",
    val instagram: String = "",
    val snapchat: String = "",
) {
    val isEmpty: Boolean
        get() = phone.isBlank() && email.isBlank() && instagram.isBlank() && snapchat.isBlank()

    fun visibleEntries(): List<Pair<String, String>> = buildList {
        if (phone.isNotBlank()) add("Phone" to phone)
        if (email.isNotBlank()) add("Email" to email)
        if (instagram.isNotBlank()) add("Instagram" to instagram)
        if (snapchat.isNotBlank()) add("Snapchat" to snapchat)
    }
}

enum class RoommateRequestStatus { PENDING, ACCEPTED, DECLINED }

/** A roommate request tied to a listing. */
data class RoommateRequest(
    val id: String,
    val listingId: String,
    val listingTitle: String,
    val fromName: String,
    val fromUid: String,
    val message: String,
    val contact: ContactInfo,
    val incoming: Boolean,
    val status: RoommateRequestStatus = RoommateRequestStatus.PENDING,
    val toUid: String = "",
    val createdAt: Long = 0L,
)

/** A student looking for a roommate, shown in Find a Roommate. */
data class RoommateSeeker(
    val uid: String,
    val name: String,
    val course: String,
    val year: String,
    val area: String,
    val budget: Int,
    val interests: List<String>,
    val emoji: String,
    val accent: Color,
)

val demoReviews = listOf(
    AccommodationReview(
        id = "r1",
        listingId = "demo-1",
        authorName = "Aisha Verma",
        rating = 5,
        text = "Clean rooms and the WiFi actually works. The aunty is lovely.",
    ),
    AccommodationReview(
        id = "r2",
        listingId = "demo-1",
        authorName = "Sana Khan",
        rating = 4,
        text = "Great location for campus. Water pressure could be better.",
    ),
    AccommodationReview(
        id = "r3",
        listingId = "demo-5",
        authorName = "Rohan Mehta",
        rating = 5,
        text = "Quiet, safe and close to the mess. Would recommend.",
    ),
)

val demoSeekers = listOf(
    RoommateSeeker(
        uid = "s1",
        name = "Meera Nair",
        course = "Computer Science",
        year = "2nd Year",
        area = "Law Gate",
        budget = 8000,
        interests = listOf("Music", "Cooking", "Gaming"),
        emoji = "👩🏽",
        accent = PeersLavender,
    ),
    RoommateSeeker(
        uid = "s2",
        name = "Rohan Mehta",
        course = "Mechanical Engineering",
        year = "3rd Year",
        area = "Urban Estate",
        budget = 10000,
        interests = listOf("Football", "Fitness", "Coffee"),
        emoji = "🧔🏽",
        accent = PeersSky,
    ),
    RoommateSeeker(
        uid = "s3",
        name = "Ananya Rao",
        course = "Business Administration",
        year = "1st Year",
        area = "Banga Road",
        budget = 7500,
        interests = listOf("Reading", "Design", "Dogs"),
        emoji = "👩🏼",
        accent = PeersMint,
    ),
)

val demoIncomingRequests = listOf(
    RoommateRequest(
        id = "demo-req-in",
        listingId = "demo-1",
        listingTitle = "Sunrise Girls PG",
        fromName = "Meera Nair",
        fromUid = "s1",
        message = "Hi! I'm looking for a share at Sunrise from December. Would love to connect.",
        contact = ContactInfo(
            phone = "+91 98765 43210",
            email = "meera.nair@example.com",
            instagram = "@meera.n",
        ),
        incoming = true,
    ),
)


data class AccommodationFilters(
    val query: String = "",
    val maxRent: Int? = null,
    val maxDistanceKm: Double? = null,
    val types: Set<String> = emptySet(),
    val gender: String? = null,
    val furnishing: String? = null,
    val amenities: Set<String> = emptySet(),
    val availableNow: Boolean = false,
) {
    val activeCount: Int
        get() = listOf(
            maxRent != null,
            maxDistanceKm != null,
            types.isNotEmpty(),
            gender != null,
            furnishing != null,
            amenities.isNotEmpty(),
            availableNow,
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
        if (availableNow && !listing.availability.contains("now", ignoreCase = true)) return false
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
