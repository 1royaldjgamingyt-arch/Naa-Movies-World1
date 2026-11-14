package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class MovieLanguage(val label: String) {
    ALL("All"),
    TELUGU("Telugu"),
    TAMIL("Tamil"),
    HINDI("Hindi"),
    ENGLISH("English");

    companion object {
        val uploadLanguages = listOf(TELUGU, TAMIL, HINDI, ENGLISH)
    }
}

enum class MovieCategory(val label: String) {
    ALL("All"),
    ACTION("Action"),
    THRILLER("Thriller"),
    DRAMA("Drama"),
    SCI_FI("Sci-Fi"),
    ROMANCE("Romance"),
    COMEDY("Comedy"),
    ADVENTURE("Adventure"),
    DOCUMENTARY("Documentary");

    companion object {
        val uploadCategories = listOf(ACTION, THRILLER, DRAMA, SCI_FI, ROMANCE, COMEDY, ADVENTURE, DOCUMENTARY)
    }
}

enum class QualityTier(val label: String) {
    UHD_4K("4K UHD"),
    HD_1080P("1080p HD"),
    HD_720P("720p HD");

    companion object {
        val all = listOf(UHD_4K, HD_1080P, HD_720P)
    }
}

@IgnoreExtraProperties
data class CastCredit(
    val name: String = "",
    val role: String = ""
)

@IgnoreExtraProperties
data class Movie(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val language: String = "Telugu",
    val category: String = "Action",
    val genres: List<String> = listOf("Action"),
    val releaseYear: Long = 2025L,
    val durationMinutes: Long = 120L,
    val rating: Double = 8.5,
    val qualityBadge: String = "4K UHD",
    val posterUrl: String = "",
    val posterKey: String = "telugu",
    val streamUrl: String = "",
    val director: String = "",
    val cast: List<CastCredit> = emptyList(),
    val subtitleLanguages: List<String> = listOf("English"),
    val trending: Boolean = false,
    val latest: Boolean = false,
    val featured: Boolean = false,
    val licenseConfirmed: Boolean = true,
    val licenseNote: String = "Creative Commons / Licensed Studio Release",
    val status: String = "published",
    val createdBy: String = "catalog",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun availableQualityBadges(): List<String> = when {
        qualityBadge.contains("4K", ignoreCase = true) -> listOf("4K UHD", "1080p HD", "720p HD")
        qualityBadge.contains("1080", ignoreCase = true) -> listOf("1080p HD", "720p HD")
        else -> listOf("720p HD")
    }

    fun resolvedCastAndCrew(): List<CastCredit> {
        if (cast.isNotEmpty()) {
            return if (director.isNotBlank() && cast.none { it.role.equals("Director", ignoreCase = true) }) {
                listOf(CastCredit(director, "Director")) + cast
            } else {
                cast
            }
        }
        return buildList {
            if (director.isNotBlank()) {
                add(CastCredit(director, "Director"))
            } else {
                add(CastCredit("$language Cinema Studio", "Presentation"))
            }
            add(CastCredit(licenseNote.ifBlank { "Authorized Cinema Unit" }, "Distribution"))
            add(CastCredit("$category Ensemble", "Featured Cast"))
            add(CastCredit("$language Audio Mastering", "Sound & Score"))
        }
    }
}

data class PopularCollection(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val filterLanguage: MovieLanguage = MovieLanguage.ALL,
    val filterCategory: MovieCategory = MovieCategory.ALL,
    val movies: List<Movie>
)

data class WatchlistItem(
    val movieId: String = "",
    val title: String = "",
    val description: String = "",
    val language: String = "Telugu",
    val category: String = "Action",
    val releaseYear: Long = 2025L,
    val durationMinutes: Long = 120L,
    val rating: Double = 8.0,
    val qualityBadge: String = "1080p HD",
    val posterUrl: String = "",
    val posterKey: String = "telugu",
    val streamUrl: String = "",
    val licenseNote: String = "",
    val addedAtMs: Long = 0L
)

fun WatchlistItem.toMovie(): Movie = Movie(
    id = movieId,
    title = title,
    description = description.ifBlank {
        "Saved in your Naa Movies World Watchlist. Ready for instant HD playback."
    },
    language = language,
    category = category,
    genres = listOf(category),
    releaseYear = releaseYear,
    durationMinutes = durationMinutes,
    rating = rating,
    qualityBadge = qualityBadge,
    posterUrl = posterUrl,
    posterKey = posterKey,
    streamUrl = streamUrl,
    licenseConfirmed = true,
    licenseNote = licenseNote.ifBlank { "Creative Commons / Licensed Studio Release" }
)

data class WatchHistoryItem(
    val movieId: String = "",
    val title: String = "",
    val posterUrl: String = "",
    val posterKey: String = "telugu",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val updatedAtMs: Long = 0L
)
