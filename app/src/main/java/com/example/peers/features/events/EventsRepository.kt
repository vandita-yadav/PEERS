package com.example.peers.features.events

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/** Firestore-backed events with bundled demo fallbacks. */
class EventsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val uid: String? get() = auth.currentUser?.uid

    private fun eventsRef() = firestore.collection("events")

    private fun attendingRef(uid: String) =
        firestore.collection("users").document(uid).collection("attendingEvents")

    fun observeEvents(onChange: (List<CommunityEvent>) -> Unit): ListenerRegistration {
        return eventsRef()
            .orderBy("startMillis", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("PeersEvents", "events listener failed", error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.documents?.mapNotNull { it.toEvent() }.orEmpty())
            }
    }

    /** Event ids the current student is attending. */
    fun observeMyAttendance(onChange: (Set<String>) -> Unit): ListenerRegistration? {
        val currentUid = uid ?: return null
        return attendingRef(currentUid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.w("PeersEvents", "attendance listener failed", error)
                return@addSnapshotListener
            }
            onChange(snapshot?.documents?.map { it.id }?.toSet().orEmpty())
        }
    }

    fun observeAttendees(
        eventId: String,
        onChange: (List<EventAttendee>) -> Unit,
    ): ListenerRegistration {
        return eventsRef().document(eventId).collection("attendees")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("PeersEvents", "attendees listener failed", error)
                    return@addSnapshotListener
                }
                onChange(
                    snapshot?.documents?.mapNotNull { doc ->
                        EventAttendee(
                            uid = doc.id,
                            name = doc.getString("name") ?: return@mapNotNull null,
                            emoji = doc.getString("emoji") ?: "🧑🏽",
                        )
                    }.orEmpty()
                )
            }
    }

    suspend fun createEvent(event: CommunityEvent) {
        val user = auth.currentUser ?: error("Not signed in")
        eventsRef().document(event.id).set(
            mapOf(
                "title" to event.title,
                "description" to event.description,
                "organizerName" to event.organizerName,
                "organizerType" to event.organizerType.name,
                "venue" to event.venue,
                "city" to event.city,
                "startMillis" to event.startMillis,
                "emoji" to event.emoji,
                "capacity" to event.capacity,
                "attendeeCount" to 0,
                "createdBy" to user.uid,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    /**
     * RSVPs or cancels atomically so the cached count and per-user attendance
     * never drift, even if the user taps twice.
     */
    suspend fun setAttending(event: CommunityEvent, attending: Boolean) {
        val user = auth.currentUser ?: error("Not signed in")
        val eventRef = eventsRef().document(event.id)
        val userRef = attendingRef(user.uid).document(event.id)
        val attendeeRef = eventRef.collection("attendees").document(user.uid)

        firestore.runTransaction { transaction ->
            val already = transaction.get(userRef).exists()
            if (already == attending) return@runTransaction
            val snapshot = transaction.get(eventRef)
            if (!snapshot.exists()) {
                transaction.set(
                    eventRef,
                    mapOf(
                        "title" to event.title,
                        "description" to event.description,
                        "organizerName" to event.organizerName,
                        "organizerType" to event.organizerType.name,
                        "venue" to event.venue,
                        "city" to event.city,
                        "startMillis" to event.startMillis,
                        "emoji" to event.emoji,
                        "capacity" to event.capacity,
                        "attendeeCount" to if (attending) 1 else 0,
                        "createdBy" to "peers_seed",
                        "createdAt" to FieldValue.serverTimestamp(),
                    )
                )
            } else {
                val count = (snapshot.getLong("attendeeCount") ?: 0L).toInt()
                transaction.update(eventRef, "attendeeCount", if (attending) count + 1 else (count - 1).coerceAtLeast(0))
            }
            if (attending) {
                transaction.set(
                    userRef,
                    mapOf("eventId" to event.id, "addedAt" to FieldValue.serverTimestamp()),
                )
                transaction.set(
                    attendeeRef,
                    mapOf(
                        "name" to (user.displayName ?: "You"),
                        "emoji" to "🧑🏽",
                        "joinedAt" to FieldValue.serverTimestamp(),
                    ),
                )
            } else {
                transaction.delete(userRef)
                transaction.delete(attendeeRef)
            }
        }.await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toEvent(): CommunityEvent? {
        val title = getString("title") ?: return null
        return CommunityEvent(
            id = id,
            title = title,
            description = getString("description").orEmpty(),
            organizerName = getString("organizerName").orEmpty().ifBlank { "PEERS Community" },
            organizerType = when (getString("organizerType")) {
                "STUDENT" -> EventOrganizerType.STUDENT
                "UNIVERSITY" -> EventOrganizerType.UNIVERSITY
                else -> EventOrganizerType.COMMUNITY
            },
            venue = getString("venue").orEmpty(),
            city = getString("city").orEmpty(),
            startMillis = getLong("startMillis") ?: 0L,
            emoji = getString("emoji") ?: "🎪",
            capacity = (getLong("capacity") ?: 0L).toInt(),
            attendeeCount = (getLong("attendeeCount") ?: 0L).toInt(),
            createdBy = getString("createdBy").orEmpty(),
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        )
    }
}
