package com.movito.movito.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.SavedStateHandle
import com.movito.movito.BuildConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class SearchUiState(
    val searchQuery: String = "",
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false
)

// Keys for SavedStateHandle
private const val SEARCH_QUERY_KEY = "search_query"

class SearchViewModel(
    private val savedStateHandle: SavedStateHandle // Renamed for clarity
) : ViewModel() {

    // Initialize state, reading the last saved query if available.
    private val _uiState = MutableStateFlow(
        SearchUiState(
            searchQuery = savedStateHandle.get<String>(SEARCH_QUERY_KEY) ?: ""
        )
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Using BuildConfig directly is fine for simple keys, but consider a Repository/DataSource for better abstraction.
    private val apiKey = BuildConfig.TMDB_API_KEY


    // Updates the search query text in the UI state AND saves it to handle.
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Save the current query to be restored after process death
        savedStateHandle[SEARCH_QUERY_KEY] = query
    }

    val searchQueryState: StateFlow<String> = _uiState
        .map { it.searchQuery }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Start collection when a subscriber is present
            initialValue = uiState.value.searchQuery // Use the current value from the main state
        )
    // Main function to trigger the movie search, now using the query stored in the state.
    fun searchMovies() {
        val query = _uiState.value.searchQuery.trim() // Get current query from state

        // Don't search if the query is empty.
        if (query.isBlank()) {
            // Clear previous results and error
            _uiState.update {
                it.copy(
                    movies = emptyList(),
                    error = null,
                    hasSearched = false // Reset search flag if query is cleared
                )
            }
            return
        }

        // Use viewModelScope to launch a coroutine.
        viewModelScope.launch {
            // Set loading state to true and mark that a search has been attempted.
            _uiState.update { it.copy(isLoading = true, error = null, hasSearched = true) }

            try {
                // Call the API.
                val response = RetrofitInstance.api.searchMovies(
                    apiKey = apiKey,
                    query = query // Use the current, trimmed query
                )
                val movies = response.results.orEmpty() // Use orEmpty() to handle null results safely

                // On success, update the state with the movie list.
                _uiState.update { it.copy(isLoading = false, movies = movies) }

            } catch (e: Exception) {
                // On failure, update the state with an error message.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        movies = emptyList(), // Clear results on error
                        error = "Failed to fetch movies: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    // Function to clear any displayed error messages from the UI.
    // NOTE: This is now a member function of the class, accessible from the UI.
    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }
}