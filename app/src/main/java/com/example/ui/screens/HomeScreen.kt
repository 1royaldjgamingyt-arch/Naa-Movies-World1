package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.model.MovieCategory
import com.example.data.model.MovieLanguage
import com.example.data.model.PopularCollection
import com.example.data.model.WatchHistoryItem
import com.example.ui.components.CategoryNavigationStrip
import com.example.ui.components.GlassSurface
import com.example.ui.components.HeroSpotlightBanner
import com.example.ui.components.MoviePosterImage
import com.example.ui.components.MovieSectionRow
import com.example.ui.components.NaaMoviesBrandCrest
import com.example.ui.components.PopularCollectionsSection
import com.example.ui.theme.CinemaBrushes
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianGlass
import com.example.ui.theme.ObsidianGlassStrong
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    movies: List<Movie>,
    savedIds: Set<String>,
    watchHistory: List<WatchHistoryItem>,
    selectedLanguage: MovieLanguage,
    catalogError: String?,
    onSelectLanguage: (MovieLanguage) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onPlayClick: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    onOpenSearch: () -> Unit,
    onSeeLanguageCatalog: (MovieLanguage) -> Unit,
    onSelectCategoryAndExplore: (MovieCategory) -> Unit,
    onSelectCollection: (PopularCollection) -> Unit,
    modifier: Modifier = Modifier
) {
    val languageFiltered = remember(movies, selectedLanguage) {
        if (selectedLanguage == MovieLanguage.ALL) movies
        else movies.filter { it.language.equals(selectedLanguage.label, ignoreCase = true) }
    }

    val featuredMovies = remember(languageFiltered) {
        languageFiltered.filter { it.featured }.ifEmpty { languageFiltered.take(4) }
    }
    val trendingMovies = remember(languageFiltered) {
        languageFiltered.filter { it.trending }.ifEmpty { languageFiltered.take(6) }
    }
    val latestMovies = remember(languageFiltered) {
        languageFiltered.filter { it.latest }.ifEmpty { languageFiltered.take(6) }
    }

    val teluguMovies = remember(movies) { movies.filter { it.language.equals("Telugu", ignoreCase = true) } }
    val tamilMovies = remember(movies) { movies.filter { it.language.equals("Tamil", ignoreCase = true) } }
    val hindiMovies = remember(movies) { movies.filter { it.language.equals("Hindi", ignoreCase = true) } }
    val englishMovies = remember(movies) { movies.filter { it.language.equals("English", ignoreCase = true) } }

    val popularCollections = remember(movies) { buildPopularCollections(movies) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_lazy_column")
        ) {
            // Cinematic Featured Hero Spotlight + Floating Glass Brand Bar
            item(key = "hero_spotlight", contentType = "hero") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    HeroSpotlightBanner(
                        featuredMovies = featuredMovies,
                        savedIds = savedIds,
                        onPlayClick = onPlayClick,
                        onDetailsClick = onMovieClick,
                        onToggleWatchlist = onToggleWatchlist
                    )

                    HomeTopBar(
                        onOpenSearch = onOpenSearch,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                    )
                }
            }

            if (!catalogError.isNullOrBlank()) {
                item(key = "catalog_error_notice", contentType = "notice") {
                    GlassSurface(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Cloud sync paused ($catalogError). Showing licensed offline catalog.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Language Switcher Pills
            item(key = "language_chips", contentType = "chips") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MovieLanguage.entries, key = { it.name }) { lang ->
                        val isSelected = selectedLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectLanguage(lang) },
                            label = {
                                Text(
                                    text = if (lang == MovieLanguage.ALL) "All Cinema" else "${lang.label} Movies",
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
                                selected = isSelected,
                                borderColor = ObsidianBorder,
                                selectedBorderColor = CinemaGold.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("lang_chip_${lang.name.lowercase()}")
                        )
                    }
                }
            }

            // Category Navigation Strip
            item(key = "category_strip", contentType = "categories") {
                CategoryNavigationStrip(onCategoryClick = onSelectCategoryAndExplore)
            }

            // Continue Watching
            if (watchHistory.isNotEmpty()) {
                item(key = "continue_watching", contentType = "continue_watching") {
                    ContinueWatchingRow(
                        history = watchHistory,
                        allMovies = movies,
                        onResumeClick = onPlayClick
                    )
                }
            }

            // Trending Now with Rank Badges
            item(key = "trending_row", contentType = "carousel") {
                MovieSectionRow(
                    title = "Trending Movies",
                    movies = trendingMovies,
                    savedIds = savedIds,
                    showRankBadges = true,
                    accentColor = CinemaCrimson,
                    onMovieClick = onMovieClick,
                    onToggleWatchlist = onToggleWatchlist,
                    onSeeAllClick = { onSeeLanguageCatalog(selectedLanguage) }
                )
            }

            // Popular Collections
            if (popularCollections.isNotEmpty()) {
                item(key = "popular_collections", contentType = "collections") {
                    PopularCollectionsSection(
                        collections = popularCollections,
                        onCollectionClick = onSelectCollection
                    )
                }
            }

            // Latest Releases
            item(key = "latest_row", contentType = "carousel") {
                MovieSectionRow(
                    title = "Latest Releases",
                    movies = latestMovies,
                    savedIds = savedIds,
                    accentColor = CinemaGold,
                    onMovieClick = onMovieClick,
                    onToggleWatchlist = onToggleWatchlist,
                    onSeeAllClick = { onSeeLanguageCatalog(selectedLanguage) }
                )
            }

            // Regional & World Cinema Rows
            if (selectedLanguage == MovieLanguage.ALL || selectedLanguage == MovieLanguage.TELUGU) {
                item(key = "telugu_row", contentType = "carousel") {
                    MovieSectionRow(
                        title = "Telugu Movies",
                        movies = teluguMovies,
                        savedIds = savedIds,
                        onMovieClick = onMovieClick,
                        onToggleWatchlist = onToggleWatchlist,
                        onSeeAllClick = { onSeeLanguageCatalog(MovieLanguage.TELUGU) }
                    )
                }
            }

            if (selectedLanguage == MovieLanguage.ALL || selectedLanguage == MovieLanguage.TAMIL) {
                item(key = "tamil_row", contentType = "carousel") {
                    MovieSectionRow(
                        title = "Tamil Movies",
                        movies = tamilMovies,
                        savedIds = savedIds,
                        onMovieClick = onMovieClick,
                        onToggleWatchlist = onToggleWatchlist,
                        onSeeAllClick = { onSeeLanguageCatalog(MovieLanguage.TAMIL) }
                    )
                }
            }

            if (selectedLanguage == MovieLanguage.ALL || selectedLanguage == MovieLanguage.HINDI) {
                item(key = "hindi_row", contentType = "carousel") {
                    MovieSectionRow(
                        title = "Hindi Movies",
                        movies = hindiMovies,
                        savedIds = savedIds,
                        onMovieClick = onMovieClick,
                        onToggleWatchlist = onToggleWatchlist,
                        onSeeAllClick = { onSeeLanguageCatalog(MovieLanguage.HINDI) }
                    )
                }
            }

            if (selectedLanguage == MovieLanguage.ALL || selectedLanguage == MovieLanguage.ENGLISH) {
                item(key = "english_row", contentType = "carousel") {
                    MovieSectionRow(
                        title = "English Movies",
                        movies = englishMovies,
                        savedIds = savedIds,
                        onMovieClick = onMovieClick,
                        onToggleWatchlist = onToggleWatchlist,
                        onSeeAllClick = { onSeeLanguageCatalog(MovieLanguage.ENGLISH) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        GlassSurface(
            shape = RoundedCornerShape(14.dp),
            containerColor = ObsidianGlassStrong,
            shadowElevation = 10.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
            ) {
                NaaMoviesBrandCrest(size = 28.dp)
                Spacer(modifier = Modifier.width(9.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NAA MOVIES",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WORLD",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = CinemaGold
                        )
                    }
                    Text(
                        text = "4K ULTRA CINEMA",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.2.sp),
                        color = CinemaGoldSoft
                    )
                }
            }
        }

        GlassSurface(
            shape = CircleShape,
            containerColor = ObsidianGlassStrong,
            shadowElevation = 10.dp
        ) {
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("home_search_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search movies",
                    tint = CinemaGoldSoft
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingRow(
    history: List<WatchHistoryItem>,
    allMovies: List<Movie>,
    onResumeClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CinemaBrushes.goldMetallic)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Continue Watching",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history, key = { it.movieId }, contentType = { "continue_card" }) { item ->
                val matchedMovie = allMovies.firstOrNull { it.id == item.movieId }
                if (matchedMovie != null) {
                    val progress = if (item.durationMs > 0L) {
                        (item.positionMs.toFloat() / item.durationMs.toFloat()).coerceIn(0.05f, 1f)
                    } else 0.15f

                    GlassSurface(
                        shape = RoundedCornerShape(14.dp),
                        containerColor = ObsidianCard,
                        modifier = Modifier
                            .width(214.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onResumeClick(matchedMovie) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            ) {
                                MoviePosterImage(
                                    posterUrl = item.posterUrl,
                                    posterKey = item.posterKey,
                                    language = matchedMovie.language,
                                    contentDescription = item.title,
                                    thumbnailMode = true,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, ObsidianBlack.copy(alpha = 0.84f))
                                            )
                                        )
                                )
                                Surface(
                                    color = CinemaCrimson.copy(alpha = 0.94f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.Center)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Resume ${item.title}",
                                        tint = TextPrimary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                color = CinemaGold,
                                trackColor = ObsidianElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Resume",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildPopularCollections(movies: List<Movie>): List<PopularCollection> {
    if (movies.isEmpty()) return emptyList()
    val southIndian = movies.filter {
        it.language.equals("Telugu", true) || it.language.equals("Tamil", true)
    }
    val highActionThrillers = movies.filter {
        it.category.equals("Action", true) || it.category.equals("Thriller", true)
    }
    val sciFiAdventure = movies.filter {
        it.category.equals("Sci-Fi", true) || it.category.equals("Adventure", true)
    }
    val hindiBlockbusters = movies.filter { it.language.equals("Hindi", true) }
    val ultra4K = movies.filter { it.qualityBadge.contains("4K", true) }

    return listOfNotNull(
        southIndian.takeIf { it.isNotEmpty() }?.let {
            PopularCollection(
                id = "south_cinema",
                title = "South Indian Cinema",
                subtitle = "Telugu & Tamil Hits",
                badge = "SPOTLIGHT",
                filterLanguage = MovieLanguage.TELUGU,
                movies = it
            )
        },
        highActionThrillers.takeIf { it.isNotEmpty() }?.let {
            PopularCollection(
                id = "action_thrillers",
                title = "High-Octane Thrillers",
                subtitle = "Pulse-Pounding Cinema",
                badge = "ACTION",
                filterCategory = MovieCategory.THRILLER,
                movies = it
            )
        },
        sciFiAdventure.takeIf { it.isNotEmpty() }?.let {
            PopularCollection(
                id = "scifi_worlds",
                title = "Sci-Fi & Adventure",
                subtitle = "Beyond Horizons",
                badge = "IMAX",
                filterCategory = MovieCategory.SCI_FI,
                movies = it
            )
        },
        hindiBlockbusters.takeIf { it.isNotEmpty() }?.let {
            PopularCollection(
                id = "hindi_classics",
                title = "Hindi Cinema Selects",
                subtitle = "Drama & Romance",
                badge = "HINDI",
                filterLanguage = MovieLanguage.HINDI,
                movies = it
            )
        },
        ultra4K.takeIf { it.isNotEmpty() }?.let {
            PopularCollection(
                id = "ultra_4k",
                title = "4K UHD Showcase",
                subtitle = "Crystal-Clear Mastering",
                badge = "4K UHD",
                movies = it
            )
        }
    )
}
