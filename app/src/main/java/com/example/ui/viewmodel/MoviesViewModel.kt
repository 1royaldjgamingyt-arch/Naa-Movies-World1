package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LicensedCatalogDefaults
import com.example.data.model.Movie
import com.example.data.model.MovieCategory
import com.example.data.model.MovieLanguage
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchlistItem
import com.example.data.repository.LocalWatchlistRepository
import com.example.data.repository.MovieRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MoviesViewModel(
    movieRepository: MovieRepository,
    private val watchlistRepository: LocalWatchlistRepository
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow(MovieLanguage.ALL)
    val selectedLanguage: StateFlow<MovieLanguage> = _selectedLanguage.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MovieCategory.ALL)
    val selectedCategory: StateFlow<MovieCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    private val remoteCatalogState: StateFlow<UiState<List<Movie>>> = movieRepository
        .observePublishedMovies()
        .map<List<Movie>, UiState<List<Movie>>> { UiState.Success(it) }
        .catch { error ->
            val fsError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Error observing published movies", root)
            emit(UiState.Error(root.message ?: "Unable to sync cloud catalog"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val catalogMovies: StateFlow<List<Movie>> = remoteCatalogState
        .map { state ->
            val remote = (state as? UiState.Success)?.data.orEmpty()
            mergeWithDefaults(remote)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = LicensedCatalogDefaults.movies
        )

    val catalogError: StateFlow<String?> = remoteCatalogState
        .map { (it as? UiState.Error)?.message }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    val filteredExploreMovies: StateFlow<List<Movie>> = combine(
        catalogMovies,
        selectedLanguage,
        selectedCategory,
        searchQuery
    ) { movies, lang, cat, query ->
        val trimmed = query.trim().lowercase()
        movies.filter { movie ->
            val matchesLang = lang == MovieLanguage.ALL || movie.language.equals(lang.label, ignoreCase = true)
            val matchesCat = cat == MovieCategory.ALL ||
                movie.category.equals(cat.label, ignoreCase = true) ||
                movie.genres.any { it.equals(cat.label, ignoreCase = true) }
            val matchesQuery = trimmed.isEmpty() ||
                movie.title.lowercase().contains(trimmed) ||
                movie.description.lowercase().contains(trimmed) ||
                movie.language.lowercase().contains(trimmed) ||
                movie.category.lowercase().contains(trimmed) ||
                movie.genres.any { it.lowercase().contains(trimmed) }
            matchesLang && matchesCat && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = LicensedCatalogDefaults.movies
    )

    val watchlistState: StateFlow<UiState<List<WatchlistItem>>> = watchlistRepository
        .observeWatchlist()
        .map<List<WatchlistItem>, UiState<List<WatchlistItem>>> { items ->
            UiState.Success(items.sortedByDescending { it.addedAtMs })
        }
        .catch { error ->
            Log.w(TAG, "Error observing local watchlist", error)
            emit(UiState.Error(error.message ?: "Failed to load local watchlist"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val watchHistory: StateFlow<List<WatchHistoryItem>> = watchlistRepository
        .observeWatchHistory()
        .map { items -> items.sortedByDescending { it.updatedAtMs } }
        .catch { error ->
            Log.w(TAG, "Error observing local watch history", error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    fun selectLanguage(language: MovieLanguage) {
        _selectedLanguage.value = language
    }

    fun selectCategory(category: MovieCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun findMovieById(movieId: String): Movie? {
        return catalogMovies.value.firstOrNull { it.id == movieId }
    }

    fun isInWatchlist(movieId: String): Boolean {
        val items = (watchlistState.value as? UiState.Success)?.data.orEmpty()
        return items.any { it.movieId == movieId }
    }

    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            val saved = isInWatchlist(movie.id)
            val result = if (saved) {
                watchlistRepository.removeFromWatchlist(movie.id)
            } else {
                watchlistRepository.addToWatchlist(movie)
            }
            result.onSuccess {
                _uiEvents.tryEmit(
                    if (saved) "Removed \"${movie.title}\" from Watchlist"
                    else "Saved \"${movie.title}\" to Watchlist"
                )
            }.onFailure { err ->
                _uiEvents.tryEmit(err.localizedMessage ?: "Watchlist update failed")
            }
        }
    }

    fun saveWatchProgress(movie: Movie, positionMs: Long, durationMs: Long) {
        if (positionMs < 2_000L) return
        viewModelScope.launch {
            watchlistRepository.saveWatchProgress(movie, positionMs, durationMs)
        }
    }

    private fun mergeWithDefaults(remote: List<Movie>): List<Movie> {
        val remoteIds = remote.map { it.id }.toSet()
        val remainingDefaults = LicensedCatalogDefaults.movies.filterNot { it.id in remoteIds }
        return remote.sortedByDescending { it.updatedAt ?: it.createdAt } + remainingDefaults
    }

    private companion object {
        const val TAG = "MoviesViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
