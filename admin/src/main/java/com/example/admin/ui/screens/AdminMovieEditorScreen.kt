package com.example.admin.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.admin.data.model.Movie
import com.example.admin.data.model.MovieCategory
import com.example.admin.data.model.MovieLanguage
import com.example.admin.data.model.QualityTier
import com.example.admin.ui.components.AdminPosterImage
import com.example.admin.ui.theme.CinemaCrimson
import com.example.admin.ui.theme.CinemaGold
import com.example.admin.ui.theme.EmeraldVerified
import com.example.admin.ui.theme.ObsidianBlack
import com.example.admin.ui.theme.ObsidianBorder
import com.example.admin.ui.theme.ObsidianCard
import com.example.admin.ui.theme.ObsidianElevated
import com.example.admin.ui.theme.TextPrimary
import com.example.admin.ui.theme.TextSecondary

private val AuthorizedSampleStreams = listOf(
    "Tears of Steel (4K Sample)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
    "Big Buck Bunny (HD)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    "Sintel (Cinema MP4)" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
)

@Composable
fun AdminMovieEditorScreen(
    existingMovie: Movie?,
    isSaving: Boolean,
    isEncodingPoster: Boolean,
    onPickPosterUri: (Uri, (String) -> Unit) -> Unit,
    onPreviewStreamUrl: (title: String, language: String, quality: String, url: String) -> Unit,
    onSaveMovie: (Movie) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = existingMovie != null
    var title by remember(existingMovie?.id) { mutableStateOf(existingMovie?.title.orEmpty()) }
    var description by remember(existingMovie?.id) { mutableStateOf(existingMovie?.description.orEmpty()) }
    var selectedLanguage by remember(existingMovie?.id) { mutableStateOf(existingMovie?.language ?: "Telugu") }
    var selectedCategory by remember(existingMovie?.id) { mutableStateOf(existingMovie?.category ?: "Action") }
    var selectedGenres by remember(existingMovie?.id) {
        mutableStateOf(existingMovie?.genres?.ifEmpty { listOf("Action") } ?: listOf("Action"))
    }
    var releaseYearText by remember(existingMovie?.id) { mutableStateOf((existingMovie?.releaseYear ?: 2026L).toString()) }
    var durationText by remember(existingMovie?.id) { mutableStateOf((existingMovie?.durationMinutes ?: 140L).toString()) }
    var ratingText by remember(existingMovie?.id) { mutableStateOf((existingMovie?.rating ?: 8.5).toString()) }
    var qualityBadge by remember(existingMovie?.id) { mutableStateOf(existingMovie?.qualityBadge ?: QualityTier.UHD_4K.label) }
    var posterUrl by remember(existingMovie?.id) { mutableStateOf(existingMovie?.posterUrl.orEmpty()) }
    var posterKey by remember(existingMovie?.id) { mutableStateOf(existingMovie?.posterKey ?: "telugu") }
    var streamUrl by remember(existingMovie?.id) { mutableStateOf(existingMovie?.streamUrl.orEmpty()) }
    var trending by remember(existingMovie?.id) { mutableStateOf(existingMovie?.trending ?: false) }
    var latest by remember(existingMovie?.id) { mutableStateOf(existingMovie?.latest ?: true) }
    var featured by remember(existingMovie?.id) { mutableStateOf(existingMovie?.featured ?: false) }
    var published by remember(existingMovie?.id) { mutableStateOf((existingMovie?.status ?: "published") == "published") }
    var licenseConfirmed by remember(existingMovie?.id) { mutableStateOf(existingMovie?.licenseConfirmed ?: false) }
    var licenseNote by remember(existingMovie?.id) { mutableStateOf(existingMovie?.licenseNote ?: "Authorized Studio Distribution") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickPosterUri(uri) { encodedDataUri ->
                posterUrl = encodedDataUri
            }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = ObsidianCard,
        unfocusedContainerColor = ObsidianCard,
        focusedBorderColor = CinemaGold,
        unfocusedBorderColor = ObsidianBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("editor_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Column {
                Text(
                    text = if (isEditing) "EDIT STUDIO TITLE" else "UPLOAD NEW MOVIE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold
                )
                Text(
                    text = if (isEditing) existingMovie.title else "Publish Licensed Film",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .widthIn(max = 640.dp)
                .navigationBarsPadding()
        ) {
            // Poster Upload Card
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(92.dp)
                            .aspectRatio(0.68f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianElevated)
                    ) {
                        AdminPosterImage(
                            posterUrl = posterUrl,
                            posterKey = posterKey,
                            language = selectedLanguage,
                            contentDescription = "Movie poster preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Movie Poster Artwork",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (posterUrl.startsWith("data:image/")) {
                                "Uploaded from device gallery"
                            } else if (posterUrl.isNotBlank()) {
                                "Using external HTTPS artwork URL"
                            } else {
                                "Select from gallery or enter HTTPS URL"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isEncodingPoster,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaGold,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("pick_poster_button")
                        ) {
                            if (isEncodingPoster) {
                                CircularProgressIndicator(
                                    color = ObsidianBlack,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Optimizing…")
                            } else {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Poster", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; validationError = null },
                label = { Text("Movie Title *") },
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; validationError = null },
                label = { Text("Synopsis / Description *") },
                minLines = 3,
                maxLines = 5,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_description_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Language *", style = MaterialTheme.typography.labelLarge, color = CinemaGold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                MovieLanguage.uploadLanguages.forEach { lang ->
                    val selected = selectedLanguage == lang.label
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedLanguage = lang.label
                            if (posterUrl.isBlank()) posterKey = lang.label.lowercase()
                        },
                        label = { Text(lang.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = ObsidianCard,
                            labelColor = TextSecondary,
                            selectedContainerColor = CinemaCrimson,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Primary Category *", style = MaterialTheme.typography.labelLarge, color = CinemaGold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                MovieCategory.uploadCategories.forEach { cat ->
                    val selected = selectedCategory == cat.label
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedCategory = cat.label
                            if (!selectedGenres.contains(cat.label)) {
                                selectedGenres = (listOf(cat.label) + selectedGenres).take(4)
                            }
                        },
                        label = { Text(cat.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = ObsidianCard,
                            labelColor = TextSecondary,
                            selectedContainerColor = CinemaCrimson,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Genres (up to 5)", style = MaterialTheme.typography.labelLarge, color = CinemaGold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                MovieCategory.uploadCategories.forEach { cat ->
                    val selected = selectedGenres.contains(cat.label)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedGenres = if (selected && selectedGenres.size > 1) {
                                selectedGenres - cat.label
                            } else if (!selected) {
                                (selectedGenres + cat.label).take(5)
                            } else {
                                selectedGenres
                            }
                        },
                        label = { Text(cat.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = ObsidianCard,
                            labelColor = TextSecondary,
                            selectedContainerColor = CinemaGold.copy(alpha = 0.2f),
                            selectedLabelColor = CinemaGold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Year, Duration, Rating row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = releaseYearText,
                    onValueChange = { releaseYearText = it.filter(Char::isDigit).take(4) },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter(Char::isDigit).take(3) },
                    label = { Text("Mins") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ratingText,
                    onValueChange = { ratingText = it.take(4) },
                    label = { Text("Rating") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Quality Tier", style = MaterialTheme.typography.labelLarge, color = CinemaGold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QualityTier.all.forEach { tier ->
                    val selected = qualityBadge == tier.label
                    FilterChip(
                        selected = selected,
                        onClick = { qualityBadge = tier.label },
                        label = { Text(tier.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = ObsidianCard,
                            labelColor = TextSecondary,
                            selectedContainerColor = CinemaCrimson,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = if (posterUrl.startsWith("data:image/")) "" else posterUrl,
                onValueChange = { posterUrl = it },
                label = { Text("Poster HTTPS URL (Optional if uploaded)") },
                placeholder = { Text("https://cdn.example.com/poster.jpg", color = TextSecondary) },
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = streamUrl,
                onValueChange = { streamUrl = it; validationError = null },
                label = { Text("Authorized Video Streaming URL (HTTPS MP4 / HLS) *") },
                placeholder = { Text("https://...", color = TextSecondary) },
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_stream_url_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                AuthorizedSampleStreams.forEach { (label, url) ->
                    Surface(
                        color = ObsidianElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier.clickable { streamUrl = url; validationError = null }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            if (streamUrl.startsWith("https://", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        onPreviewStreamUrl(
                            title.ifBlank { "Stream Preview" },
                            selectedLanguage,
                            qualityBadge,
                            streamUrl.trim()
                        )
                    },
                    border = BorderStroke(1.dp, CinemaGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Video Stream")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Merchandising & Visibility Switches
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Visibility & Promotion",
                        style = MaterialTheme.typography.titleSmall,
                        color = CinemaGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ToggleRow(
                        label = "Published (Visible in NAA MOVIES WORLD)",
                        checked = published,
                        onCheckedChange = { published = it },
                        testTag = "editor_published_switch"
                    )
                    ToggleRow(
                        label = "Trending Movies Row",
                        checked = trending,
                        onCheckedChange = { trending = it },
                        testTag = "editor_trending_switch"
                    )
                    ToggleRow(
                        label = "Latest Releases Row",
                        checked = latest,
                        onCheckedChange = { latest = it },
                        testTag = "editor_latest_switch"
                    )
                    ToggleRow(
                        label = "Featured Hero Spotlight Banner",
                        checked = featured,
                        onCheckedChange = { featured = it },
                        testTag = "editor_featured_switch"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Licensing Confirmation
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (licenseConfirmed) EmeraldVerified.copy(alpha = 0.6f) else CinemaCrimson.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { licenseConfirmed = !licenseConfirmed }
                    ) {
                        Checkbox(
                            checked = licenseConfirmed,
                            onCheckedChange = { licenseConfirmed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EmeraldVerified,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = ObsidianBlack
                            ),
                            modifier = Modifier.testTag("editor_license_checkbox")
                        )
                        Text(
                            text = "I confirm this film & poster are legally licensed or open-licensed for streaming distribution.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = licenseNote,
                        onValueChange = { licenseNote = it },
                        label = { Text("License / Distribution Note") },
                        singleLine = true,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!validationError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = CinemaCrimson.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CinemaCrimson)
                ) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val cleanTitle = title.trim()
                    val cleanDesc = description.trim()
                    val cleanStream = streamUrl.trim()
                    val year = releaseYearText.toLongOrNull() ?: 2026L
                    val mins = durationText.toLongOrNull() ?: 135L
                    val rating = (ratingText.toDoubleOrNull() ?: 8.5).coerceIn(0.0, 10.0)

                    when {
                        cleanTitle.isBlank() -> validationError = "Enter a movie title."
                        cleanDesc.isBlank() -> validationError = "Enter a movie description."
                        !cleanStream.startsWith("https://", ignoreCase = true) ->
                            validationError = "Enter a valid HTTPS streaming URL (MP4 or HLS)."
                        !licenseConfirmed ->
                            validationError = "Confirm legal licensing authorization before publishing."
                        else -> {
                            val finalMovie = Movie(
                                id = existingMovie?.id.orEmpty(),
                                title = cleanTitle,
                                description = cleanDesc,
                                language = selectedLanguage,
                                category = selectedCategory,
                                genres = selectedGenres.ifEmpty { listOf(selectedCategory) },
                                releaseYear = year.coerceIn(1950L, 2035L),
                                durationMinutes = mins.coerceIn(1L, 600L),
                                rating = rating,
                                qualityBadge = qualityBadge,
                                posterUrl = posterUrl.trim(),
                                posterKey = posterKey.ifBlank { selectedLanguage.lowercase() },
                                streamUrl = cleanStream,
                                trending = trending,
                                latest = latest,
                                featured = featured,
                                licenseConfirmed = licenseConfirmed,
                                licenseNote = licenseNote.trim().ifBlank { "Authorized Studio Distribution" },
                                status = if (published) "published" else "draft",
                                createdBy = existingMovie?.createdBy.orEmpty(),
                                createdAt = existingMovie?.createdAt
                            )
                            onSaveMovie(finalMovie)
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaCrimson,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("editor_save_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving to Studio Catalog…")
                } else {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Save Movie Changes" else "Save & Publish Movie",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = CinemaGold,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = ObsidianElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
