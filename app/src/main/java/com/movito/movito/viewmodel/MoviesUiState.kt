package com.movito.movito.viewmodel

import com.movito.movito.data.model.Movie
import androidx.lifecycle.ViewModel

/**
 * UI state representation for paginated movie lists.
 *
 * This data class holds all state necessary for displaying paginated [Movie]-[List]s,
 * including loading states, pagination states, movie data, and error handling.
 * It's designed to support incremental loading, pull-to-refresh, and error recovery.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @property isLoading Indicates if initial data is being loaded (first page)
 * @property isRefreshing Indicates if a pull-to-refresh operation is in progress
 * @property isLoadingMore Indicates if additional pages are being loaded (pagination)
 * @property movies [List] of [Movie]s currently loaded and displayed
 * @property error Error message [String], `null` if no error occurred
 *
 * @since fist appear in MoviesByGenreViewModel.kt file (8 Nov 2025), then moved to this file (15 Nov 2025)
 * @see MoviesByGenreViewModel for [ViewModel] using this state
 * @see Movie for the individual movie data model
 */
data class MoviesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null
)