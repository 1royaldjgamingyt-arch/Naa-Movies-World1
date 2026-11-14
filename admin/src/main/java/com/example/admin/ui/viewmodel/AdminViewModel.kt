package com.example.admin.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin.auth.adminAuthStateFlow
import com.example.admin.data.model.AdminCatalogFilter
import com.example.admin.data.model.Movie
import com.example.admin.data.model.MovieLanguage
import com.example.admin.data.repository.AdminMovieRepository
import com.example.admin.util.PosterImageEncoder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AdminAccessState {
    data object Checking : AdminAccessState
    data object Unauthenticated : AdminAccessState
    data class VerifiedAdmin(val user: FirebaseUser) : AdminAccessState
    data class Unauthorized(val user: FirebaseUser) : AdminAccessState
}

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModel(
    private val repository: AdminMovieRepository
) : ViewModel() {

    private val authState = Firebase.auth.adminAuthStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, Firebase.auth.currentUser)

    val accessState: StateFlow<AdminAccessState> = authState
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(AdminAccessState.Unauthenticated)
            } else {
                flow<AdminAccessState> {
                    emit(AdminAccessState.Checking)
                    val isAdmin = repository.checkIsAdmin()
                    if (isAdmin) {
                        emit(AdminAccessState.VerifiedAdmin(user))
                    } else {
                        emit(AdminAccessState.Unauthorized(user))
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminAccessState.Checking)

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError: StateFlow<String?> = _catalogError.asStateFlow()

    val allMovies: StateFlow<List<Movie>> = accessState
        .flatMapLatest { state ->
            if (state is AdminAccessState.VerifiedAdmin) {
                _catalogError.value = null
                repository.observeAllMovies()
                    .map { list ->
                        list.sortedWith(
                            compareByDescending<Movie> { it.trending }
                                .thenByDescending { it.latest }
                                .thenByDescending { it.releaseYear }
                                .thenBy { it.title }
                        )
                    }
                    .catch { err ->
                        _catalogError.value = err.localizedMessage ?: "Failed to load studio catalog"
                        emit(emptyList())
                    }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(AdminCatalogFilter.ALL)
    val selectedFilter: StateFlow<AdminCatalogFilter> = _selectedFilter.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(MovieLanguage.ALL)
    val selectedLanguage: StateFlow<MovieLanguage> = _selectedLanguage.asStateFlow()

    val filteredMovies: StateFlow<List<Movie>> = combine(
        allMovies,
        _searchQuery,
        _selectedFilter,
        _selectedLanguage
    ) { movies, query, filter, language ->
        val q = query.trim().lowercase()
        movies.filter { movie ->
            val matchesFilter = when (filter) {
                AdminCatalogFilter.ALL -> true
                AdminCatalogFilter.PUBLISHED -> movie.status == "published"
                AdminCatalogFilter.DRAFTS -> movie.status != "published"
                AdminCatalogFilter.TRENDING -> movie.trending
                AdminCatalogFilter.LATEST -> movie.latest
            }
            val matchesLanguage = language == MovieLanguage.ALL ||
                movie.language.equals(language.label, ignoreCase = true)
            val matchesQuery = q.isBlank() ||
                movie.title.lowercase().contains(q) ||
                movie.category.lowercase().contains(q) ||
                movie.language.lowercase().contains(q) ||
                movie.genres.any { it.lowercase().contains(q) }
            matchesFilter && matchesLanguage && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isEncodingPoster = MutableStateFlow(false)
    val isEncodingPoster: StateFlow<Boolean> = _isEncodingPoster.asStateFlow()

    private val _uiEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFilter(filter: AdminCatalogFilter) {
        _selectedFilter.value = filter
    }

    fun selectLanguage(language: MovieLanguage) {
        _selectedLanguage.value = language
    }

    fun findMovieById(id: String): Movie? = allMovies.value.firstOrNull { it.id == id }

    fun encodePosterFromUri(
        context: Context,
        uri: Uri,
        onEncoded: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isEncodingPoster.value = true
            PosterImageEncoder.encodePosterToDataUri(context, uri)
                .onSuccess { dataUri ->
                    onEncoded(dataUri)
                    _uiEvents.tryEmit("Poster optimized and ready for catalog")
                }
                .onFailure { err ->
                    _uiEvents.tryEmit(err.localizedMessage ?: "Failed to process selected poster")
                }
            _isEncodingPoster.value = false
        }
    }

    fun saveMovie(movie: Movie, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val wasEditing = movie.id.isNotBlank()
            repository.saveMovie(movie)
                .onSuccess {
                    _uiEvents.tryEmit(
                        if (wasEditing) "Updated \"${movie.title}\""
                        else "Published \"${movie.title}\" to catalog"
                    )
                    onSuccess()
                }
                .onFailure { err ->
                    _uiEvents.tryEmit(err.localizedMessage ?: "Failed to save movie")
                }
            _isSaving.value = false
        }
    }

    fun togglePublishStatus(movie: Movie) {
        viewModelScope.launch {
            val nextStatus = if (movie.status == "published") "draft" else "published"
            repository.updateQuickFlags(movie = movie, status = nextStatus)
                .onSuccess {
                    val label = if (nextStatus == "published") "Published" else "Moved to Drafts"
                    _uiEvents.tryEmit("$label: ${movie.title}")
                }
                .onFailure { err ->
                    _uiEvents.tryEmit(err.localizedMessage ?: "Status update failed")
                }
        }
    }

    fun toggleTrending(movie: Movie) {
        viewModelScope.launch {
            val next = !movie.trending
            repository.updateQuickFlags(movie = movie, trending = next)
                .onSuccess {
                    _uiEvents.tryEmit(
                        if (next) "Marked ${movie.title} as Trending"
                        else "Removed ${movie.title} from Trending"
                    )
                }
                .onFailure { err ->
                    _uiEvents.tryEmit(err.localizedMessage ?: "Failed to update Trending flag")
                }
        }
    }

    fun toggleLatest(movie: Movie) {
        viewModelScope.launch {
            val next = !movie.latest
            repository.updateQuickFlags(movie = movie, latest = next)
                .onSuccess {
                    _uiEvents.tryEmit(
                        if (next) "Marked ${movie.title} as Latest"
                        else "Removed ${movie.title} from Latest"
                    )
                }
                .onFailure { err ->
                    _uiEvents.tryEmit(err.localizedMessage ?: "Failed to update Latest flag")
                }
        }
    }

    fun toggleFeatured(movie: Movie) {
        viewModelScope.launch {
            val next = !movie.featured
            repository.updateQuickFlags(movie = movie, featured = next)
                .onSuccess {
                    _uiEvents.tryEmit(
                        if (next) "Featured ${movie.title} on Hero Spotlight"
                        else "Removed ${movie.title} from Hero Spotlight"
                    )
                }
                .onFailure { err ->
                    _uiEvents.tryEmit(err.localizedMessage ?: "Failed to update Featured flag")
                }
        }
    }

    fun updateCategory(movie: Movie, newCategory: String) {
        viewModelScope.launch {
            repository.updateQuickFlags(movie = movie, category = newCategory)
                .onSuccess {
                    _uiEvents.tryEmit("Moved \"${movie.title}\" to $newCategory")
                }
                .onFailure { err ->
                    _uiEvents.tryEmit(err.localizedMessage ?: "Category update failed")
                }
        }
    }

    fun deleteMovie(movie: Movie) {
        viewModelScope.launch {
            repository.deleteMovie(movie.id)
                .onSuccess {
                    _uiEvents.tryEmit("Deleted \"${movie.title}\" from catalog")
                }
                .onFailure { err ->
                    _uiEvents.tryEmit(err.localizedMessage ?: "Delete failed")
                }
        }
    }
}
