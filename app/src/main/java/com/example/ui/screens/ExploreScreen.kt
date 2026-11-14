package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ads.AdFreeCinemaPassCard
import com.example.ads.UnityBannerAdSlot
import com.example.data.model.Movie
import com.example.data.model.MovieCategory
import com.example.data.model.MovieLanguage
import com.example.ui.components.GlassSurface
import com.example.ui.components.MoviePosterCard
import com.example.ui.theme.CinemaBrushes
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianGlass
import com.example.ui.theme.ObsidianGlassElevated
import com.example.ui.theme.ObsidianGlassStrong
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExploreScreen(
    movies: List<Movie>,
    savedIds: Set<String>,
    searchQuery: String,
    selectedLanguage: MovieLanguage,
    selectedCategory: MovieCategory,
    onSearchQueryChange: (String) -> Unit,
    onSelectLanguage: (MovieLanguage) -> Unit,
    onSelectCategory: (MovieCategory) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    adFreeMinutes: Int = 0,
    onWatchRewardedAd: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
    ) {
        val columnCount = when {
            maxWidth >= 900.dp -> 5
            maxWidth >= 650.dp -> 4
            maxWidth >= 420.dp -> 3
            else -> 2
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Header & Glassmorphic Search Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CinemaBrushes.goldMetallic)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Explore Cinema",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Surface(
                        color = ObsidianGlass,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(0.8.dp, CinemaGold.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${movies.size} Titles",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGoldSoft,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    containerColor = ObsidianGlassElevated,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = CinemaGold
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear search",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ObsidianGlassStrong,
                            unfocusedContainerColor = ObsidianGlassElevated,
                            focusedBorderColor = CinemaGold.copy(alpha = 0.7f),
                            unfocusedBorderColor = ObsidianBorder.copy(alpha = 0.4f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CinemaGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )
                }
            }

            // Language Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MovieLanguage.entries, key = { it.name }, contentType = { "lang_chip" }) { lang ->
                    val selected = selectedLanguage == lang
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectLanguage(lang) },
                        label = {
                            Text(
                                text = if (lang == MovieLanguage.ALL) "All Languages" else lang.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaCrimson,
                            selectedLabelColor = TextPrimary,
                            containerColor = ObsidianGlass,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = ObsidianBorder,
                            selectedBorderColor = CinemaGold.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.testTag("explore_lang_chip_${lang.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category Pills
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MovieCategory.entries, key = { it.name }, contentType = { "cat_chip" }) { cat ->
                    val selected = selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(cat) },
                        leadingIcon = if (cat == MovieCategory.ALL) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        label = {
                            Text(
                                text = if (cat == MovieCategory.ALL) "All Genres" else cat.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaGold.copy(alpha = 0.22f),
                            selectedLabelColor = CinemaGoldSoft,
                            selectedLeadingIconColor = CinemaGold,
                            containerColor = ObsidianGlass,
                            labelColor = TextSecondary,
                            iconColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = ObsidianBorder,
                            selectedBorderColor = CinemaGold
                        ),
                        modifier = Modifier.testTag("category_chip_${cat.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            UnityBannerAdSlot(adFreeMinutes = adFreeMinutes)

            Spacer(modifier = Modifier.height(6.dp))

            if (movies.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, end = 24.dp, bottom = 96.dp)
                ) {
                    GlassSurface(
                        shape = RoundedCornerShape(20.dp),
                        containerColor = ObsidianGlassElevated,
                        modifier = Modifier.widthIn(max = 420.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(26.dp)
                        ) {
                            Surface(
                                color = CinemaGold.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.4f)),
                                modifier = Modifier.size(58.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MovieFilter,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Matching Movies Found",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try adjusting your search keywords, language, or category filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            TextButton(
                                onClick = {
                                    onSearchQueryChange("")
                                    onSelectLanguage(MovieLanguage.ALL)
                                    onSelectCategory(MovieCategory.ALL)
                                }
                            ) {
                                Text("Reset Filters", color = CinemaCrimson)
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 108.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(
                        key = "explore_result_summary",
                        span = { GridItemSpan(columnCount) },
                        contentType = "explore_count_header"
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${movies.size} ${if (movies.size == 1) "Movie" else "Movies"} Available",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary
                            )
                            if (selectedLanguage != MovieLanguage.ALL || selectedCategory != MovieCategory.ALL || searchQuery.isNotBlank()) {
                                TextButton(
                                    onClick = {
                                        onSearchQueryChange("")
                                        onSelectLanguage(MovieLanguage.ALL)
                                        onSelectCategory(MovieCategory.ALL)
                                    }
                                ) {
                                    Text("Clear Filters", color = CinemaGold)
                                }
                            }
                        }
                    }

                    items(
                        items = movies,
                        key = { it.id },
                        contentType = { "explore_poster_card" }
                    ) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            isSaved = movie.id in savedIds,
                            onClick = { onMovieClick(movie) },
                            onToggleWatchlist = { onToggleWatchlist(movie) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (onWatchRewardedAd != null) {
                        item(
                            key = "explore_rewarded_pass",
                            span = { GridItemSpan(columnCount) },
                            contentType = "rewarded_pass"
                        ) {
                            AdFreeCinemaPassCard(
                                adFreeMinutes = adFreeMinutes,
                                onWatchRewardedAd = onWatchRewardedAd,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
