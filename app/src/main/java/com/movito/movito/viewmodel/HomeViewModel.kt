package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException


data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val apiKey = BuildConfig.TMDB_API_KEY

    init {
        loadMovies(isLoading = true)
    }

    fun loadMovies(isLoading: Boolean = false, isRefreshing: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    error = null
                )
            }

            try {
                val response = RetrofitInstance.api.getPopularMovies(apiKey)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        movies = response.results
                    )
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
                        error = "An unexpected error occurred: ${e.message}"
                    )
                }
            }
        }
    }
}
