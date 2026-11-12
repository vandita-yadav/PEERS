package com.example.peers.features.accommodation

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Firestore-backed accommodation inventory. Reads are best-effort so the demo
 * listings remain usable before the accommodation rules are published.
 */
class AccommodationRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val uid: String? get() = auth.currentUser?.uid

    private fun listingsRef() = firestore.collection("accommodations")

    /** Live listings, newest first. */
    fun observeListings(onChange: (List<AccommodationListing>) -> Unit): ListenerRegistration {
        return listingsRef()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("PeersAccommodation", "listings listener failed", error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.documents?.mapNotNull { it.toListing() }.orEmpty())
            }
    }

    suspend fun addListing(listing: AccommodationListing) {
        listingsRef().document(listing.id).set(listing.toMap()).await()
    }

    /** Records one genuine-verification per student and bumps the listing counter. */
    suspend fun markGenuine(listingId: String) {
        val user = auth.currentUser ?: error("Not signed in")
        val listing = listingsRef().document(listingId)
        val verification = listing.collection("verifications").document(user.uid)
        firestore.runTransaction { transaction ->
            val listingSnapshot = transaction.get(listing)
            if (!listingSnapshot.exists()) error("Listing not found")
            if (transaction.get(verification).exists()) error("Already verified")
            val count = (listingSnapshot.getLong("verifiedCount") ?: 0L).toInt()
            transaction.update(listing, "verifiedCount", count + 1)
            transaction.set(verification, mapOf("createdAt" to FieldValue.serverTimestamp()))
        }.await()
    }

    /** Removes the signed-in student's verification if they change their mind. */
    suspend fun unmarkGenuine(listingId: String) {
        val user = auth.currentUser ?: error("Not signed in")
        val listing = listingsRef().document(listingId)
        val verification = listing.collection("verifications").document(user.uid)
        firestore.runTransaction { transaction ->
            val listingSnapshot = transaction.get(listing)
            if (!listingSnapshot.exists()) error("Listing not found")
            if (!transaction.get(verification).exists()) return@runTransaction
            val count = (listingSnapshot.getLong("verifiedCount") ?: 0L).toInt()
            transaction.update(listing, "verifiedCount", (count - 1).coerceAtLeast(0))
            transaction.delete(verification)
        }.await()
    }

    /** Adds one review per student and refreshes the listing aggregate. */
    suspend fun addReview(
        listingId: String,
        review: AccommodationReview,
        newRating: Double,
        newCount: Int,
    ) {
        val user = auth.currentUser ?: error("Not signed in")
        val listing = listingsRef().document(listingId)
        listing.collection("reviews").document(user.uid).set(
            mapOf(
                "authorName" to review.authorName,
                "rating" to review.rating,
                "text" to review.text,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        listing.update(
            mapOf("rating" to newRating, "reviewCount" to newCount)
        ).await()
    }

    /** Sends a roommate request to the listing contributor. */
    suspend fun sendRoommateRequest(request: RoommateRequest) {
        val user = auth.currentUser ?: error("Not signed in")
        firestore.collection("roommateRequests").document(request.id).set(
            mapOf(
                "listingId" to request.listingId,
                "listingTitle" to request.listingTitle,
                "fromName" to user.displayName.orEmpty(),
                "fromUid" to user.uid,
                "toUid" to request.toUid,
                "message" to request.message,
                "phone" to request.contact.phone,
                "email" to request.contact.email,
                "instagram" to request.contact.instagram,
                "snapchat" to request.contact.snapchat,
                "status" to request.status.name.lowercase(),
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    /** Roommate requests involving the signed-in student, live from Firestore. */
    fun observeRoommateRequests(onChange: (List<RoommateRequest>) -> Unit): List<ListenerRegistration> {
        val user = auth.currentUser ?: return emptyList()
        val byId = linkedMapOf<String, RoommateRequest>()

        fun listen(query: Query): ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.w("PeersAccommodation", "roommate listener failed", error)
                return@addSnapshotListener
            }
            snapshot?.documents?.mapNotNull { it.toRoommateRequest(user.uid) }
                ?.forEach { byId[it.id] = it }
            onChange(byId.values.toList())
        }

        return listOf(
            listen(firestore.collection("roommateRequests").whereEqualTo("fromUid", user.uid)),
            listen(firestore.collection("roommateRequests").whereEqualTo("toUid", user.uid)),
        )
    }

    /** Accepts or declines a roommate request. */
    suspend fun updateRoommateStatus(requestId: String, accept: Boolean) {
        firestore.collection("roommateRequests").document(requestId)
            .update("status", if (accept) "accepted" else "declined")
            .await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toRoommateRequest(
        currentUid: String,
    ): RoommateRequest? {
        val id = id.ifBlank { return null }
        val fromUid = getString("fromUid") ?: return null
        val toUid = getString("toUid").orEmpty()
        val status = when (getString("status")) {
            "accepted" -> RoommateRequestStatus.ACCEPTED
            "declined" -> RoommateRequestStatus.DECLINED
            else -> RoommateRequestStatus.PENDING
        }
        return RoommateRequest(
            id = id,
            listingId = getString("listingId").orEmpty(),
            listingTitle = getString("listingTitle").orEmpty(),
            fromName = getString("fromName").orEmpty(),
            fromUid = fromUid,
            message = getString("message").orEmpty(),
            contact = ContactInfo(
                phone = getString("phone").orEmpty(),
                email = getString("email").orEmpty(),
                instagram = getString("instagram").orEmpty(),
                snapchat = getString("snapchat").orEmpty(),
            ),
            incoming = toUid == currentUid && fromUid != currentUid,
            status = status,
            toUid = toUid,
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toListing(): AccommodationListing? {
        val id = id.ifBlank { return null }
        val title = getString("title") ?: return null
        return AccommodationListing(
            id = id,
            title = title,
            type = getString("type") ?: "Apartment",
            rentPerMonth = (getLong("rentPerMonth") ?: 0L).toInt(),
            address = getString("address").orEmpty(),
            distanceKm = getDouble("distanceKm") ?: 0.0,
            genderPreference = getString("genderPreference") ?: "Any",
            furnishing = getString("furnishing") ?: "Unfurnished",
            amenities = (get("amenities") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            availability = getString("availability") ?: "Available now",
            photoUrls = (get("photoUrls") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            emoji = getString("emoji") ?: "🏠",
            contributorName = getString("contributorName").orEmpty(),
            contributorUid = getString("contributorUid").orEmpty(),
            verifiedCount = (getLong("verifiedCount") ?: 0L).toInt(),
            rating = getDouble("rating") ?: 0.0,
            reviewCount = (getLong("reviewCount") ?: 0L).toInt(),
            status = getString("status") ?: "active",
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        )
    }

    private fun AccommodationListing.toMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "type" to type,
        "rentPerMonth" to rentPerMonth,
        "address" to address,
        "distanceKm" to distanceKm,
        "genderPreference" to genderPreference,
        "furnishing" to furnishing,
        "amenities" to amenities,
        "availability" to availability,
        "photoUrls" to photoUrls,
        "emoji" to emoji,
        "contributorName" to contributorName,
        "contributorUid" to (contributorUid.ifBlank { uid.orEmpty() }),
        "verifiedCount" to verifiedCount,
        "rating" to rating,
        "reviewCount" to reviewCount,
        "status" to status,
        "createdAt" to FieldValue.serverTimestamp(),
    )
}
