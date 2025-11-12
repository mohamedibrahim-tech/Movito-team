package com.movito.movito.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.Movie
import com.movito.movito.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Represents all possible states for the Search screen.
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "", // The text entered in the search field
    val movies: List<Movie> = emptyList(), // The search results
    val error: String? = null,
    val hasSearched: Boolean = false // To determine if a search operation has actually been performed
)

/**
 * The ViewModel responsible for managing the Search screen data.
 */
class SearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Temporary fake data (to represent the database/API)
    private val mockMovies = listOf(
        Movie(1, "Cosmic Echoes", "2025", "2h 15m", R.drawable.poster_test),
        Movie(2, "Cyber City", "2024", "1h 55m", R.drawable.poster_test),
        Movie(3, "Ocean's Deep", "2023", "2h 05m", R.drawable.poster_test),
        Movie(4, "Mountain Peak", "2025", "2h 20m", R.drawable.poster_test),
        Movie(5, "Desert Runner", "2024", "1h 45m", R.drawable.poster_test),
        Movie(6, "Space Pirates", "2023", "2h 00m", R.drawable.poster_test),
        Movie(7, "Time Bender", "2025", "2h 30m", R.drawable.poster_test),
        Movie(8, "Alpha Centauri", "2024", "2h 10m", R.drawable.poster_test)
    )

    /**
     * Updates the search text immediately as the user types.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * A function responsible for fetching movie data matching the search text (currently in a fake manner).
     */
    fun searchMovies(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(movies = emptyList(), hasSearched = true, error = null) }
            return
        }

        viewModelScope.launch {
            // 1. Display the loading state
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    hasSearched = true // The search button was pressed
                )
            }

            // 2. Pretend to fetch data (1-second delay)
            delay(1000)

            // 3. Filter the dummy data based on the search text (Query)
            val results = mockMovies.filter { movie ->
                movie.title.contains(query, ignoreCase = true)
            }

            // 4. Display the filtered data
            _uiState.update {
                it.copy(
                    isLoading = false,
                    movies = results
                    // error = "Search API Failed" // (If you want to test the error appearance)
                )
            }
        }
    }
}