package com.movito.movito.viewmodel

import com.movito.movito.data.model.Movie

data class MoviesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null
)
