package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "watchlist")
data class LocalWatchlistEntity(
    @PrimaryKey val movieId: String,
    val title: String,
    val description: String,
    val language: String,
    val category: String,
    val releaseYear: Long,
    val durationMinutes: Long,
    val rating: Double,
    val qualityBadge: String,
    val posterUrl: String,
    val posterKey: String,
    val streamUrl: String,
    val licenseNote: String,
    val addedAtMs: Long
)

@Entity(tableName = "watch_history")
data class LocalWatchHistoryEntity(
    @PrimaryKey val movieId: String,
    val title: String,
    val posterUrl: String,
    val posterKey: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAtMs: Long
)

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAtMs DESC")
    fun observeWatchlist(): Flow<List<LocalWatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: LocalWatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun deleteFromWatchlist(movieId: String)

    @Query("SELECT * FROM watch_history ORDER BY updatedAtMs DESC")
    fun observeWatchHistory(): Flow<List<LocalWatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(item: LocalWatchHistoryEntity)
}

@Database(
    entities = [LocalWatchlistEntity::class, LocalWatchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WatchlistDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile
        private var INSTANCE: WatchlistDatabase? = null

        fun getInstance(context: Context): WatchlistDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WatchlistDatabase::class.java,
                    "naa_movies_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
