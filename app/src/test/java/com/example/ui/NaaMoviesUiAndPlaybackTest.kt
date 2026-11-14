package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.NaaMoviesApplication
import com.example.R
import com.example.ads.AdFreeCinemaPassCard
import com.example.data.LicensedCatalogDefaults
import com.example.data.model.Movie
import com.example.data.model.MovieCategory
import com.example.data.model.MovieLanguage
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchlistItem
import com.example.ui.components.posterDrawableRes
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MovieDetailsScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.NaaMoviesTheme
import com.example.ui.viewmodel.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NaaMoviesUiAndPlaybackTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleMovie: Movie = LicensedCatalogDefaults.movies.first()

    @Test
    fun `posterDrawableRes maps distinct licensed keys and initializes Coil caches`() {
        val deccan = posterDrawableRes("deccan", "Telugu")
        val quantum = posterDrawableRes("quantum", "Telugu")
        val wildlands = posterDrawableRes("wildlands", "English")
        val telugu = posterDrawableRes("telugu", "Telugu")
        val tamil = posterDrawableRes("tamil", "Tamil")
        val hindi = posterDrawableRes("hindi", "Hindi")
        val english = posterDrawableRes("english", "English")
        val splash = posterDrawableRes("splash", "All")

        assertEquals(R.drawable.img_poster_deccan_fort_1794669635334, deccan)
        assertEquals(R.drawable.img_poster_quantum_drift_1794669635127, quantum)
        assertEquals(R.drawable.img_poster_wildlands_1794669636555, wildlands)
        assertNotEquals(telugu, tamil)
        assertNotEquals(hindi, english)
        assertEquals(R.drawable.img_splash_hero_1794667014216, splash)

        val app = ApplicationProvider.getApplicationContext<NaaMoviesApplication>()
        val loader = app.newImageLoader()
        assertNotNull(loader.memoryCache)
        assertNotNull(loader.diskCache)
        loader.shutdown()
    }

    @Test
    fun `home screen renders featured banner collections carousels and routes category selection`() {
        var clickedCategory: MovieCategory? = null
        var clickedCollectionId: String? = null
        var clickedMovieId: String? = null

        composeRule.setContent {
            NaaMoviesTheme {
                HomeScreen(
                    movies = LicensedCatalogDefaults.movies,
                    savedIds = setOf(sampleMovie.id),
                    watchHistory = listOf(
                        WatchHistoryItem(
                            movieId = sampleMovie.id,
                            title = sampleMovie.title,
                            posterUrl = sampleMovie.posterUrl,
                            posterKey = sampleMovie.posterKey,
                            positionMs = 30_000L,
                            durationMs = 120_000L
                        )
                    ),
                    selectedLanguage = MovieLanguage.ALL,
                    catalogError = null,
                    onSelectLanguage = {},
                    onMovieClick = { clickedMovieId = it.id },
                    onPlayClick = {},
                    onToggleWatchlist = {},
                    onOpenSearch = {},
                    onSeeLanguageCatalog = {},
                    onSelectCategoryAndExplore = { clickedCategory = it },
                    onSelectCollection = { clickedCollectionId = it.id }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("NAA MOVIES").assertIsDisplayed()

        composeRule.onNodeWithTag("home_lazy_column").performScrollToKey("category_strip")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Browse by Genre").assertIsDisplayed()
        composeRule.onNodeWithTag("home_category_tile_action").performClick()
        assertEquals(MovieCategory.ACTION, clickedCategory)

        composeRule.onNodeWithTag("home_lazy_column").performScrollToKey("popular_collections")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("collection_card_south_cinema").performClick()
        assertEquals("south_cinema", clickedCollectionId)

        composeRule.onNodeWithTag("home_lazy_column").performScrollToKey("trending_row")
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasTestTag("movie_card_${sampleMovie.id}")).onFirst().performClick()
        assertEquals(sampleMovie.id, clickedMovieId)
    }

    @Test
    fun `explore screen filters by search query language and genre without authentication gate`() {
        var currentQuery by mutableStateOf("")
        var currentLanguage by mutableStateOf(MovieLanguage.ALL)
        var currentCategory by mutableStateOf(MovieCategory.ALL)

        composeRule.setContent {
            val filtered = LicensedCatalogDefaults.movies.filter { movie ->
                val matchesLang = currentLanguage == MovieLanguage.ALL ||
                    movie.language.equals(currentLanguage.label, ignoreCase = true)
                val matchesCat = currentCategory == MovieCategory.ALL ||
                    movie.category.equals(currentCategory.label, ignoreCase = true) ||
                    movie.genres.any { it.equals(currentCategory.label, ignoreCase = true) }
                val q = currentQuery.trim().lowercase()
                val matchesQuery = q.isEmpty() || movie.title.lowercase().contains(q)
                matchesLang && matchesCat && matchesQuery
            }

            NaaMoviesTheme {
                ExploreScreen(
                    movies = filtered,
                    savedIds = emptySet(),
                    searchQuery = currentQuery,
                    selectedLanguage = currentLanguage,
                    selectedCategory = currentCategory,
                    onSearchQueryChange = { currentQuery = it },
                    onSelectLanguage = { currentLanguage = it },
                    onSelectCategory = { currentCategory = it },
                    onMovieClick = {},
                    onToggleWatchlist = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Explore Cinema").assertIsDisplayed()
        composeRule.onNodeWithTag("search_input").performTextInput("Rudra")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("1 Movie Available").assertIsDisplayed()
        composeRule.onNodeWithTag("movie_card_${sampleMovie.id}").assertIsDisplayed()

        composeRule.onNodeWithTag("clear_search_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("category_chip_thriller").performClick()
        composeRule.waitForIdle()
        assertEquals(MovieCategory.THRILLER, currentCategory)
    }

    @Test
    fun `movie details screen renders quality badges cast crew related movies and action buttons`() {
        var played = false
        var toggledWatchlist = false
        var relatedClicked: String? = null
        val related = LicensedCatalogDefaults.movies.drop(1).take(3)

        composeRule.setContent {
            NaaMoviesTheme {
                MovieDetailsScreen(
                    movie = sampleMovie,
                    isSaved = false,
                    resumePositionMs = 0L,
                    relatedMovies = related,
                    savedIds = emptySet(),
                    onBackClick = {},
                    onPlayClick = { played = true },
                    onToggleWatchlist = { toggledWatchlist = true },
                    onRelatedMovieClick = { relatedClicked = it.id }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(sampleMovie.title).assertIsDisplayed()

        composeRule.onNodeWithTag("details_lazy_column").performScrollToKey("details_quality_badges")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("4K UHD").assertIsDisplayed()
        composeRule.onNodeWithText("1080p HD").assertIsDisplayed()
        composeRule.onNodeWithText("720p HD").assertIsDisplayed()

        composeRule.onNodeWithTag("details_lazy_column").performScrollToKey("details_actions")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("watch_now_button").performClick()
        assertTrue(played)

        composeRule.onNodeWithTag("watchlist_toggle_button").performClick()
        assertTrue(toggledWatchlist)

        composeRule.onNodeWithTag("details_lazy_column").performScrollToKey("details_cast_crew")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Cast & Crew").assertIsDisplayed()
        composeRule.onNodeWithText("Ian Hubert").assertIsDisplayed()

        composeRule.onNodeWithTag("details_lazy_column").performScrollToKey("details_related")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("movie_card_${related.first().id}").performClick()
        assertEquals(related.first().id, relatedClicked)
    }

    @Test
    fun `watchlist screen renders saved movies and triggers play and remove`() {
        var playTitle: String? = null
        var removedTitle: String? = null
        val savedItem = WatchlistItem(
            movieId = sampleMovie.id,
            title = sampleMovie.title,
            description = sampleMovie.description,
            language = sampleMovie.language,
            category = sampleMovie.category,
            releaseYear = sampleMovie.releaseYear,
            durationMinutes = sampleMovie.durationMinutes,
            rating = sampleMovie.rating,
            qualityBadge = sampleMovie.qualityBadge,
            posterUrl = sampleMovie.posterUrl,
            posterKey = sampleMovie.posterKey,
            streamUrl = sampleMovie.streamUrl,
            licenseNote = sampleMovie.licenseNote,
            addedAtMs = 1_000L
        )

        composeRule.setContent {
            NaaMoviesTheme {
                WatchlistScreen(
                    watchlistState = UiState.Success(listOf(savedItem)),
                    onMovieClick = {},
                    onPlayClick = { playTitle = it.title },
                    onRemoveFromWatchlist = { removedTitle = it.title },
                    onExploreClick = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("1 Saved").assertIsDisplayed()
        composeRule.onNodeWithTag("watchlist_play_${sampleMovie.id}").performClick()
        assertEquals(sampleMovie.title, playTitle)

        composeRule.onNodeWithTag("watchlist_remove_${sampleMovie.id}").performClick()
        assertEquals(sampleMovie.title, removedTitle)
    }

    @Test
    fun `video player renders transport quality subtitles fullscreen controls and saves progress on exit`() {
        var backPressed = false
        var savedPos = -1L

        composeRule.setContent {
            NaaMoviesTheme {
                VideoPlayerScreen(
                    movie = sampleMovie,
                    initialPositionMs = 12_000L,
                    onSaveProgress = { pos, _ -> savedPos = pos },
                    onBackClick = { backPressed = true }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("player_play_pause_button").assertIsDisplayed()
        composeRule.onNodeWithTag("player_rewind_button").assertIsDisplayed()
        composeRule.onNodeWithTag("player_forward_button").assertIsDisplayed()
        composeRule.onNodeWithTag("player_quality_button").assertIsDisplayed()
        composeRule.onNodeWithTag("player_subtitles_button").assertIsDisplayed()
        composeRule.onNodeWithTag("player_fullscreen_button").assertIsDisplayed()

        // Open Quality Selector and switch to 1080p HD
        composeRule.onNodeWithTag("player_quality_button").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("1080p HD").onFirst().performClick()
        composeRule.waitForIdle()

        // Open Subtitle Selector and select English (CC)
        composeRule.onNodeWithTag("player_subtitles_button").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("English (CC)").onFirst().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("CC: English", substring = true).assertIsDisplayed()

        // Rewind & Forward 10s
        composeRule.onNodeWithTag("player_forward_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("+10s").assertIsDisplayed()

        // Exit player saves progress
        composeRule.onNodeWithTag("player_back_button").performClick()
        composeRule.waitForIdle()
        assertTrue(backPressed)
        assertTrue(savedPos >= 0L)
    }

    @Test
    fun `ad free cinema pass card renders locked state triggers rewarded callback and shows active state`() {
        var rewardedRequested = false
        var adFreeMinutes by mutableStateOf(0)

        composeRule.setContent {
            NaaMoviesTheme {
                AdFreeCinemaPassCard(
                    adFreeMinutes = adFreeMinutes,
                    onWatchRewardedAd = {
                        rewardedRequested = true
                        adFreeMinutes = 30
                    }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("rewarded_ad_pass_card").assertIsDisplayed()
        composeRule.onNodeWithText("Unlock 30m Ad-Free Cinema").assertIsDisplayed()
        composeRule.onNodeWithText("30m Free").assertIsDisplayed()
        composeRule.onNodeWithTag("rewarded_ad_pass_button", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithTag("rewarded_ad_pass_card").performClick()
        composeRule.waitForIdle()

        assertTrue(rewardedRequested)
        composeRule.onNodeWithText("Ad-Free Cinema Pass Active").assertIsDisplayed()
        composeRule.onNodeWithText("30 min uninterrupted browsing remaining").assertIsDisplayed()
    }
}
