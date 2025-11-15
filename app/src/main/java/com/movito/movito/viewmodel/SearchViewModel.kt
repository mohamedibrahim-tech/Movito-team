package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import com.movito.movito.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false
)

class SearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchMovies(query: String) {
        // TODO: Implement actual search logic using TMDB API
    }
}