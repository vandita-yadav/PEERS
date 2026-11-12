package com.example.peers.features.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WishlistCacheEntity::class,
        NotificationCacheEntity::class,
        SavedEventCacheEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OfflineCacheDatabase : RoomDatabase() {
    abstract fun cacheDao(): OfflineCacheDao

    companion object {
        @Volatile
        private var instance: OfflineCacheDatabase? = null

        fun get(context: Context): OfflineCacheDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OfflineCacheDatabase::class.java,
                    "peers-cache.db",
                ).build().also { instance = it }
            }
    }
}
