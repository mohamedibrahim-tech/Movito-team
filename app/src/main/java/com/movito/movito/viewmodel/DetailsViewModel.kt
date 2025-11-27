package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
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

/**
 * UI state representation for the Details screen.
 *
 * @property isLoading Indicates if a network request is in progress
 * @property genres List of available movie genres from TMDB
 * @property recommendedMovies List of movies recommended based on the current movie
 * @property trailerUrl YouTube URL for the movie trailer, null if not loaded
 * @property urlToShare URL prepared for sharing, null if not loaded
 * @property error Error message string, null if no error occurred
 */
data class DetailsUiState(
    val isLoading: Boolean = false,
    val genres: List<Genre> = emptyList(),
    val recommendedMovies: List<Movie> = emptyList(),
    val trailerUrl: String? = null,
    val urlToShare: String? = null,
    val error: String? = null
)

/**
 * ViewModel for managing movie details screen state and data.
 *
 * Handles:
 * - Loading movie genres from TMDB API
 * - Fetching movie recommendations
 * - Finding and preparing trailer URLs
 * - Managing share functionality
 * - Error state handling
 *
 * Uses TMDB API for all data operations and maintains UI state through [DetailsUiState].
 */
class DetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val apiKey = BuildConfig.TMDB_API_KEY

    // Load genres automatically when ViewModel is created
    init {
        loadGenres()
    }

    /**
     * Loads all available movie genres from TMDB API.
     *
     * Updates [DetailsUiState.genres] on success, or [DetailsUiState.error] on failure.
     * Sets [DetailsUiState.isLoading] during the network request.
     */
    fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = RetrofitInstance.api.getGenres(apiKey)
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
                        error = "Failed to load genres: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "An unexpected error occurred: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Loads movie recommendations based on the provided movie ID.
     *
     * @param movieId The TMDB movie ID to get recommendations for
     * Updates [DetailsUiState.recommendedMovies] on success, or [DetailsUiState.error] on failure.
     */
    fun loadRecommendations(movieId: Int){
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieRecommendations(movieId, apiKey)
                _uiState.update { it.copy(recommendedMovies = response.results) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load Recommendations: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "An unexpected error occurred: ${e.message}")
                }
            }
        }
    }

    /**
     * Finds the best available trailer for a movie and prepares it for playback.
     *
     * @param movieId The TMDB movie ID to find trailer for
     * Sets [DetailsUiState.isLoading] during the search.
     * On success, sets [DetailsUiState.trailerUrl] with YouTube URL.
     * On failure, sets [DetailsUiState.error] with appropriate message.
     */
    fun findTrailer(movieId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = RetrofitInstance.api.getMovieVideos(movieId, apiKey)
                val trailer = findBestTrailer(response.results)
                val url = trailer?.key?.let { "https://www.youtube.com/watch?v=$it" }

                if (url != null) {
                    _uiState.update {
                        it.copy(isLoading = false, trailerUrl = url)
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "No Trailer Found.")
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load trailer: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "An unexpected error occurred: ${e.message}")
                }
            }
        }
    }

    /**
     * Prepares a shareable URL for the movie trailer.
     *
     * @param movieId The TMDB movie ID to get share URL for
     * On success, sets [DetailsUiState.urlToShare] with YouTube URL.
     * On failure, sets [DetailsUiState.error] with appropriate message.
     */
    fun prepareShareUrl(movieId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieVideos(movieId, apiKey)
                val trailer = findBestTrailer(response.results)
                val url = trailer?.key?.let { "https://www.youtube.com/watch?v=$it" }

                if (url != null) {
                    _uiState.update { it.copy(urlToShare = url) }
                } else {
                    _uiState.update { it.copy(error = "No Trailer Found.") }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(error = "Failed to load trailer: ${e.message}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "An unexpected error occurred: ${e.message}") }
            }
        }
    }

    // State cleanup methods

    /**
     * Clears the trailer URL after it has been launched.
     * Called when the trailer player is opened to reset the state.
     */
    fun onTrailerLaunched() {
        _uiState.update { it.copy(trailerUrl = null) }
    }

    /**
     * Clears the share URL after it has been shared.
     * Called when the share intent is completed to reset the state.
     */
    fun onUrlShared() {
        _uiState.update { it.copy(urlToShare = null) }
    }

    /**
     * Clears the error message after it has been shown to the user.
     * Called when an error toast/snackbar is displayed to reset the state.
     */
    fun onToastShown() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Finds the best trailer from a list of videos using a priority-based algorithm.
     *
     * Priority order:
     * 1. Official Trailer on YouTube
     * 2. Any Trailer on YouTube
     * 3. Official Teaser on YouTube
     * 4. Any Teaser on YouTube
     * 5. Any Official video on YouTube
     * 6. Any video on YouTube
     *
     * @param videos List of videos from TMDB API
     * @return The best available trailer video, or null if no suitable video found
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