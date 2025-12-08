package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
import com.movito.movito.LanguageManager
import com.movito.movito.data.model.Genre
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.RetrofitInstance
import com.movito.movito.data.source.remote.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import com.movito.movito.data.source.remote.TmdbApi

/**
 * UI state representation for the movie details screen.
 *
 * This comprehensive state class holds all data needed to display movie details,
 * including genres, recommendations, trailer information, and error states.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @property isLoading Indicates if a network request is in progress
 * @property genres List of available movie genres from TMDB
 * @property recommendedMovies List of movies recommended based on the current movie
 * @property trailerUrl YouTube URL for the movie trailer, null if not loaded
 * @property urlToShare URL prepared for sharing, null if not loaded
 * @property genreError Error message for genre loading failures
 * @property trailerError Error message for trailer loading failures
 * @property recommendationsError Error message for recommendations loading failures
 *
 * @since 14 Nov 2025
 */
data class DetailsUiState(
    val isLoading: Boolean = false,
    val genres: List<Genre> = emptyList(),
    val recommendedMovies: List<Movie> = emptyList(),
    val trailerUrl: String? = null,
    val urlToShare: String? = null,
    val genreError: String? = null,
    val trailerError: String? = null,
    val recommendationsError: String? = null,
)

/**
 * [ViewModel] for managing movie details screen state and data.
 *
 * This [ViewModel] handles multiple data sources for a comprehensive movie details view:
 * - Loading movie genres from TMDB API
 * - Fetching movie recommendations based on similarity
 * - Finding and preparing trailer URLs from YouTube
 * - Managing share functionality for trailers
 * - Comprehensive error state handling per data type
 *
 * The [ViewModel] uses TMDB API for all data operations and maintains UI state
 * through [DetailsUiState]. It's designed to be extended for specific use cases.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @see Movie for the primary data model
 * @see Genre for genre information
 * @see Video for trailer/video data
 *
 * @since 14 Nov 2025
 */
open class DetailsViewModel : ViewModel() {

    /**
     * Internal mutable state flow for details UI state.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     */
    private val _uiState = MutableStateFlow(DetailsUiState())

    /**
     * `public` immutable [StateFlow] exposing the current details UI state.
     *
     * Collect this flow to reactively update UI with movie details, trailers,
     * recommendations, and error states.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     */
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    /**
     * TMDB API key for authentication.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     */
    private val apiKey = BuildConfig.TMDB_API_KEY

    /**
     * Current language state for localized API requests.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    private val currentLanguage = LanguageManager.currentLanguage

    /**
     * Initializes the ViewModel and automatically loads genres.
     *
     * Genres are loaded immediately as they're needed for displaying
     * movie genre names throughout the details screen.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     */
    init {
        loadGenres()
    }

    /**
     * Localized message for when no trailer is found.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @return Localized `"No Trailer Found"` message
     *
     * @since 1 Dec 2025
     */
    private fun noTrailerFoundMsg() = if (LanguageManager.currentLanguage.value == "ar") "لم يتم العثور على مقطع دعائي." else "No Trailer Found."

    /**
     * Localized error message for trailer loading failures.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param errorMsg Original error message
     * @return Localized error message
     *
     * @since 1 Dec 2025
     */
    private fun falidToLoadTrailerMsg(errorMsg: String?) = if (LanguageManager.currentLanguage.value == "ar") "تعذر تحميل المقطع الدعائي: $errorMsg" else "Failed to load trailer: $errorMsg"

    /**
     * Localized generic error message for unexpected failures.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param errorMsg Original error message
     * @return Localized error message
     *
     * @since 1 Dec 2025
     */
    private fun unexpectedErrorMsg(errorMsg: String?) = if (LanguageManager.currentLanguage.value == "ar") "حدث خطأ غير متوقع: $errorMsg" else "An unexpected error occurred: $errorMsg"

    /**
     * Loads all available movie genres from TMDB API.
     *
     * This method fetches the complete list of movie genres with localized names.
     * Genres are used to display movie categories throughout the details screen.
     *
     * State updates:
     * - Sets [DetailsUiState.isLoading] during request
     * - Updates [DetailsUiState.genres] on success
     * - Sets [DetailsUiState.genreError] on failure
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 27 Nov 2025
     *
     * @see TmdbApi.getGenres for API implementation
     */
    fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, genreError = null) }

            try {
                val response = RetrofitInstance.api.getGenres(apiKey, currentLanguage.value)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        genres = response.genres
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        genreError = if(currentLanguage.value == "ar") "تعذر تحميل الفئات: ${e.message}" else "Failed to load genres: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        genreError = unexpectedErrorMsg(e.message)
                    )
                }
            }
        }
    }

    /**
     * Loads movie recommendations based on the provided movie ID.
     *
     * This method fetches movies that are similar to the specified movie
     * using TMDB's recommendation algorithm.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param movieId The TMDB movie ID to get recommendations for
     *
     * State updates:
     * - Updates [DetailsUiState.recommendedMovies] on success
     * - Sets [DetailsUiState.recommendationsError] on failure
     *
     * @since 24 Nov 2025
     */
    fun loadRecommendations(movieId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieRecommendations(movieId, apiKey, currentLanguage.value)
                _uiState.update { it.copy(recommendedMovies = response.results) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, recommendationsError = if (currentLanguage.value == "ar") "تعذر تحميل الإتراحات: ${e.message}" else "Failed to load Recommendations: ${e.message}" )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, recommendationsError = unexpectedErrorMsg(e.message))
                }
            }
        }
    }

    /**
     * Finds the best available trailer for a movie and prepares it for playback.
     *
     * This method searches for available videos and selects the most appropriate
     * trailer based on a priority algorithm (official trailers first).
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @param movieId The TMDB movie ID to find trailer for
     *
     * State updates:
     * - Sets [DetailsUiState.isLoading] during search
     * - Sets [DetailsUiState.trailerUrl] with YouTube URL on success
     * - Sets [DetailsUiState.trailerError] on failure or if no trailer found
     *
     * @since 14 Nov 2025
     *
     * @see findBestTrailer for trailer selection algorithm
     */
    open fun findTrailer(movieId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, trailerError = null) }

            try {
                val response = RetrofitInstance.api.getMovieVideos(movieId, apiKey, currentLanguage.value)
                val trailer = findBestTrailer(response.results)
                val url = trailer?.key?.let { "https://www.youtube.com/watch?v=$it" }

                if (url != null) {
                    _uiState.update {
                        it.copy(isLoading = false, trailerUrl = url)
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, trailerError = noTrailerFoundMsg())
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, trailerError = falidToLoadTrailerMsg(e.message))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, trailerError = unexpectedErrorMsg(e.message))
                }
            }
        }
    }

    /**
     * Prepares a shareable URL for the movie trailer.
     *
     * This method finds the best trailer and creates a shareable URL
     * that can be used with Android's share intent.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @param movieId The TMDB movie ID to get share URL for
     *
     * State updates:
     * - Sets [DetailsUiState.urlToShare] with YouTube URL on success
     * - Sets [DetailsUiState.trailerError] on failure
     *
     * @since 16 Nov 2025
     */
    open fun prepareShareUrl(movieId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieVideos(movieId, apiKey, currentLanguage.value)
                val trailer = findBestTrailer(response.results)

                val url = trailer?.key?.let { "https://www.youtube.com/watch?v=$it" }

                if (url != null) {
                    _uiState.update { it.copy(urlToShare = url) }
                } else {
                    _uiState.update { it.copy(trailerError = noTrailerFoundMsg()) }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(trailerError = falidToLoadTrailerMsg(e.message)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(trailerError = falidToLoadTrailerMsg(e.message)) }
            }
        }
    }

    // State cleanup methods

    /**
     * Clears the trailer URL after it has been launched.
     *
     * Call this method when the trailer player is opened to reset the state
     * and prevent duplicate launches.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 15 Nov 2025
     */
    fun onTrailerLaunched() {
        _uiState.update { it.copy(trailerUrl = null) }
    }

    /**
     * Clears the share URL after it has been shared.
     *
     * Call this method when the share intent is completed to reset the state.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 15 Nov 2025
     */
    fun onUrlShared() {
        _uiState.update { it.copy(urlToShare = null) }
    }

    /**
     * Clears the error message after it has been shown to the user.
     *
     * Call this method when an error toast/snackbar is displayed to reset
     * the error state and prevent duplicate error displays.
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 16 Nov 2025
     */
    fun onTrailerToastShown() {
        _uiState.update { it.copy(trailerError = null) }
    }

    /**
     * Finds the best trailer from a list of videos using a priority-based algorithm.
     *
     * Priority order (highest to lowest):
     * 1. Official Trailer on YouTube
     * 2. Any Trailer on YouTube
     * 3. Official Teaser on YouTube
     * 4. Any Teaser on YouTube
     * 5. Any Official video on YouTube
     * 6. Any video on YouTube
     *
     * This algorithm ensures the best possible trailer is selected for playback.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     *
     * @param videos [List] of [Video]s from TMDB API
     * @return The best available trailer video, or `null` if no suitable video found
     */
    private fun findBestTrailer(videos: List<Video>): Video? {
        val youtubeVideos = videos.filter { it.site == "YouTube" }

        return youtubeVideos.firstOrNull { it.type == "Trailer" && it.official }
            ?: youtubeVideos.firstOrNull { it.type == "Trailer" }
            ?: youtubeVideos.firstOrNull { it.type == "Teaser" && it.official }
            ?: youtubeVideos.firstOrNull { it.type == "Teaser" }
            ?: youtubeVideos.firstOrNull { it.official }
            ?: youtubeVideos.firstOrNull()
    }
}