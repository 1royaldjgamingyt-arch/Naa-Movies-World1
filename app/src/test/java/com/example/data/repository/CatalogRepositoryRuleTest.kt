package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.WatchlistDatabase
import com.example.data.model.Movie
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CatalogRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun movies_unauthenticatedUser_canObservePublishedMoviesWithoutLogin() = runBlocking {
        val pubId = "pub_${UUID.randomUUID().toString().take(8)}"
        val draftId = "draft_${UUID.randomUUID().toString().take(8)}"

        seedMovieAsEmulatorAdmin(pubId, "published")
        seedMovieAsEmulatorAdmin(draftId, "draft")

        // User App runs with ZERO authentication
        val movieRepo = MovieRepository(firestore)
        val published = withTimeout(FLOW_TIMEOUT_MS) {
            movieRepo.observePublishedMovies().first { list -> list.any { it.id == pubId } }
        }
        assertTrue(published.any { it.id == pubId })
        assertFalse(published.any { it.id == draftId })
    }

    @Test
    fun movies_unauthenticatedUser_cannotReadDraftOrWriteCatalog() = runBlocking {
        val draftId = "draft_priv_${UUID.randomUUID().toString().take(8)}"
        seedMovieAsEmulatorAdmin(draftId, "draft")

        var readDraftError: Exception? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("movies").document(draftId).get().await()
            }
        } catch (e: Exception) {
            readDraftError = e
        }
        assertNotNull(readDraftError)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            readDraftError!!.unwrapFirestoreException()?.code
        )

        var writeError: Exception? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("movies").document("any_write_check").set(
                    hashMapOf(
                        "title" to "Unauthorized",
                        "description" to "Should fail",
                        "language" to "Telugu",
                        "category" to "Action",
                        "genres" to listOf("Action"),
                        "releaseYear" to 2026L,
                        "durationMinutes" to 120L,
                        "rating" to 8.0,
                        "qualityBadge" to "4K UHD",
                        "posterUrl" to "",
                        "posterKey" to "telugu",
                        "streamUrl" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                        "trending" to false,
                        "latest" to true,
                        "featured" to false,
                        "licenseConfirmed" to true,
                        "licenseNote" to "None",
                        "status" to "published",
                        "createdBy" to "anon",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            writeError = e
        }
        assertNotNull(writeError)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            writeError!!.unwrapFirestoreException()?.code
        )
    }

    @Test
    fun localWatchlist_addObserveAndRemove_persistsOnDevice() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, WatchlistDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = LocalWatchlistRepository(db.watchlistDao())
            val movie = Movie(
                id = "local_1",
                title = "Devara Rising",
                description = "Coastal action epic",
                language = "Telugu",
                category = "Action",
                releaseYear = 2026L,
                durationMinutes = 148L,
                rating = 8.9,
                qualityBadge = "4K UHD",
                posterUrl = "",
                posterKey = "telugu",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
            )
            assertTrue(repo.addToWatchlist(movie).isSuccess)
            val saved = withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeWatchlist().first { it.any { item -> item.movieId == "local_1" } }
            }
            assertEquals("Devara Rising", saved.first().title)

            assertTrue(repo.saveWatchProgress(movie, 35_000L, 120_000L).isSuccess)
            val history = withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeWatchHistory().first { it.any { item -> item.movieId == "local_1" } }
            }
            assertEquals(35_000L, history.first().positionMs)

            assertTrue(repo.removeFromWatchlist("local_1").isSuccess)
            val afterDelete = withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeWatchlist().first { items -> items.none { it.movieId == "local_1" } }
            }
            assertTrue(afterDelete.none { it.movieId == "local_1" })
        } finally {
            db.close()
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
