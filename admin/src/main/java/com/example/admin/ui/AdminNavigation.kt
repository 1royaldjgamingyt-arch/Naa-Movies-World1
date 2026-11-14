package com.example.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.admin.R
import com.example.admin.auth.attemptAdminAutoSignIn
import com.example.admin.auth.onAdminGoogleSignInClicked
import com.example.admin.auth.signOutAdmin
import com.example.admin.data.repository.AdminMovieRepository
import com.example.admin.ui.screens.AdminAccountScreen
import com.example.admin.ui.screens.AdminAuthScreen
import com.example.admin.ui.screens.AdminCategoriesScreen
import com.example.admin.ui.screens.AdminDashboardScreen
import com.example.admin.ui.screens.AdminMovieEditorScreen
import com.example.admin.ui.screens.AdminSplashScreen
import com.example.admin.ui.screens.AdminStreamPreviewScreen
import com.example.admin.ui.screens.AdminUnauthorizedScreen
import com.example.admin.ui.theme.CinemaCrimson
import com.example.admin.ui.theme.CinemaGold
import com.example.admin.ui.theme.ObsidianBlack
import com.example.admin.ui.theme.ObsidianSurface
import com.example.admin.ui.theme.TextPrimary
import com.example.admin.ui.theme.TextSecondary
import com.example.admin.ui.viewmodel.AdminAccessState
import com.example.admin.ui.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable

@Serializable
data object AdminDashboardRoute

@Serializable
data object AdminCategoriesRoute

@Serializable
data object AdminAccountRoute

@Serializable
data class AdminEditorRoute(val movieId: String? = null)

@Serializable
data class AdminStreamPreviewRoute(
    val title: String,
    val language: String,
    val qualityBadge: String,
    val streamUrl: String
)

private data class AdminNavDestination(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val AdminTopDestinations = listOf(
    AdminNavDestination(
        AdminDashboardRoute,
        "Catalog",
        Icons.Filled.Dashboard,
        Icons.Outlined.Dashboard,
        "admin_nav_catalog"
    ),
    AdminNavDestination(
        AdminCategoriesRoute,
        "Categories",
        Icons.Filled.Category,
        Icons.Outlined.Category,
        "admin_nav_categories"
    ),
    AdminNavDestination(
        AdminAccountRoute,
        "Studio",
        Icons.Filled.AdminPanelSettings,
        Icons.Outlined.AdminPanelSettings,
        "admin_nav_studio"
    )
)

@Composable
fun AdminNavigation() {
    var showSplash by remember { mutableStateOf(true) }
    if (showSplash) {
        AdminSplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    val viewModel: AdminViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val dbId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                AdminViewModel(AdminMovieRepository(db))
            }
        }
    )

    val accessState by viewModel.accessState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()

    var authLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var autoAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(accessState) {
        if (accessState is AdminAccessState.Unauthenticated && !autoAttempted) {
            autoAttempted = true
            attemptAdminAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    authError = null
                    authLoading = false
                },
                onUnauthenticated = {
                    authLoading = false
                },
                scope = coroutineScope
            )
        }
    }

    when (val state = accessState) {
        AdminAccessState.Checking -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CinemaGold, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Verifying Studio Administrator…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        AdminAccessState.Unauthenticated -> {
            AdminAuthScreen(
                isLoading = authLoading,
                errorMessage = authError,
                onGoogleSignInClick = {
                    authLoading = true
                    authError = null
                    onAdminGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            authLoading = false
                            authError = null
                        },
                        onAuthError = { msg ->
                            authLoading = false
                            authError = msg
                        },
                        onAuthCancelled = {
                            authLoading = false
                        },
                        scope = coroutineScope
                    )
                }
            )
        }

        is AdminAccessState.Unauthorized -> {
            AdminUnauthorizedScreen(
                signedInEmail = state.user.email,
                onSignOutClick = {
                    signOutAdmin(credentialManager, coroutineScope)
                }
            )
        }

        is AdminAccessState.VerifiedAdmin -> {
            AdminMainScaffold(
                adminUser = state.user,
                viewModel = viewModel,
                onSignOutClick = {
                    signOutAdmin(credentialManager, coroutineScope)
                }
            )
        }
    }
}

@Composable
private fun AdminMainScaffold(
    adminUser: FirebaseUser,
    viewModel: AdminViewModel,
    onSignOutClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current

    val allMovies by viewModel.allMovies.collectAsStateWithLifecycle()
    val filteredMovies by viewModel.filteredMovies.collectAsStateWithLifecycle()
    val catalogError by viewModel.catalogError.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val isEncodingPoster by viewModel.isEncodingPoster.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val showChrome = currentDestination?.let { dest ->
        dest.hasRoute<AdminDashboardRoute>() ||
            dest.hasRoute<AdminCategoriesRoute>() ||
            dest.hasRoute<AdminAccountRoute>()
    } ?: true

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            containerColor = ObsidianBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (currentDestination?.hasRoute<AdminDashboardRoute>() == true) {
                    FloatingActionButton(
                        onClick = { navController.navigate(AdminEditorRoute(null)) },
                        containerColor = CinemaGold,
                        contentColor = ObsidianBlack,
                        modifier = Modifier.testTag("admin_fab_add_movie")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Upload New Movie")
                    }
                }
            },
            bottomBar = {
                if (showChrome && !useRail) {
                    NavigationBar(
                        containerColor = ObsidianSurface,
                        contentColor = TextPrimary,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        AdminTopDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                when (dest.route) {
                                    is AdminDashboardRoute -> it.hasRoute<AdminDashboardRoute>()
                                    is AdminCategoriesRoute -> it.hasRoute<AdminCategoriesRoute>()
                                    is AdminAccountRoute -> it.hasRoute<AdminAccountRoute>()
                                    else -> false
                                }
                            } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaGold,
                                    selectedTextColor = TextPrimary,
                                    indicatorColor = CinemaGold.copy(alpha = 0.16f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showChrome && useRail) {
                    NavigationRail(
                        containerColor = ObsidianSurface,
                        contentColor = TextPrimary,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        AdminTopDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                when (dest.route) {
                                    is AdminDashboardRoute -> it.hasRoute<AdminDashboardRoute>()
                                    is AdminCategoriesRoute -> it.hasRoute<AdminCategoriesRoute>()
                                    is AdminAccountRoute -> it.hasRoute<AdminAccountRoute>()
                                    else -> false
                                }
                            } == true

                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CinemaGold,
                                    selectedTextColor = TextPrimary,
                                    indicatorColor = CinemaGold.copy(alpha = 0.16f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = AdminDashboardRoute
                    ) {
                        composable<AdminDashboardRoute> {
                            AdminDashboardScreen(
                                allMovies = allMovies,
                                filteredMovies = filteredMovies,
                                searchQuery = searchQuery,
                                selectedFilter = selectedFilter,
                                selectedLanguage = selectedLanguage,
                                catalogError = catalogError,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onSelectFilter = viewModel::selectFilter,
                                onSelectLanguage = viewModel::selectLanguage,
                                onAddMovieClick = { navController.navigate(AdminEditorRoute(null)) },
                                onEditMovieClick = { movie ->
                                    navController.navigate(AdminEditorRoute(movie.id))
                                },
                                onPreviewStreamClick = { movie ->
                                    navController.navigate(
                                        AdminStreamPreviewRoute(
                                            title = movie.title,
                                            language = movie.language,
                                            qualityBadge = movie.qualityBadge,
                                            streamUrl = movie.streamUrl
                                        )
                                    )
                                },
                                onTogglePublish = viewModel::togglePublishStatus,
                                onToggleTrending = viewModel::toggleTrending,
                                onToggleLatest = viewModel::toggleLatest,
                                onDeleteMovie = viewModel::deleteMovie
                            )
                        }

                        composable<AdminCategoriesRoute> {
                            AdminCategoriesScreen(
                                movies = allMovies,
                                onReassignCategory = viewModel::updateCategory,
                                onEditMovieClick = { movie ->
                                    navController.navigate(AdminEditorRoute(movie.id))
                                }
                            )
                        }

                        composable<AdminAccountRoute> {
                            AdminAccountScreen(
                                adminUser = adminUser,
                                movies = allMovies,
                                onSignOutClick = onSignOutClick
                            )
                        }

                        composable<AdminEditorRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<AdminEditorRoute>()
                            val existing = route.movieId?.let { viewModel.findMovieById(it) }
                            AdminMovieEditorScreen(
                                existingMovie = existing,
                                isSaving = isSaving,
                                isEncodingPoster = isEncodingPoster,
                                onPickPosterUri = { uri, onEncoded ->
                                    viewModel.encodePosterFromUri(context, uri, onEncoded)
                                },
                                onPreviewStreamUrl = { title, lang, quality, url ->
                                    navController.navigate(
                                        AdminStreamPreviewRoute(
                                            title = title,
                                            language = lang,
                                            qualityBadge = quality,
                                            streamUrl = url
                                        )
                                    )
                                },
                                onSaveMovie = { movie ->
                                    viewModel.saveMovie(movie) {
                                        navController.popBackStack()
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable<AdminStreamPreviewRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<AdminStreamPreviewRoute>()
                            AdminStreamPreviewScreen(
                                title = route.title,
                                language = route.language,
                                qualityBadge = route.qualityBadge,
                                streamUrl = route.streamUrl,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
