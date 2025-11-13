package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.data.model.Movie
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val movies: List<Movie> = emptyList(), // ده بيستخدم Movie data class
    val error: String? = null,
    val hasSearched: Boolean = false
)

class SearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val mockMovies = listOf(
        Movie(1, "Dune: Part One", "2021", "2h 35m", "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg"),
        Movie(2, "Dune: Part Two", "2024", "2h 46m", "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05ESUPt.jpg"),
        Movie(3, "Cosmic Echoes", "2023", "2h 05m", "https://image.tmdb.org/t/p/w500/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg")
    )

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchMovies(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(movies = emptyList(), hasSearched = true, error = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    hasSearched = true
                )
            }

            delay(1000)

            val results = mockMovies.filter { movie ->
                movie.title.contains(query, ignoreCase = true)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    movies = results
                )
            }
        }
    }
}