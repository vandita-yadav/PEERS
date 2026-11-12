package com.example.peers.features.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineCacheDao {

    // ── Wishlist ─────────────────────────────────────────────────────────────

    @Query("SELECT listingId FROM wishlist_cache WHERE pendingDelete = 0 ORDER BY addedAt DESC")
    fun observeWishlistIds(): Flow<List<String>>

    @Query("SELECT * FROM wishlist_cache")
    suspend fun wishlistRows(): List<WishlistCacheEntity>

    @Query("SELECT * FROM wishlist_cache WHERE pendingSync = 1")
    suspend fun pendingWishlist(): List<WishlistCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWishlist(row: WishlistCacheEntity)

    @Query("DELETE FROM wishlist_cache WHERE listingId = :listingId")
    suspend fun deleteWishlist(listingId: String)

    @Query("DELETE FROM wishlist_cache WHERE pendingSync = 0")
    suspend fun clearSyncedWishlist()

    // ── Notifications ────────────────────────────────────────────────────────

    @Query("SELECT * FROM notification_cache ORDER BY timestampMillis DESC")
    fun observeNotifications(): Flow<List<NotificationCacheEntity>>

    @Query("SELECT * FROM notification_cache")
    suspend fun notificationRows(): List<NotificationCacheEntity>

    @Query("SELECT * FROM notification_cache WHERE id = :id")
    suspend fun notification(id: String): NotificationCacheEntity?

    @Query("SELECT * FROM notification_cache WHERE pendingRead = 1")
    suspend fun pendingReadNotifications(): List<NotificationCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotification(row: NotificationCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotifications(rows: List<NotificationCacheEntity>)

    @Query("DELETE FROM notification_cache WHERE pendingRead = 0")
    suspend fun clearSyncedNotifications()

    @Query("UPDATE notification_cache SET isUnread = 0")
    suspend fun markAllRead()

    @Query("UPDATE notification_cache SET pendingRead = 1 WHERE id IN (:ids)")
    suspend fun markPendingRead(ids: List<String>)

    @Query("UPDATE notification_cache SET pendingRead = 0 WHERE id IN (:ids)")
    suspend fun clearPendingRead(ids: List<String>)

    // ── Saved events ─────────────────────────────────────────────────────────

    @Query("SELECT eventId FROM saved_event_cache WHERE pendingDelete = 0")
    fun observeSavedEventIds(): Flow<List<String>>

    @Query("SELECT * FROM saved_event_cache WHERE pendingDelete = 0 ORDER BY epochDay ASC")
    fun observeSavedEvents(): Flow<List<SavedEventCacheEntity>>

    @Query("SELECT * FROM saved_event_cache")
    suspend fun savedEventRows(): List<SavedEventCacheEntity>

    @Query("SELECT * FROM saved_event_cache WHERE pendingSync = 1")
    suspend fun pendingSavedEvents(): List<SavedEventCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedEvent(row: SavedEventCacheEntity)

    @Query("DELETE FROM saved_event_cache WHERE eventId = :eventId")
    suspend fun deleteSavedEvent(eventId: String)

    @Query("DELETE FROM saved_event_cache WHERE pendingSync = 0")
    suspend fun clearSyncedSavedEvents()
}
