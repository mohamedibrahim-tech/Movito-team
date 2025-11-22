package com.movito.movito.viewmodel

import android.R.attr.apiKey
import android.R.attr.query
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.data.model.Movie
import com.movito.movito.data.model.MovieRepository
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.SavedStateHandle
import com.movito.movito.BuildConfig

@Immutable
data class SearchUiState(
    val searchQuery: String = "",
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false // Track if a search has been attempted
)

class SearchViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {


    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // The ViewModel creates and holds its own repository instance.
    private val movieRepository = MovieRepository()


    private val apiKey = BuildConfig.TMDB_API_KEY

    private val query: String = savedStateHandle.get<String>("query")!!


    // Updates the search query text in the UI state.
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Main function to trigger the movie search.
    fun searchMovies(query: String) {
        // Don't search if the query is empty.
        if (query.isBlank()) {
            // Clear previous results
            _uiState.update { it.copy(movies = emptyList(), hasSearched = false) }
            return
        }

        // Use viewModelScope to launch a coroutine that is automatically cancelled
        // when the ViewModel is cleared.
        viewModelScope.launch {
            // Set loading state to true and mark that a search has been attempted.
            _uiState.update { it.copy(isLoading = true, hasSearched = true) }
            try {
                // Call the repository to get movies from the API.
                val response = RetrofitInstance.api.searchMovies(
                    apiKey = apiKey,
                    query = query
                )
                val movies = response.results
                // On success, update the state with the movie list and set loading to false.
                _uiState.update { it.copy(isLoading = false, movies = movies) }
            } catch (e: Exception) {
                // On failure, update the state with an error message and set loading to false.
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to fetch movies: ${e.message}")
                }
            }
        }



        // A function to clear any displayed error messages from the UI.
        fun errorShown() {
            _uiState.update { it.copy(error = null) }
        }


    }
}