package com.example.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
abstract class FirestoreEmulatorTestBase {

    protected lateinit var firestore: FirebaseFirestore
    protected lateinit var databaseId: String

    @Before
    open fun setUpFirebase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
        databaseId = if (resId != 0) {
            context.getString(resId)
        } else {
            "ai-studio-android-naamovie-c061f3a1-2b0a-4254-a182-5ba9aea6cea5"
        }

        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            val newApp = FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.naamovies.wqxzkp")
                    .setProjectId(PROJECT_ID)
                    .setApiKey("fake-api-key-for-emulator")
                    .build()
            )
            val newFirestore = FirebaseFirestore.getInstance(newApp, databaseId)
            newFirestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
            newFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
            newApp
        } else {
            FirebaseApp.getInstance()
        }

        firestore = FirebaseFirestore.getInstance(app, databaseId)
    }

    protected suspend fun seedMovieAsEmulatorAdmin(docId: String, status: String) = withContext(Dispatchers.IO) {
        val url = URL(
            "http://$EMULATOR_HOST:$FIRESTORE_PORT/v1/projects/$PROJECT_ID/databases/$databaseId/documents/movies?documentId=$docId"
        )
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 5000
            readTimeout = 5000
            doOutput = true
            setRequestProperty("Authorization", "Bearer owner")
            setRequestProperty("Content-Type", "application/json")
        }
        val body = """
            {
              "fields": {
                "title": { "stringValue": "Devara Rising" },
                "description": { "stringValue": "Epic coastal action drama." },
                "language": { "stringValue": "Telugu" },
                "category": { "stringValue": "Action" },
                "genres": { "arrayValue": { "values": [{ "stringValue": "Action" }, { "stringValue": "Drama" }] } },
                "releaseYear": { "integerValue": "2026" },
                "durationMinutes": { "integerValue": "148" },
                "rating": { "doubleValue": 8.9 },
                "qualityBadge": { "stringValue": "4K UHD" },
                "posterUrl": { "stringValue": "" },
                "posterKey": { "stringValue": "telugu" },
                "streamUrl": { "stringValue": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4" },
                "trending": { "booleanValue": true },
                "latest": { "booleanValue": true },
                "featured": { "booleanValue": true },
                "licenseConfirmed": { "booleanValue": true },
                "licenseNote": { "stringValue": "Licensed Open Film" },
                "status": { "stringValue": "$status" },
                "createdBy": { "stringValue": "admin_seed_uid" },
                "createdAt": { "timestampValue": "2026-01-01T00:00:00Z" },
                "updatedAt": { "timestampValue": "2026-01-01T00:00:00Z" }
              }
            }
        """.trimIndent()
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val err = if (code >= 400) conn.errorStream?.bufferedReader()?.readText().orEmpty() else ""
        conn.disconnect()
        check(code in 200..299) { "Failed to seed emulator doc $docId ($code): $err" }
    }

    protected fun Throwable.unwrapFirestoreException(): FirebaseFirestoreException? {
        var current: Throwable? = this
        while (current != null) {
            if (current is FirebaseFirestoreException) return current
            current = current.cause
        }
        return null
    }

    private companion object {
        const val EMULATOR_HOST = "127.0.0.1"
        const val FIRESTORE_PORT = 8085
        const val PROJECT_ID = "demo-no-project"
    }
}
