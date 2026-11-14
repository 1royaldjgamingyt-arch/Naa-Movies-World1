package com.example.admin.data.repository

import com.example.admin.base.AdminFirestoreEmulatorTestBase
import com.example.admin.data.model.Movie
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class AdminMovieRepositoryRuleTest : AdminFirestoreEmulatorTestBase() {

    @Test
    fun verifiedAdmin_canCreateUpdatePublishToggleAndDeleteMovie() = runBlocking {
        signInAsVerifiedBootstrapAdmin()
        val repo = AdminMovieRepository(firestore, auth)

        assertTrue(repo.checkIsAdmin())

        val docId = "admin_movie_${UUID.randomUUID().toString().take(8)}"
        val draftMovie = Movie(
            title = "Kalki Studio Cut",
            description = "Licensed sci-fi action featurette.",
            language = "Telugu",
            category = "Sci-Fi",
            genres = listOf("Sci-Fi", "Action"),
            releaseYear = 2026L,
            durationMinutes = 142L,
            rating = 9.0,
            qualityBadge = "4K UHD",
            posterUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
            posterKey = "telugu",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            trending = false,
            latest = true,
            featured = false,
            licenseConfirmed = true,
            licenseNote = "Licensed Studio Distribution",
            status = "draft"
        )

        val saveRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveMovie(draftMovie, customId = docId)
        }
        assertTrue(saveRes.isSuccess)

        val moviesWithDraft = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAllMovies().first { list -> list.any { it.id == docId } }
        }
        val fetched = moviesWithDraft.first { it.id == docId }
        assertEquals("draft", fetched.status)
        assertEquals("Sci-Fi", fetched.category)

        // Toggle publish + trending + category
        val updateRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.updateQuickFlags(
                movie = fetched,
                status = "published",
                trending = true,
                latest = true,
                category = "Action"
            )
        }
        assertTrue(updateRes.isSuccess)

        val updated = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAllMovies().first { list ->
                list.any { it.id == docId && it.status == "published" && it.trending && it.category == "Action" }
            }
        }
        assertTrue(updated.any { it.id == docId })

        val deleteRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.deleteMovie(docId)
        }
        assertTrue(deleteRes.isSuccess)

        val afterDelete = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAllMovies().first { list -> list.none { it.id == docId } }
        }
        assertFalse(afterDelete.any { it.id == docId })
    }

    @Test
    fun nonAdminUser_cannotCreateOrModifyCatalog() = runBlocking {
        signInTestUser("viewer_user@example.com")
        val repo = AdminMovieRepository(firestore, auth)

        assertFalse(repo.checkIsAdmin())

        val unauthorizedAttempt = Movie(
            title = "Unauthorized Film",
            description = "Should be denied by rules.",
            language = "Hindi",
            category = "Action",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            licenseConfirmed = true,
            status = "published"
        )
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveMovie(unauthorizedAttempt, customId = "unauth_${UUID.randomUUID().toString().take(8)}")
        }
        assertTrue(result.isFailure)
        val fsError = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError!!.code)
    }

    private suspend fun signInAsVerifiedBootstrapAdmin(): String {
        signInTestUser(ADMIN_EMAIL)
        val user = checkNotNull(auth.currentUser)
        if (!user.isEmailVerified) {
            user.sendEmailVerification().await()
            withContext(Dispatchers.IO) {
                val conn = URL("http://127.0.0.1:9099/emulator/v1/projects/demo-no-project/oobCodes")
                    .openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                val json = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                val codes = JSONObject(json).getJSONArray("oobCodes")
                var foundCode: String? = null
                for (i in codes.length() - 1 downTo 0) {
                    val entry = codes.getJSONObject(i)
                    if (entry.optString("email").equals(ADMIN_EMAIL, ignoreCase = true)) {
                        foundCode = entry.getString("oobCode")
                        break
                    }
                }
                val oobCode = checkNotNull(foundCode) { "No OOB code found for $ADMIN_EMAIL" }
                auth.applyActionCode(oobCode).await()
                user.reload().await()
                user.getIdToken(true).await()
            }
            auth.signOut()
            signInTestUser(ADMIN_EMAIL)
        }
        return checkNotNull(auth.currentUser?.uid)
    }

    private companion object {
        const val ADMIN_EMAIL = "1royaldjgamingyt@gmail.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
