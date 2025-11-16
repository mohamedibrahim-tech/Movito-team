package com.movito.movito.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class MoviesByGenreViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private val apiKey = BuildConfig.TMDB_API_KEY
    private var currentPage = 1
    private val genreId: Int = savedStateHandle.get<Int>("genreId")!!

    init {
        loadMovies(isLoading = true)
    }

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
                    genreId = genreId
                )
                _uiState.update {
                    val currentMovies = if (isRefreshing) emptyList() else it.movies
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        movies = currentMovies + response.results
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
                        error = "An unexpected error occurred: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadMoreMovies() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing || _uiState.value.isLoadingMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            try {
                val response = RetrofitInstance.api.discoverMoviesByGenre(
                    apiKey = apiKey,
                    page = currentPage,
                    genreId = genreId
                )
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        movies = it.movies + response.results
                    )
                }
                if (response.results.isNotEmpty()) {
                    currentPage++
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = "Failed to load more movies: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = "An unexpected error occurred: ${e.message}"
                    )
                }
            }
        }
    }
}