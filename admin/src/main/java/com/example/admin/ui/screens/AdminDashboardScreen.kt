package com.example.admin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.admin.R
import com.example.admin.data.model.AdminCatalogFilter
import com.example.admin.data.model.Movie
import com.example.admin.data.model.MovieLanguage
import com.example.admin.ui.components.AdminPosterImage
import com.example.admin.ui.theme.CinemaCrimson
import com.example.admin.ui.theme.CinemaGold
import com.example.admin.ui.theme.ElectricCyan
import com.example.admin.ui.theme.EmeraldVerified
import com.example.admin.ui.theme.ObsidianBlack
import com.example.admin.ui.theme.ObsidianBorder
import com.example.admin.ui.theme.ObsidianCard
import com.example.admin.ui.theme.ObsidianElevated
import com.example.admin.ui.theme.TextPrimary
import com.example.admin.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    allMovies: List<Movie>,
    filteredMovies: List<Movie>,
    searchQuery: String,
    selectedFilter: AdminCatalogFilter,
    selectedLanguage: MovieLanguage,
    catalogError: String?,
    onSearchQueryChange: (String) -> Unit,
    onSelectFilter: (AdminCatalogFilter) -> Unit,
    onSelectLanguage: (MovieLanguage) -> Unit,
    onAddMovieClick: () -> Unit,
    onEditMovieClick: (Movie) -> Unit,
    onPreviewStreamClick: (Movie) -> Unit,
    onTogglePublish: (Movie) -> Unit,
    onToggleTrending: (Movie) -> Unit,
    onToggleLatest: (Movie) -> Unit,
    onDeleteMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    var movieToDelete by remember { mutableStateOf<Movie?>(null) }

    val publishedCount = remember(allMovies) { allMovies.count { it.status == "published" } }
    val draftCount = remember(allMovies) { allMovies.count { it.status != "published" } }
    val trendingCount = remember(allMovies) { allMovies.count { it.trending } }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 104.dp),
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.admin_launcher_emblem_1794668485984),
                        contentDescription = "Studio Admin",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "STUDIO CATALOG",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold
                        )
                        Text(
                            text = "NAA MOVIES ADMIN",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onAddMovieClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaCrimson,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("admin_add_movie_top_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // KPI Metrics Strip
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                DashboardMetricCard(
                    label = "Total",
                    value = allMovies.size.toString(),
                    accent = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    label = "Live",
                    value = publishedCount.toString(),
                    accent = EmeraldVerified,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    label = "Drafts",
                    value = draftCount.toString(),
                    accent = CinemaGold,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    label = "Trending",
                    value = trendingCount.toString(),
                    accent = CinemaCrimson,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search catalog title, language, category…", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = CinemaGold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ObsidianCard,
                    unfocusedContainerColor = ObsidianCard,
                    focusedBorderColor = CinemaGold,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("admin_search_input")
            )
        }

        // Status filter chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                AdminCatalogFilter.entries.forEach { filter ->
                    val selected = filter == selectedFilter
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectFilter(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = ObsidianCard,
                            labelColor = TextSecondary,
                            selectedContainerColor = CinemaCrimson,
                            selectedLabelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = ObsidianBorder,
                            selectedBorderColor = CinemaCrimson
                        )
                    )
                }
            }
        }

        // Language filter chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                MovieLanguage.entries.forEach { lang ->
                    val selected = lang == selectedLanguage
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectLanguage(lang) },
                        label = { Text(lang.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = ObsidianElevated.copy(alpha = 0.7f),
                            labelColor = TextSecondary,
                            selectedContainerColor = CinemaGold.copy(alpha = 0.22f),
                            selectedLabelColor = CinemaGold
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = ObsidianBorder,
                            selectedBorderColor = CinemaGold
                        )
                    )
                }
            }
        }

        if (!catalogError.isNullOrBlank()) {
            item {
                Surface(
                    color = CinemaCrimson.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CinemaCrimson.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = catalogError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        if (filteredMovies.isEmpty()) {
            item {
                Surface(
                    color = ObsidianCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "No Movies Match Filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Adjust your search or upload a new licensed movie to the studio catalog.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredMovies, key = { it.id }) { movie ->
                AdminMovieManagementCard(
                    movie = movie,
                    onEditClick = { onEditMovieClick(movie) },
                    onPreviewClick = { onPreviewStreamClick(movie) },
                    onTogglePublish = { onTogglePublish(movie) },
                    onToggleTrending = { onToggleTrending(movie) },
                    onToggleLatest = { onToggleLatest(movie) },
                    onDeleteClick = { movieToDelete = movie },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }

    movieToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { movieToDelete = null },
            containerColor = ObsidianCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Delete Movie?") },
            text = {
                Text(
                    "Remove \"${target.title}\" permanently from Firestore? It will immediately disappear from NAA MOVIES WORLD."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMovie(target)
                        movieToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaCrimson)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { movieToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DashboardMetricCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AdminMovieManagementCard(
    movie: Movie,
    onEditClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onTogglePublish: () -> Unit,
    onToggleTrending: () -> Unit,
    onToggleLatest: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPublished = movie.status == "published"

    Surface(
        color = ObsidianCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_movie_card_${movie.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(78.dp)
                        .aspectRatio(0.68f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianElevated)
                        .clickable(onClick = onEditClick)
                ) {
                    AdminPosterImage(
                        posterUrl = movie.posterUrl,
                        posterKey = movie.posterKey,
                        language = movie.language,
                        contentDescription = "${movie.title} poster",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatusBadgePill(
                            text = if (isPublished) "PUBLISHED" else "UNPUBLISHED",
                            color = if (isPublished) EmeraldVerified else CinemaGold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", movie.rating),
                                style = MaterialTheme.typography.labelMedium,
                                color = CinemaGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${movie.language} • ${movie.category} • ${movie.releaseYear} • ${movie.durationMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Admin Action Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                QuickFlagButton(
                    icon = if (isPublished) Icons.Filled.Public else Icons.Filled.PublicOff,
                    label = if (isPublished) "Live" else "Publish",
                    active = isPublished,
                    activeColor = EmeraldVerified,
                    onClick = onTogglePublish,
                    tag = "admin_publish_toggle_${movie.id}"
                )
                QuickFlagButton(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Trending",
                    active = movie.trending,
                    activeColor = CinemaCrimson,
                    onClick = onToggleTrending,
                    tag = "admin_trending_toggle_${movie.id}"
                )
                QuickFlagButton(
                    icon = Icons.Filled.NewReleases,
                    label = "Latest",
                    active = movie.latest,
                    activeColor = ElectricCyan,
                    onClick = onToggleLatest,
                    tag = "admin_latest_toggle_${movie.id}"
                )
                QuickFlagButton(
                    icon = Icons.Filled.PlayCircle,
                    label = "Stream",
                    active = movie.streamUrl.isNotBlank(),
                    activeColor = CinemaGold,
                    onClick = onPreviewClick,
                    tag = "admin_preview_stream_${movie.id}"
                )
                QuickFlagButton(
                    icon = Icons.Filled.Edit,
                    label = "Edit",
                    active = false,
                    activeColor = TextPrimary,
                    onClick = onEditClick,
                    tag = "admin_edit_button_${movie.id}"
                )
                QuickFlagButton(
                    icon = Icons.Filled.Delete,
                    label = "Delete",
                    active = false,
                    activeColor = CinemaCrimson,
                    onClick = onDeleteClick,
                    tag = "admin_delete_button_${movie.id}"
                )
            }
        }
    }
}

@Composable
private fun StatusBadgePill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun QuickFlagButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        color = if (active) activeColor.copy(alpha = 0.18f) else ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (active) activeColor.copy(alpha = 0.65f) else ObsidianBorder),
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) activeColor else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) activeColor else TextPrimary
            )
        }
    }
}
