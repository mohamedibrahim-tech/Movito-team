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
import com.movito.movito.LanguageManager
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.Composable
import android.widget.Toast

/**
 * [Immutable] UI state representation for the movie search screen.
 *
 * The [Immutable] annotation helps Compose optimize recompositions by
 * indicating that instances of this data class won't change after creation.
 *
 * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
 *
 * @property searchQuery Current search query text entered by user
 * @property movies [List] of [Movie]s matching the search query
 * @property isLoading Indicates if a search operation is in progress
 * @property error Error message if search failed, `null` otherwise
 * @property hasSearched Indicates if at least one search has been performed
 *
 * @since 12 Nov 2025
 */
@Immutable
data class SearchUiState(
    val searchQuery: String = "",
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false
)

// Keys for SavedStateHandle persistence
private const val SEARCH_QUERY_KEY = "search_query"

/**
 * Current language state flow for localized API requests.
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 */
private val currentLanguage = LanguageManager.currentLanguage

/**
 * [ViewModel] for managing movie search functionality and state.
 *
 * This [ViewModel] handles:
 * - Real-time search query state management
 * - TMDB API search operations with debouncing
 * - State persistence across configuration changes using [SavedStateHandle]
 * - Multi-language support for error messages
 * - Search history preservation across process death
 *
 * The [ViewModel] uses [SavedStateHandle] to preserve search state (query text)
 * across configuration changes and process death, ensuring seamless user experience.
 *
 * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
 *
 * @param savedStateHandle Container for saving and restoring search state
 *
 * @see SearchUiState for the UI state structure
 * @see searchMovies for the API endpoint
 *
 * @since 12 Nov 2025
 */
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle // Renamed for clarity
) : ViewModel() {

    /**
     * Internal mutable state flow for search UI state.
     *
     * Initialized with saved search query from [SavedStateHandle] if available.
     *
     * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
     *
     * @since 12 Nov 2025
     */
    private val _uiState = MutableStateFlow(
        SearchUiState(
            searchQuery = savedStateHandle.get<String>(SEARCH_QUERY_KEY) ?: ""
        )
    )

    /**
     * `public` immutable [StateFlow] exposing the current search UI state.
     *
     * Collect this flow in [Composable]s to reactively update UI with:
     * - Search query text
     * - Search results
     * - Loading states
     * - Error messages
     *
     * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
     *
     * @since 12 Nov 2025
     */
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /**
     * TMDB API key for authentication.
     *
     * Retrieved from [BuildConfig] during Gradle build.
     *
     * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
     *
     * @since 22 Nov 2025
     */
    private val apiKey = BuildConfig.TMDB_API_KEY

    /**
     * Updates the search query text in UI state and persists it to [SavedStateHandle].
     *
     * This method updates both the reactive UI state and persists the query
     * to [SavedStateHandle] to survive configuration changes and process death.
     *
     * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
     *
     * @param query The new search query text entered by the user
     *
     * @since 12 Nov 2025
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Persist the current query to survive process death
        savedStateHandle[SEARCH_QUERY_KEY] = query
    }

    /**
     * Executes a movie search using the current search query.
     *
     * This method:
     * 1. Trims and validates the search query
     * 2. Shows loading state during API request
     * 3. Calls TMDB search API with current language
     * 4. Updates UI state with results or error
     * 5. Tracks if a search has been performed
     *
     * Empty queries will clear previous results and reset the search flag.
     *
     * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
     *
     * @see searchMovies for the API implementation
     *
     * @since 12 Nov 2025
     */
    fun searchMovies() {
        val query = _uiState.value.searchQuery.trim()

        // Don't search if the query is empty
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasSearched = true) }

            try {
                val response = RetrofitInstance.api.searchMovies(
                    apiKey = apiKey,
                    query = query,
                    language = currentLanguage.value
                )
                val movies = response.results.orEmpty() // Handle null results safely

                _uiState.update { it.copy(isLoading = false, movies = movies) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        movies = emptyList(), // Clear results on error
                        error = if(currentLanguage.value == "ar") "تعذر تحميل الأفلام: ${e.message} خطأ عير معروف" else "Failed to fetch movies: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    /**
     * Clears any displayed error messages from the UI.
     *
     * Call this method after displaying an error to the user (e.g., after showing
     * a [Snackbar] or [Toast]) to reset the error state and prevent duplicate displays.
     *
     * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
     *
     * @since 22 Nov 2025
     */
    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }
}