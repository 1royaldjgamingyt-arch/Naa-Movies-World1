package com.example.ui

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.R
import com.example.ads.UnityAdsManager
import kotlinx.coroutines.launch
import com.example.data.local.WatchlistDatabase
import com.example.data.model.MovieLanguage
import com.example.data.repository.LocalWatchlistRepository
import com.example.data.repository.MovieRepository
import com.example.ui.components.GlassSurface
import com.example.ui.components.NaaMoviesBrandCrest
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MovieDetailsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.CinemaBrushes
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaCrimsonBright
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianGlassStrong
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MoviesViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object ExploreRoute

@Serializable
data object WatchlistRoute

@Serializable
data class MovieDetailsRoute(val movieId: String)

@Serializable
data class VideoPlayerRoute(val movieId: String)

private data class BottomNavDestination(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val TopLevelDestinations = listOf(
    BottomNavDestination(HomeRoute, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    BottomNavDestination(ExploreRoute, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_explore"),
    BottomNavDestination(WatchlistRoute, "Watchlist", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_watchlist")
)

private fun navigateToTopLevel(navController: NavController, route: Any) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation() {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    MainAppScaffold()
}

@Composable
private fun MainAppScaffold() {
    val viewModel: MoviesViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val localDb = WatchlistDatabase.getInstance(app)
                MoviesViewModel(
                    movieRepository = MovieRepository(db),
                    watchlistRepository = LocalWatchlistRepository(localDb.watchlistDao())
                )
            }
        }
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val movies by viewModel.catalogMovies.collectAsStateWithLifecycle()
    val catalogError by viewModel.catalogError.collectAsStateWithLifecycle()
    val exploreMovies by viewModel.filteredExploreMovies.collectAsStateWithLifecycle()
    val watchlistState by viewModel.watchlistState.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val savedIds = remember(watchlistState) {
        (watchlistState as? UiState.Success)?.data?.map { it.movieId }?.toSet() ?: emptySet()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val adFreeMinutes by UnityAdsManager.adFreeRemainingMinutes.collectAsStateWithLifecycle()

    LaunchedEffect(context) {
        UnityAdsManager.refreshAdFreeStatus(context)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val launchRewardedPass: () -> Unit = {
        UnityAdsManager.showRewardedAd(
            activity = activity,
            onRewardGranted = { mins ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Ad-Free Cinema Pass unlocked for $mins minutes!")
                }
            },
            onUnavailable = { msg ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    val showChrome = currentDestination?.let { dest ->
        dest.hasRoute<HomeRoute>() ||
            dest.hasRoute<ExploreRoute>() ||
            dest.hasRoute<WatchlistRoute>()
    } ?: true

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            containerColor = ObsidianBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = if (showChrome && !useRail) {
                        Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 84.dp)
                    } else {
                        Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (showChrome && useRail) {
                        NavigationRail(
                            containerColor = ObsidianSurface,
                            contentColor = TextPrimary,
                            header = {
                                Spacer(modifier = Modifier.height(12.dp))
                                NaaMoviesBrandCrest(size = 34.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                            },
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            TopLevelDestinations.forEach { dest ->
                                val selected = currentDestination?.hierarchy?.any {
                                    when (dest.route) {
                                        is HomeRoute -> it.hasRoute<HomeRoute>()
                                        is ExploreRoute -> it.hasRoute<ExploreRoute>()
                                        is WatchlistRoute -> it.hasRoute<WatchlistRoute>()
                                        else -> false
                                    }
                                } == true

                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTopLevel(navController, dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = CinemaGold,
                                        selectedTextColor = CinemaGoldSoft,
                                        indicatorColor = CinemaCrimson.copy(alpha = 0.24f),
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        NavHost(
                            navController = navController,
                            startDestination = HomeRoute,
                            enterTransition = { fadeIn(animationSpec = tween(220)) },
                            exitTransition = { fadeOut(animationSpec = tween(180)) },
                            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
                            popExitTransition = { fadeOut(animationSpec = tween(180)) }
                        ) {
                            composable<HomeRoute> {
                                HomeScreen(
                                    movies = movies,
                                    savedIds = savedIds,
                                    watchHistory = watchHistory,
                                    selectedLanguage = selectedLanguage,
                                    catalogError = catalogError,
                                    onSelectLanguage = viewModel::selectLanguage,
                                    onMovieClick = { movie ->
                                        navController.navigate(MovieDetailsRoute(movie.id))
                                        UnityAdsManager.recordNavigationAndMaybeShowInterstitial(
                                            activity,
                                            "home_to_details"
                                        )
                                    },
                                    onPlayClick = { movie ->
                                        navController.navigate(VideoPlayerRoute(movie.id))
                                    },
                                    onToggleWatchlist = viewModel::toggleWatchlist,
                                    onOpenSearch = {
                                        navigateToTopLevel(navController, ExploreRoute)
                                    },
                                    onSeeLanguageCatalog = { lang ->
                                        viewModel.selectLanguage(lang)
                                        navigateToTopLevel(navController, ExploreRoute)
                                    },
                                    onSelectCategoryAndExplore = { category ->
                                        viewModel.selectCategory(category)
                                        navigateToTopLevel(navController, ExploreRoute)
                                    },
                                    onSelectCollection = { collection ->
                                        viewModel.selectLanguage(collection.filterLanguage)
                                        viewModel.selectCategory(collection.filterCategory)
                                        navigateToTopLevel(navController, ExploreRoute)
                                    }
                                )
                            }

                            composable<ExploreRoute> {
                                ExploreScreen(
                                    movies = exploreMovies,
                                    savedIds = savedIds,
                                    searchQuery = searchQuery,
                                    selectedLanguage = selectedLanguage,
                                    selectedCategory = selectedCategory,
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    onSelectLanguage = viewModel::selectLanguage,
                                    onSelectCategory = viewModel::selectCategory,
                                    onMovieClick = { movie ->
                                        navController.navigate(MovieDetailsRoute(movie.id))
                                        UnityAdsManager.recordNavigationAndMaybeShowInterstitial(
                                            activity,
                                            "explore_to_details"
                                        )
                                    },
                                    onToggleWatchlist = viewModel::toggleWatchlist,
                                    adFreeMinutes = adFreeMinutes,
                                    onWatchRewardedAd = launchRewardedPass
                                )
                            }

                            composable<WatchlistRoute> {
                                WatchlistScreen(
                                    watchlistState = watchlistState,
                                    onMovieClick = { movie ->
                                        navController.navigate(MovieDetailsRoute(movie.id))
                                    },
                                    onPlayClick = { movie ->
                                        navController.navigate(VideoPlayerRoute(movie.id))
                                    },
                                    onRemoveFromWatchlist = viewModel::toggleWatchlist,
                                    onExploreClick = {
                                        viewModel.selectLanguage(MovieLanguage.ALL)
                                        navigateToTopLevel(navController, ExploreRoute)
                                    }
                                )
                            }

                            composable<MovieDetailsRoute> { backStackEntry ->
                                val route = backStackEntry.toRoute<MovieDetailsRoute>()
                                val movie = viewModel.findMovieById(route.movieId) ?: movies.firstOrNull()
                                if (movie != null) {
                                    val resumeMs = watchHistory
                                        .firstOrNull { it.movieId == movie.id }
                                        ?.positionMs ?: 0L
                                    val related = movies.filter {
                                        it.id != movie.id &&
                                            (it.language == movie.language || it.category == movie.category)
                                    }.take(8)

                                    MovieDetailsScreen(
                                        movie = movie,
                                        isSaved = movie.id in savedIds,
                                        resumePositionMs = resumeMs,
                                        relatedMovies = related,
                                        savedIds = savedIds,
                                        onBackClick = { navController.popBackStack() },
                                        onPlayClick = { target ->
                                            navController.navigate(VideoPlayerRoute(target.id))
                                        },
                                        onToggleWatchlist = viewModel::toggleWatchlist,
                                        onRelatedMovieClick = { relatedMovie ->
                                            navController.navigate(MovieDetailsRoute(relatedMovie.id))
                                            UnityAdsManager.recordNavigationAndMaybeShowInterstitial(
                                                activity,
                                                "related_to_details"
                                            )
                                        },
                                        adFreeMinutes = adFreeMinutes,
                                        onWatchRewardedAd = launchRewardedPass
                                    )
                                }
                            }

                            composable<VideoPlayerRoute> { backStackEntry ->
                                val route = backStackEntry.toRoute<VideoPlayerRoute>()
                                val movie = viewModel.findMovieById(route.movieId) ?: movies.firstOrNull()
                                if (movie != null) {
                                    val resumeMs = watchHistory
                                        .firstOrNull { it.movieId == movie.id }
                                        ?.positionMs ?: 0L
                                    VideoPlayerScreen(
                                        movie = movie,
                                        initialPositionMs = resumeMs,
                                        onSaveProgress = { pos, dur ->
                                            viewModel.saveWatchProgress(movie, pos, dur)
                                        },
                                        onBackClick = {
                                            navController.popBackStack()
                                            UnityAdsManager.recordNavigationAndMaybeShowInterstitial(
                                                activity,
                                                "exit_player"
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating Animated Obsidian Glass Bottom Navigation Bar
                if (showChrome && !useRail) {
                    GlassBottomNavigationBar(
                        destinations = TopLevelDestinations,
                        isSelected = { dest ->
                            currentDestination?.hierarchy?.any {
                                when (dest.route) {
                                    is HomeRoute -> it.hasRoute<HomeRoute>()
                                    is ExploreRoute -> it.hasRoute<ExploreRoute>()
                                    is WatchlistRoute -> it.hasRoute<WatchlistRoute>()
                                    else -> false
                                }
                            } == true
                        },
                        onSelect = { dest -> navigateToTopLevel(navController, dest.route) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassBottomNavigationBar(
    destinations: List<BottomNavDestination>,
    isSelected: (BottomNavDestination) -> Boolean,
    onSelect: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        shape = RoundedCornerShape(26.dp),
        containerColor = ObsidianGlassStrong,
        shadowElevation = 14.dp,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 460.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            destinations.forEach { dest ->
                val selected = isSelected(dest)
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "nav_icon_scale_${dest.label}"
                )
                val tintColor by animateColorAsState(
                    targetValue = if (selected) CinemaGoldSoft else TextSecondary,
                    animationSpec = tween(200),
                    label = "nav_tint_${dest.label}"
                )
                val pillAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = tween(200),
                    label = "nav_pill_alpha_${dest.label}"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minWidth = 48.dp, minHeight = 52.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    CinemaCrimson.copy(alpha = 0.24f * pillAlpha),
                                    CinemaCrimsonBright.copy(alpha = 0.14f * pillAlpha)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onSelect(dest) }
                        .testTag(dest.testTag)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = dest.label,
                            tint = if (selected) CinemaGold else tintColor,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = tintColor
                            )
                            if (selected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(CinemaBrushes.goldMetallic)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
