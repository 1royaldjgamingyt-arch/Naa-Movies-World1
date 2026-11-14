package com.example.admin.data.repository

import android.content.Context
import com.example.admin.R
import com.example.admin.data.OperationType
import com.example.admin.data.handleFirestoreError
import com.example.admin.data.model.Movie
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdminMovieRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Admin must be signed in before modifying catalog.")
    }

    suspend fun checkIsAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        if (user.isEmailVerified && user.email.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)) {
            return true
        }
        return try {
            val doc = db.collection(COLLECTION_ADMINS).document(user.uid).get().await()
            doc.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    fun observeAllMovies(): Flow<List<Movie>> {
        val path = COLLECTION_MOVIES
        return db.collection(COLLECTION_MOVIES)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    Movie::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val toLog = firestoreError ?: (error as? Exception)
                if (toLog != null && auth.currentUser != null) {
                    handleFirestoreError(toLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observePublishedMoviesFallback(): Flow<List<Movie>> {
        val path = COLLECTION_MOVIES
        return db.collection(COLLECTION_MOVIES)
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    Movie::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val toLog = firestoreError ?: (error as? Exception)
                if (toLog != null && auth.currentUser != null) {
                    handleFirestoreError(toLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun saveMovie(movie: Movie, customId: String? = null): Result<String> {
        val uid = requireUserId()
        val movieId = customId?.takeIf { it.isNotBlank() }
            ?: movie.id.takeIf { it.isNotBlank() }
            ?: "movie_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection(COLLECTION_MOVIES).document(movieId)

        val isExisting = movie.id.isNotBlank() && movie.createdAt != null
        val payload = hashMapOf<String, Any>(
            "title" to movie.title.trim().take(120),
            "description" to movie.description.trim().take(1200),
            "language" to movie.language,
            "category" to movie.category,
            "genres" to movie.genres.ifEmpty { listOf(movie.category) }.take(5),
            "releaseYear" to movie.releaseYear,
            "durationMinutes" to movie.durationMinutes,
            "rating" to movie.rating,
            "qualityBadge" to movie.qualityBadge,
            "posterUrl" to movie.posterUrl.trim().take(250000),
            "posterKey" to movie.posterKey.ifBlank { movie.language.lowercase() }.take(80),
            "streamUrl" to movie.streamUrl.trim().take(1000),
            "trending" to movie.trending,
            "latest" to movie.latest,
            "featured" to movie.featured,
            "licenseConfirmed" to movie.licenseConfirmed,
            "licenseNote" to movie.licenseNote.trim().ifBlank { "Licensed Studio Distribution" }.take(300),
            "status" to movie.status,
            "createdBy" to (if (isExisting && movie.createdBy.isNotBlank()) movie.createdBy else uid),
            "createdAt" to (movie.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Result.success(movieId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isExisting) OperationType.UPDATE else OperationType.CREATE,
                docRef.path
            )
            Result.failure(e)
        }
    }

    suspend fun updateQuickFlags(
        movie: Movie,
        status: String = movie.status,
        trending: Boolean = movie.trending,
        latest: Boolean = movie.latest,
        featured: Boolean = movie.featured,
        category: String = movie.category
    ): Result<Unit> {
        requireUserId()
        require(movie.id.isNotBlank()) { "Movie ID must not be blank for quick update" }
        val updatedGenres = if (movie.genres.contains(category)) {
            movie.genres
        } else {
            (listOf(category) + movie.genres).distinct().take(5)
        }
        val docRef = db.collection(COLLECTION_MOVIES).document(movie.id)
        val updates = hashMapOf<String, Any>(
            "status" to status,
            "trending" to trending,
            "latest" to latest,
            "featured" to featured,
            "category" to category,
            "genres" to updatedGenres,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMovie(movieId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection(COLLECTION_MOVIES).document(movieId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    companion object {
        const val COLLECTION_MOVIES = "movies"
        const val COLLECTION_ADMINS = "admins"
        const val BOOTSTRAP_ADMIN_EMAIL = "1royaldjgamingyt@gmail.com"
    }
}
