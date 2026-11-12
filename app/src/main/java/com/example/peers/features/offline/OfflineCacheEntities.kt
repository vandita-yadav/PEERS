package com.example.peers.features.offline

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Offline mirror of server-backed user state. Rows flagged `pending*` were changed
 * locally while the network was unavailable and still need to be pushed.
 */
@Entity(tableName = "wishlist_cache")
data class WishlistCacheEntity(
    @PrimaryKey val listingId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val pendingSync: Boolean = false,
    val pendingDelete: Boolean = false,
)

@Entity(tableName = "notification_cache")
data class NotificationCacheEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val emoji: String,
    val timestampMillis: Long,
    val isUnread: Boolean,
    val type: String,
    val remoteBacked: Boolean = true,
    val pendingRead: Boolean = false,
)

@Entity(tableName = "saved_event_cache")
data class SavedEventCacheEntity(
    @PrimaryKey val eventId: String,
    val title: String,
    val organizer: String,
    val epochDay: Long,
    val time: String,
    val location: String,
    val attendees: Int,
    val emoji: String,
    val savedAt: Long = System.currentTimeMillis(),
    val pendingSync: Boolean = false,
    val pendingDelete: Boolean = false,
)

/** A notification snapshot as read from Firestore. */
data class RemoteNotification(
    val id: String,
    val title: String,
    val body: String,
    val emoji: String,
    val timestampMillis: Long,
    val isUnread: Boolean,
    val type: String,
)
