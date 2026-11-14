package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.Movie
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Public read-only movie repository for the Naa Movies World User App.
 * Reads only published movies without requiring authentication or containing admin logic.
 */
class MovieRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observePublishedMovies(): Flow<List<Movie>> {
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
                if (toLog != null) {
                    handleFirestoreError(toLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    companion object {
        const val COLLECTION_MOVIES = "movies"
    }
}
