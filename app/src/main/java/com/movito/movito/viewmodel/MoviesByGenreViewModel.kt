package com.movito.movito.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
import com.movito.movito.LanguageManager
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import com.movito.movito.data.source.remote.TmdbApi
import androidx.compose.runtime.Composable
import com.movito.movito.data.model.Movie

/**
 * [ViewModel] for managing movies filtered by a specific genre.
 *
 * This [ViewModel] handles:
 * - Paginated loading of movies by genre from TMDB API
 * - Pull-to-refresh functionality
 * - Infinite scrolling with load more capability
 * - Duplicate movie filtering to prevent UI issues
 * - Multi-language support for error messages
 *
 * The [ViewModel] uses [SavedStateHandle] to retrieve the genre ID passed via navigation
 * and maintains pagination state across configuration changes.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @param savedStateHandle Container for saving and restoring state across process death
 *
 * @see MoviesUiState for the UI state structure
 * @see TmdbApi.discoverMoviesByGenre for the API endpoint
 *
 * @since first appear with name: "HomeViewModel" (8 Nov 2025), then changed to "MoviesByGenreViewModel" (14 Nov 2025)
 */
class MoviesByGenreViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    /**
     * Internal mutable state flow for movies UI state.
     *
     * Initialized with default [MoviesUiState] values.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 8 Nov 2025
     */
    private val _uiState = MutableStateFlow(MoviesUiState())

    /**
     * Public immutable StateFlow exposing the current movies UI state.
     *
     * Collect this flow in [Composable]s to reactively update UI with:
     * - [Movie] [List]s
     * - Loading states (initial, refresh, load more)
     * - Error messages
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 8 Nov 2025
     */
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    /**
     * TMDB API key for authentication.
     *
     * Retrieved from [BuildConfig] during Gradle build.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 13 Nov 2025
     */
    private val apiKey = BuildConfig.TMDB_API_KEY

    /**
     * Current language state flow for localized API requests.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    private val currentLanguage = LanguageManager.currentLanguage

    /**
     * Current page number for pagination.
     *
     * Starts at `1` and increments after successful page loads.
     * Resets to `1` when refreshing.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     */
    private var currentPage = 1

    /**
     * Genre ID passed via navigation arguments.
     *
     * Retrieved from [SavedStateHandle] and required for all API calls.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @throws IllegalStateException if genreId is not provided.
     *
     * @since 14 Nov 2025
     */
    private val genreId: Int = savedStateHandle.get<Int>("genreId")!!

    /**
     * Localized generic error message for unexpected failures.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param errorMsg Original error message from exception
     * @return Localized error message in current language
     *
     * @since 1 Dec 2025
     */
    private fun unexpectedErrorMsg(errorMsg: String?) = if (LanguageManager.currentLanguage.value == "ar") "حدث خطأ غير متوقع: $errorMsg" else "An unexpected error occurred: $errorMsg"

    /**
     * Initializes the [ViewModel] and triggers initial movie loading.
     *
     * Automatically calls [loadMovies] with `isLoading = true` to load
     * the first page of movies when the [ViewModel] is created.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 8 Nov 2025
     */
    init {
        loadMovies(isLoading = true)
    }

    /**
     * Loads movies from TMDB API with pagination support.
     *
     * This method handles both initial loading and pull-to-refresh scenarios:
     * - Initial load: Shows loading indicator
     * - Refresh: Shows refresh indicator and resets pagination
     *
     * Features:
     * - Prevents duplicate movie entries by filtering existing IDs
     * - Updates pagination counter on successful loads
     * - Handles network and general exceptions with localized errors
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @param isLoading Set to true for initial loading (shows loading indicator)
     * @param isRefreshing Set to true for pull-to-refresh (shows refresh indicator)
     *
     * @since 8 Nov 2025
     *
     * @see loadMoreMovies for paginated loading of additional pages
     */
    fun loadMovies(isLoading: Boolean = false, isRefreshing: Boolean = false) {
        if (isRefreshing) {
            currentPage = 1
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    error = null
                )
            }

            try {
                val response = RetrofitInstance.api.discoverMoviesByGenre(
                    apiKey = apiKey,
                    page = currentPage,
                    genreId = genreId,
                    language = currentLanguage.value
                )
                _uiState.update { currentState ->
                    val currentMovies = if (isRefreshing) emptyList() else currentState.movies

                    // Filter to prevent duplicate movies and app crashes
                    val existingMovieIds = currentMovies.map { it.id }.toSet()
                    val newMovies = response.results.filter { it.id !in existingMovieIds }

                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        movies = currentMovies + newMovies
                    )
                }
                if (response.results.isNotEmpty()) {
                    currentPage++
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Failed to load movies: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = unexpectedErrorMsg(e.message)
                    )
                }
            }
        }
    }

    /**
     * Loads additional pages of movies for infinite scrolling.
     *
     * This method implements pagination by loading the next page of movies
     * when the user scrolls to the bottom of the list. It prevents:
     * - Concurrent loading operations
     * - Loading when already loading or refreshing
     * - Loading when there's already an error
     *
     * Features:
     * - Shows loading more indicator
     * - Prevents duplicate movie entries
     * - Updates pagination on success
     * - Handles errors with localized messages
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     */
    fun loadMoreMovies() {
        // Prevent multiple concurrent load operations
        if (_uiState.value.isLoading || _uiState.value.isRefreshing || _uiState.value.isLoadingMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            try {
                val response = RetrofitInstance.api.discoverMoviesByGenre(
                    apiKey = apiKey,
                    page = currentPage,
                    genreId = genreId,
                    language = currentLanguage.value
                )
                _uiState.update { currentState ->
                    // Filter to prevent duplicate movies and app crashes
                    val existingMovieIds = currentState.movies.map { it.id }.toSet()
                    val newMovies = response.results.filter { it.id !in existingMovieIds }

                    currentState.copy(
                        isLoadingMore = false,
                        movies = currentState.movies + newMovies
                    )
                }
                if (response.results.isNotEmpty()) {
                    currentPage++
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = if (currentLanguage.value == "ar") "فشل تحميل المزيد من الأفلام ${e.message}" else "Failed to load more movies: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = unexpectedErrorMsg(e.message)
                    )
                }
            }
        }
    }
}