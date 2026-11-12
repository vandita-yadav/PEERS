package com.example.peers.features.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.room.withTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Offline-first cache for user state that also lives in Firestore.
 *
 * Room is always read first so the UI paints instantly and keeps working offline.
 * Firestore snapshots hydrate the cache, and local writes are marked pending until
 * they can be pushed. A connectivity callback flushes pending writes on reconnect.
 */
class OfflineCacheRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val database = OfflineCacheDatabase.get(appContext)
    private val dao = database.cacheDao()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val uid: String? get() = auth.currentUser?.uid

    private fun wishlistRef() =
        uid?.let { firestore.collection("users").document(it).collection("wishlist") }

    private fun notificationsRef() =
        uid?.let { firestore.collection("users").document(it).collection("notifications") }

    private fun savedEventsRef() =
        uid?.let { firestore.collection("users").document(it).collection("savedEvents") }

    init {
        seedDemoNotifications()
        registerConnectivity()
    }

    // ── Flows (always local-first) ───────────────────────────────────────────

    fun observeWishlist(): Flow<Set<String>> =
        dao.observeWishlistIds().map { it.toSet() }

    fun observeNotifications(): Flow<List<NotificationCacheEntity>> =
        dao.observeNotifications()

    fun observeSavedEventIds(): Flow<Set<String>> =
        dao.observeSavedEventIds().map { it.toSet() }

    fun observeSavedEvents(): Flow<List<SavedEventCacheEntity>> =
        dao.observeSavedEvents()

    // ── Remote listeners ─────────────────────────────────────────────────────

    fun observeRemoteWishlist(onChange: (Set<String>) -> Unit): ListenerRegistration? {
        val ref = wishlistRef() ?: return null
        return ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.w("PeersCache", "wishlist listener failed", error)
                return@addSnapshotListener
            }
            onChange(snapshot?.documents?.map { it.id }?.toSet().orEmpty())
        }
    }

    fun observeRemoteNotifications(
        onChange: (List<RemoteNotification>) -> Unit,
    ): ListenerRegistration? {
        val ref = notificationsRef() ?: return null
        return ref
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("PeersCache", "notifications listener failed", error)
                    return@addSnapshotListener
                }
                onChange(
                    snapshot?.documents?.mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        RemoteNotification(
                            id = doc.id,
                            title = title,
                            body = doc.getString("body").orEmpty(),
                            emoji = doc.getString("emoji") ?: "🔔",
                            timestampMillis = doc.getTimestamp("createdAt")?.toDate()?.time
                                ?: System.currentTimeMillis(),
                            isUnread = doc.getBoolean("read") != true,
                            type = doc.getString("type") ?: "info",
                        )
                    }.orEmpty()
                )
            }
    }

    fun observeRemoteSavedEvents(
        onChange: (List<SavedEventCacheEntity>) -> Unit,
    ): ListenerRegistration? {
        val ref = savedEventsRef() ?: return null
        return ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.w("PeersCache", "saved events listener failed", error)
                return@addSnapshotListener
            }
            onChange(
                snapshot?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    SavedEventCacheEntity(
                        eventId = doc.id,
                        title = title,
                        organizer = doc.getString("organizer").orEmpty(),
                        epochDay = doc.getLong("epochDay") ?: 0L,
                        time = doc.getString("time").orEmpty(),
                        location = doc.getString("location").orEmpty(),
                        attendees = (doc.getLong("attendees") ?: 0L).toInt(),
                        emoji = doc.getString("emoji") ?: "🎪",
                        savedAt = doc.getLong("savedAt") ?: 0L,
                        pendingSync = false,
                        pendingDelete = false,
                    )
                }.orEmpty()
            )
        }
    }

    // ── Hydration ────────────────────────────────────────────────────────────

    suspend fun hydrateWishlist(remoteIds: Set<String>) = database.withTransaction {
        val local = dao.wishlistRows()
        val pending = local.filter { it.pendingSync }
        dao.clearSyncedWishlist()
        remoteIds.forEach { id ->
            if (pending.none { it.listingId == id }) {
                dao.upsertWishlist(WishlistCacheEntity(listingId = id))
            }
        }
        // Local intent wins until it has been pushed.
        pending.forEach { row ->
            val stillRemote = row.listingId in remoteIds
            when {
                row.pendingDelete && !stillRemote -> dao.deleteWishlist(row.listingId)
                !row.pendingDelete && stillRemote ->
                    dao.upsertWishlist(row.copy(pendingSync = false))
                else -> dao.upsertWishlist(row)
            }
        }
    }

    suspend fun hydrateNotifications(remote: List<RemoteNotification>) {
        if (remote.isEmpty()) return // keep the demo seed while the server is empty
        database.withTransaction {
            val local = dao.notificationRows().associateBy { it.id }
            dao.clearSyncedNotifications()
            remote.forEach { item ->
                val pendingLocal = local[item.id]
                if (pendingLocal != null && pendingLocal.pendingRead) {
                    // Keep the local read state until the server catches up.
                    dao.upsertNotification(
                        item.toEntity(
                            isUnread = false,
                            remoteBacked = true,
                            pendingRead = item.isUnread,
                        )
                    )
                } else {
                    dao.upsertNotification(item.toEntity())
                }
            }
        }
    }

    suspend fun hydrateSavedEvents(remote: List<SavedEventCacheEntity>) = database.withTransaction {
        val local = dao.savedEventRows().associateBy { it.eventId }
        dao.clearSyncedSavedEvents()
        remote.forEach { item ->
            val pendingLocal = local[item.eventId]
            if (pendingLocal == null || !pendingLocal.pendingSync) {
                dao.upsertSavedEvent(item)
            }
        }
    }

    // ── Local-first writes ───────────────────────────────────────────────────

    /** Returns true when the change reached Firestore immediately. */
    suspend fun setWishlisted(listingId: String, wishlisted: Boolean): Boolean {
        dao.upsertWishlist(
            WishlistCacheEntity(
                listingId = listingId,
                addedAt = System.currentTimeMillis(),
                pendingSync = true,
                pendingDelete = !wishlisted,
            )
        )
        val ref = wishlistRef()?.document(listingId) ?: return false
        return runCatching {
            if (wishlisted) {
                ref.set(mapOf("addedAt" to FieldValue.serverTimestamp())).await()
                dao.upsertWishlist(WishlistCacheEntity(listingId = listingId, pendingSync = false, pendingDelete = false))
            } else {
                ref.delete().await()
                dao.deleteWishlist(listingId)
            }
            true
        }.getOrElse {
            android.util.Log.w("PeersCache", "wishlist write queued", it)
            false
        }
    }

    /** Returns true when all read receipts reached Firestore immediately. */
    suspend fun markAllNotificationsRead(): Boolean {
        val unreadRemote = dao.notificationRows()
            .filter { it.isUnread && it.remoteBacked }
            .map { it.id }
        dao.markAllRead()
        if (unreadRemote.isEmpty()) return true
        val ref = notificationsRef() ?: return false
        return runCatching {
            val snapshot = ref.whereEqualTo("read", false).get().await()
            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.update(it.reference, "read", true) }
                batch.commit().await()
            }
            true
        }.getOrElse {
            android.util.Log.w("PeersCache", "read receipts queued", it)
            dao.markPendingRead(unreadRemote)
            false
        }
    }

    /** Returns true when the save/remove reached Firestore immediately. */
    suspend fun setSavedEvent(event: SavedEventCacheEntity, saved: Boolean): Boolean {
        if (saved) {
            dao.upsertSavedEvent(event.copy(pendingSync = true, pendingDelete = false))
        } else {
            dao.upsertSavedEvent(
                event.copy(pendingSync = true, pendingDelete = true)
            )
        }
        val ref = savedEventsRef()?.document(event.eventId) ?: return false
        return runCatching {
            if (saved) {
                ref.set(
                    mapOf(
                        "title" to event.title,
                        "organizer" to event.organizer,
                        "epochDay" to event.epochDay,
                        "time" to event.time,
                        "location" to event.location,
                        "attendees" to event.attendees,
                        "emoji" to event.emoji,
                        "savedAt" to System.currentTimeMillis(),
                    )
                ).await()
                dao.upsertSavedEvent(event.copy(pendingSync = false, pendingDelete = false))
            } else {
                ref.delete().await()
                dao.deleteSavedEvent(event.eventId)
            }
            true
        }.getOrElse {
            android.util.Log.w("PeersCache", "saved event queued", it)
            false
        }
    }

    // ── Reconnect sync ───────────────────────────────────────────────────────

    suspend fun syncPending() {
        dao.pendingWishlist().forEach { row ->
            runCatching {
                val ref = wishlistRef()?.document(row.listingId) ?: return@forEach
                if (row.pendingDelete) {
                    ref.delete().await()
                    dao.deleteWishlist(row.listingId)
                } else {
                    ref.set(mapOf("addedAt" to FieldValue.serverTimestamp())).await()
                    dao.upsertWishlist(row.copy(pendingSync = false, pendingDelete = false))
                }
            }
        }

        dao.pendingReadNotifications().forEach { row ->
            runCatching {
                val ref = notificationsRef()?.document(row.id) ?: return@forEach
                ref.update("read", true).await()
                dao.clearPendingRead(listOf(row.id))
            }
        }

        dao.pendingSavedEvents().forEach { row ->
            runCatching {
                val ref = savedEventsRef()?.document(row.eventId) ?: return@forEach
                if (row.pendingDelete) {
                    ref.delete().await()
                    dao.deleteSavedEvent(row.eventId)
                } else {
                    ref.set(
                        mapOf(
                            "title" to row.title,
                            "organizer" to row.organizer,
                            "epochDay" to row.epochDay,
                            "time" to row.time,
                            "location" to row.location,
                            "attendees" to row.attendees,
                            "emoji" to row.emoji,
                            "savedAt" to row.savedAt,
                        )
                    ).await()
                    dao.upsertSavedEvent(row.copy(pendingSync = false))
                }
            }
        }
    }

    private fun registerConnectivity() {
        runCatching {
            val manager = appContext.getSystemService(ConnectivityManager::class.java) ?: return
            manager.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        scope.launch { syncPending() }
                    }
                }
            )
        }.onFailure {
            android.util.Log.w("PeersCache", "connectivity monitoring unavailable", it)
        }
    }

    /** A small demo seed so notifications are useful before the first server sync. */
    private fun seedDemoNotifications() {
        scope.launch {
            if (dao.notificationRows().isNotEmpty()) return@launch
            val now = System.currentTimeMillis()
            dao.upsertNotifications(
                listOf(
                    NotificationCacheEntity(
                        id = "demo-1",
                        title = "Connection accepted",
                        body = "Aisha Verma accepted your connection request.",
                        emoji = "🤝",
                        timestampMillis = now - 60_000,
                        isUnread = true,
                        type = "connection_accepted",
                        remoteBacked = false,
                    ),
                    NotificationCacheEntity(
                        id = "demo-2",
                        title = "New connection request",
                        body = "Rohan Mehta wants to connect with you.",
                        emoji = "👋",
                        timestampMillis = now - 10 * 60_000,
                        isUnread = true,
                        type = "connection_request",
                        remoteBacked = false,
                    ),
                    NotificationCacheEntity(
                        id = "demo-3",
                        title = "Event near you",
                        body = "Freshers' Meetup starts in 2 days — 84 peers going.",
                        emoji = "🎉",
                        timestampMillis = now - 26 * 60 * 60_000,
                        isUnread = false,
                        type = "info",
                        remoteBacked = false,
                    ),
                )
            )
        }
    }

    private fun RemoteNotification.toEntity(
        isUnread: Boolean = this.isUnread,
        remoteBacked: Boolean = true,
        pendingRead: Boolean = false,
    ): NotificationCacheEntity = NotificationCacheEntity(
        id = id,
        title = title,
        body = body,
        emoji = emoji,
        timestampMillis = timestampMillis,
        isUnread = isUnread,
        type = type,
        remoteBacked = remoteBacked,
        pendingRead = pendingRead,
    )

    companion object {
        @Volatile
        private var instance: OfflineCacheRepository? = null

        fun get(context: Context): OfflineCacheRepository =
            instance ?: synchronized(this) {
                instance ?: OfflineCacheRepository(context).also { instance = it }
            }
    }
}
