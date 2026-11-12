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

    private fun wishlistRef() = uid?.let {
        firestore.collection("users").document(it).collection("wishlist")
    }

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

    /** Live wishlist ids for the signed-in user. */
    fun observeWishlist(onChange: (Set<String>) -> Unit): ListenerRegistration? {
        val ref = wishlistRef() ?: return null
        return ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.w("PeersAccommodation", "wishlist listener failed", error)
                return@addSnapshotListener
            }
            onChange(snapshot?.documents?.map { it.id }?.toSet().orEmpty())
        }
    }

    suspend fun addListing(listing: AccommodationListing) {
        listingsRef().document(listing.id).set(listing.toMap()).await()
    }

    suspend fun setWishlisted(listingId: String, wishlisted: Boolean) {
        val ref = wishlistRef()?.document(listingId) ?: error("Not signed in")
        if (wishlisted) {
            ref.set(mapOf("addedAt" to FieldValue.serverTimestamp())).await()
        } else {
            ref.delete().await()
        }
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
