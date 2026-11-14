package com.example.admin.data.model

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

enum class AdminCatalogFilter(val label: String) {
    ALL("All"),
    PUBLISHED("Published"),
    DRAFTS("Unpublished"),
    TRENDING("Trending"),
    LATEST("Latest")
}

@IgnoreExtraProperties
data class Movie(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val language: String = "Telugu",
    val category: String = "Action",
    val genres: List<String> = listOf("Action"),
    val releaseYear: Long = 2026L,
    val durationMinutes: Long = 135L,
    val rating: Double = 8.6,
    val qualityBadge: String = "4K UHD",
    val posterUrl: String = "",
    val posterKey: String = "telugu",
    val streamUrl: String = "",
    val trending: Boolean = false,
    val latest: Boolean = false,
    val featured: Boolean = false,
    val licenseConfirmed: Boolean = true,
    val licenseNote: String = "Licensed Studio Distribution",
    val status: String = "published",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
