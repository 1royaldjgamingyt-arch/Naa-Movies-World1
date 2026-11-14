package com.example.data.repository

import com.example.data.local.LocalWatchHistoryEntity
import com.example.data.local.LocalWatchlistEntity
import com.example.data.local.WatchlistDao
import com.example.data.model.Movie
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalWatchlistRepository(
    private val dao: WatchlistDao
) {
    fun observeWatchlist(): Flow<List<WatchlistItem>> {
        return dao.observeWatchlist().map { entities ->
            entities.map { entity ->
                WatchlistItem(
                    movieId = entity.movieId,
                    title = entity.title,
                    description = entity.description,
                    language = entity.language,
                    category = entity.category,
                    releaseYear = entity.releaseYear,
                    durationMinutes = entity.durationMinutes,
                    rating = entity.rating,
                    qualityBadge = entity.qualityBadge,
                    posterUrl = entity.posterUrl,
                    posterKey = entity.posterKey,
                    streamUrl = entity.streamUrl,
                    licenseNote = entity.licenseNote,
                    addedAtMs = entity.addedAtMs
                )
            }
        }
    }

    suspend fun addToWatchlist(movie: Movie): Result<Unit> {
        return try {
            val movieId = movie.id.ifBlank {
                movie.title.lowercase().replace(Regex("[^a-z0-9]+"), "_")
            }
            dao.insertWatchlist(
                LocalWatchlistEntity(
                    movieId = movieId,
                    title = movie.title,
                    description = movie.description,
                    language = movie.language,
                    category = movie.category,
                    releaseYear = movie.releaseYear,
                    durationMinutes = movie.durationMinutes,
                    rating = movie.rating,
                    qualityBadge = movie.qualityBadge,
                    posterUrl = movie.posterUrl,
                    posterKey = movie.posterKey.ifBlank { movie.language.lowercase() },
                    streamUrl = movie.streamUrl,
                    licenseNote = movie.licenseNote,
                    addedAtMs = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromWatchlist(movieId: String): Result<Unit> {
        return try {
            dao.deleteFromWatchlist(movieId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeWatchHistory(): Flow<List<WatchHistoryItem>> {
        return dao.observeWatchHistory().map { entities ->
            entities.map { entity ->
                WatchHistoryItem(
                    movieId = entity.movieId,
                    title = entity.title,
                    posterUrl = entity.posterUrl,
                    posterKey = entity.posterKey,
                    positionMs = entity.positionMs,
                    durationMs = entity.durationMs,
                    updatedAtMs = entity.updatedAtMs
                )
            }
        }
    }

    suspend fun saveWatchProgress(movie: Movie, positionMs: Long, durationMs: Long): Result<Unit> {
        val movieId = movie.id.ifBlank { return Result.success(Unit) }
        return try {
            dao.upsertWatchHistory(
                LocalWatchHistoryEntity(
                    movieId = movieId,
                    title = movie.title,
                    posterUrl = movie.posterUrl,
                    posterKey = movie.posterKey.ifBlank { movie.language.lowercase() },
                    positionMs = positionMs.coerceAtLeast(0L),
                    durationMs = durationMs.coerceAtLeast(0L),
                    updatedAtMs = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
