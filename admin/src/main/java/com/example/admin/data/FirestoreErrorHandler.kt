package com.example.admin.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class OperationType {
    CREATE, UPDATE, DELETE, LIST, GET, WRITE
}

@Serializable
data class FirestoreErrorInfo(
    val error: String,
    val operationType: String,
    val path: String?,
    val userId: String?,
    val emailVerified: Boolean?,
    val providerIds: List<String>
)

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String?
) {
    val currentUser = try {
        Firebase.auth.currentUser
    } catch (_: IllegalStateException) {
        null
    }

    val errorInfo = FirestoreErrorInfo(
        error = exception.message ?: exception.toString(),
        operationType = operationType.name.lowercase(),
        path = path,
        userId = currentUser?.uid,
        emailVerified = currentUser?.isEmailVerified,
        providerIds = currentUser?.providerData?.map { it.providerId } ?: emptyList()
    )

    val jsonString = Json.encodeToString(errorInfo)
    Log.e("FirestoreError", "Admin Firestore Error: $jsonString", exception)
}
