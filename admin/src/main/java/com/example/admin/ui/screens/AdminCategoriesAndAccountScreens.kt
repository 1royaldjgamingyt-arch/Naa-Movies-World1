package com.example.admin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.admin.data.model.Movie
import com.example.admin.data.model.MovieCategory
import com.example.admin.ui.theme.CinemaCrimson
import com.example.admin.ui.theme.CinemaGold
import com.example.admin.ui.theme.EmeraldVerified
import com.example.admin.ui.theme.ObsidianBlack
import com.example.admin.ui.theme.ObsidianBorder
import com.example.admin.ui.theme.ObsidianCard
import com.example.admin.ui.theme.ObsidianElevated
import com.example.admin.ui.theme.TextPrimary
import com.example.admin.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseUser

@Composable
fun AdminCategoriesScreen(
    movies: List<Movie>,
    onReassignCategory: (Movie, String) -> Unit,
    onEditMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedCategory by remember { mutableStateOf<String?>(MovieCategory.ACTION.label) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "TAXONOMY & CURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold
                )
                Text(
                    text = "Manage Categories",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap any category to inspect assigned films or move movies across categories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        items(MovieCategory.uploadCategories, key = { it.label }) { category ->
            val inCategory = remember(movies, category) {
                movies.filter {
                    it.category.equals(category.label, ignoreCase = true) ||
                        it.genres.any { g -> g.equals(category.label, ignoreCase = true) }
                }
            }
            val isExpanded = expandedCategory == category.label

            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (isExpanded) CinemaGold.copy(alpha = 0.65f) else ObsidianBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCategory = if (isExpanded) null else category.label
                            }
                            .testTag("admin_category_row_${category.label}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = CinemaGold.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Category,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${inCategory.size} titles • ${inCategory.count { it.status == "published" }} live",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Surface(
                            color = ObsidianElevated,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isExpanded) "Hide" else "Manage",
                                style = MaterialTheme.typography.labelMedium,
                                color = CinemaGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        if (inCategory.isEmpty()) {
                            Text(
                                text = "No movies currently assigned to ${category.label}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        } else {
                            inCategory.forEach { movie ->
                                CategoryMovieReassignRow(
                                    movie = movie,
                                    onReassignCategory = { nextCategory ->
                                        onReassignCategory(movie, nextCategory)
                                    },
                                    onOpenEditor = { onEditMovieClick(movie) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryMovieReassignRow(
    movie: Movie,
    onReassignCategory: (String) -> Unit,
    onOpenEditor: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onOpenEditor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Movie,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Primary: ${movie.category} • ${movie.language}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = "Change primary category",
                    tint = CinemaGold
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(ObsidianElevated)
            ) {
                MovieCategory.uploadCategories.forEach { cat ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Set ${cat.label}",
                                color = if (movie.category == cat.label) CinemaGold else TextPrimary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onReassignCategory(cat.label)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminAccountScreen(
    adminUser: FirebaseUser,
    movies: List<Movie>,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "STUDIO SECURITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold
                )
                Text(
                    text = "Admin Console Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
        }

        item {
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, EmeraldVerified.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Surface(
                        color = EmeraldVerified.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = "Verified Administrator",
                            tint = EmeraldVerified,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = adminUser.displayName ?: "Studio Administrator",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = adminUser.email ?: "Verified Studio Admin",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldVerified
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Shared Firebase Project • Public User Catalog Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = ObsidianCard,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Catalog & Security Rules Status",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                    }
                    SummaryRow("Total Catalog Titles", movies.size.toString())
                    SummaryRow("Published to Public App", movies.count { it.status == "published" }.toString())
                    SummaryRow("Unpublished Drafts (Admin Only)", movies.count { it.status != "published" }.toString())
                    SummaryRow("Trending Spotlight", movies.count { it.trending }.toString())
                    SummaryRow("Latest Releases", movies.count { it.latest }.toString())
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onSignOutClick,
                border = BorderStroke(1.dp, CinemaCrimson),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaCrimson),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
                    .testTag("admin_sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out of Studio Admin",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = CinemaGold)
    }
}
