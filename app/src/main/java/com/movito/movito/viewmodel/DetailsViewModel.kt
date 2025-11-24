package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.RetrofitInstance
import com.movito.movito.data.source.remote.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class DetailsUiState(
    val isLoading: Boolean = false,
    val recommendedMovies: List<Movie> = emptyList(), // Recommendations for similar movies
    val trailerUrl: String? = null, // For playing the trailer
    val urlToShare: String? = null, // For the share intent
    val error: String? = null
)

class DetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val apiKey = BuildConfig.TMDB_API_KEY

    fun loadRecommendations(movieId: Int){
        viewModelScope.launch {
            val response = RetrofitInstance.api.getMovieRecommendations(movieId, apiKey)
            _uiState.update {it.copy(recommendedMovies = response.results) }
        }
    }

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

    fun onTrailerLaunched() {
        _uiState.update { it.copy(trailerUrl = null) }
    }

    fun onUrlShared() {
        _uiState.update { it.copy(urlToShare = null) }
    }

    fun onToastShown() {
        _uiState.update { it.copy(error = null) }
    }

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