package com.example.peers.features.community

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/** Lifecycle of a peer connection, as shown on cards and the profile sheet. */
enum class ConnectionState { NONE, SENDING, PENDING, CONNECTED }

/**
 * Connection requests are stored per-user under `users/{uid}/connections/{peerId}`
 * so they're cached offline and scoped by the existing security rules.
 *
 * NOTE: Phase 2 uses demo peers; their acceptance is simulated in-app. When the real
 * Community phase lands, the recipient's app writes `status = accepted` instead.
 */
class ConnectionRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val uid: String?
        get() = auth.currentUser?.uid

    private fun connectionsRef() =
        firestore.collection("users").document(uid ?: error("Not signed in"))
            .collection("connections")

    private fun notificationsRef() =
        firestore.collection("users").document(uid ?: error("Not signed in"))
            .collection("notifications")

    /** Live map of peerId → connection state. */
    fun observeConnections(onChange: (Map<String, ConnectionState>) -> Unit): ListenerRegistration {
        return connectionsRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("PeersConnect", "connections listener failed", error)
                onChange(emptyMap())
                return@addSnapshotListener
            }
            val states = snapshot?.documents?.associate { doc ->
                doc.id to when (doc.getString("status")) {
                    "accepted" -> ConnectionState.CONNECTED
                    "pending" -> ConnectionState.PENDING
                    else -> ConnectionState.NONE
                }
            }.orEmpty()
            onChange(states)
        }
    }

    /** Sends a request (idempotent). Returns false if the peer is already connected/pending. */
    suspend fun sendRequest(peerId: String, peerName: String): Boolean {
        val doc = connectionsRef().document(peerId)
        val existing = doc.get().await()
        if (existing.exists() && existing.getString("status") in listOf("pending", "accepted")) {
            return false
        }
        doc.set(
            mapOf(
                "peerId" to peerId,
                "peerName" to peerName,
                "status" to "pending",
                "sentAt" to FieldValue.serverTimestamp(),
            ),
            SetOptions.merge(),
        ).await()
        addNotification(
            title = "Request sent",
            body = "Your connection request to $peerName is on its way.",
            emoji = "📨",
            type = "request_sent",
        )
        return true
    }

    /** Demo-only: the peer "accepts" shortly after the request. */
    suspend fun simulateAcceptance(peerId: String, peerName: String) {
        connectionsRef().document(peerId).set(
            mapOf("status" to "accepted", "acceptedAt" to FieldValue.serverTimestamp()),
            SetOptions.merge(),
        ).await()
        addNotification(
            title = "Connection accepted",
            body = "$peerName accepted your connection request. Say hi! 👋",
            emoji = "🤝",
            type = "connection_accepted",
        )
    }

    /** In-app notification stored under the current user (recipient/sender copies later). */
    private suspend fun addNotification(title: String, body: String, emoji: String, type: String) {
        notificationsRef().add(
            mapOf(
                "title" to title,
                "body" to body,
                "emoji" to emoji,
                "type" to type,
                "read" to false,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    /** Live notifications, newest first. */
    fun observeNotifications(
        onChange: (List<Map<String, Any?>>) -> Unit,
    ): ListenerRegistration {
        return notificationsRef()
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("PeersConnect", "notifications listener failed", error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.documents?.map { it.data.orEmpty() }.orEmpty())
            }
    }

    suspend fun markAllNotificationsRead() {
        val unread = notificationsRef().whereEqualTo("read", false).get().await()
        if (unread.isEmpty) return
        val batch = firestore.batch()
        unread.documents.forEach { batch.update(it.reference, "read", true) }
        batch.commit().await()
    }
}
