package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
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
    val trailerUrl: String? = null,
    val error: String? = null
)

class DetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val apiKey = BuildConfig.TMDB_API_KEY

    fun findTrailer(movieId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = RetrofitInstance.api.getMovieVideos(movieId, apiKey)
                val trailer = findBestTrailer(response.results)
                val trailerUrl = trailer?.let { "https://www.youtube.com/watch?v=${it.key}" }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trailerUrl = trailerUrl
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load trailer: ${e.message}"
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

    private fun findBestTrailer(videos: List<Video>): Video? {
        return videos.firstOrNull { it.site == "YouTube" && it.type == "Trailer" && it.official } 
            ?: videos.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }
    }
}
