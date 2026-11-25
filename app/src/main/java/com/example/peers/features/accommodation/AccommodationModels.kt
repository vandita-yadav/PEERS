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
    val emoji: String = "",
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
        text = "Organized gated colony near Law Gate with easy auto and food access.",
    ),
    AccommodationReview(
        id = "r2",
        listingId = "demo-2",
        authorName = "Rohan Mehta",
        rating = 5,
        text = "Solar backup and CCTV around Simar Enclave work well; super close walk to LPU Law Gate.",
    ),
    AccommodationReview(
        id = "r3",
        listingId = "demo-3",
        authorName = "Sana Khan",
        rating = 4,
        text = "Clean modern studio flats inside Green Valley. Power backup handles summer outages.",
    ),
    AccommodationReview(
        id = "r4",
        listingId = "demo-4",
        authorName = "Meera Nair",
        rating = 5,
        text = "Attached kitchen and individual room inverter are super helpful for long study nights.",
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
        emoji = "",
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
        emoji = "",
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
        emoji = "",
        accent = PeersMint,
    ),
)

val demoIncomingRequests = listOf(
    RoommateRequest(
        id = "demo-req-in",
        listingId = "demo-1",
        listingTitle = "Green Valley Apartments",
        fromName = "Meera Nair",
        fromUid = "s1",
        message = "Hi! I'm looking for a share at Green Valley from next month. Would love to connect.",
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
val listingEmojis = emptyList<String>()

private val demoAccents = listOf(PeersLavender, PeersSky, PeersMint)

/** Genuine LPU / Phagwara student stays curated from Maps, Justdial & Magicbricks. */
val demoListings = listOf(
    AccommodationListing(
        id = "demo-1",
        title = "Green Valley Apartments",
        type = "Apartment",
        rentPerMonth = 9500,
        address = "Green Valley Colony, Law Gate, Phagwara",
        distanceKm = 0.8,
        genderPreference = "Any",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "AC", "Kitchen", "Power Backup", "Parking"),
        availability = "Available now",
        emoji = "",
        accent = demoAccents[0],
        contributorName = "Aisha Verma",
        verifiedCount = 7,
        rating = 4.6,
        reviewCount = 18,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-2",
        title = "Sardar Apartments (The Grand Sardar PG)",
        type = "PG",
        rentPerMonth = 8000,
        address = "Simar Enclave, Meheru (Law Gate), Phagwara",
        distanceKm = 0.6,
        genderPreference = "Any",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "AC", "Power Backup", "Water", "Laundry"),
        availability = "Available now",
        emoji = "",
        accent = demoAccents[1],
        contributorName = "Rohan Mehta",
        verifiedCount = 9,
        rating = 4.7,
        reviewCount = 24,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-3",
        title = "Five 50 Apartments",
        type = "Shared Flat",
        rentPerMonth = 9000,
        address = "Green Valley, Phagwara Sharki, Meheru",
        distanceKm = 0.9,
        genderPreference = "Any",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "AC", "Kitchen", "Power Backup", "Water"),
        availability = "Available now",
        emoji = "",
        accent = demoAccents[2],
        contributorName = "Sana Khan",
        verifiedCount = 6,
        rating = 4.4,
        reviewCount = 15,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-4",
        title = "Divjot Paying Guest (Simar Enclave)",
        type = "PG",
        rentPerMonth = 7200,
        address = "Simar Enclave, Law Gate Road, Meheru, Phagwara",
        distanceKm = 0.7,
        genderPreference = "Any",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "AC", "Kitchen", "Power Backup", "Water"),
        availability = "Available now",
        emoji = "",
        accent = demoAccents[0],
        contributorName = "Meera Nair",
        verifiedCount = 5,
        rating = 4.5,
        reviewCount = 12,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-5",
        title = "GTK Studio Apartments & Housing",
        type = "Apartment",
        rentPerMonth = 8500,
        address = "Meheru, LPU Road, Phagwara",
        distanceKm = 1.0,
        genderPreference = "Any",
        furnishing = "Semi-furnished",
        amenities = listOf("WiFi", "Power Backup", "Parking", "Water"),
        availability = "Available now",
        emoji = "",
        accent = demoAccents[1],
        contributorName = "Kabir Singh",
        verifiedCount = 4,
        rating = 4.3,
        reviewCount = 10,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-6",
        title = "Jagan Stay (Saanjh Apartments)",
        type = "Shared Flat",
        rentPerMonth = 8200,
        address = "Law Gate, Phagwara Sharki",
        distanceKm = 0.8,
        genderPreference = "Girls",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "AC", "Power Backup", "Laundry", "Water"),
        availability = "Available now",
        emoji = "",
        accent = demoAccents[2],
        contributorName = "Ananya Rao",
        verifiedCount = 5,
        rating = 4.5,
        reviewCount = 11,
        isDemo = true,
    ),
    AccommodationListing(
        id = "demo-7",
        title = "Yours Space Boys Hostel",
        type = "Hostel",
        rentPerMonth = 6500,
        address = "Chaheru · Law Gate Road, Phagwara",
        distanceKm = 1.3,
        genderPreference = "Boys",
        furnishing = "Furnished",
        amenities = listOf("WiFi", "Laundry", "Power Backup", "Water"),
        availability = "Available now",
        emoji = "",
        accent = demoAccents[0],
        contributorName = "Dev Patel",
        verifiedCount = 4,
        rating = 4.2,
        reviewCount = 9,
        isDemo = true,
    ),
)
