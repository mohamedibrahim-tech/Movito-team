package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
import com.movito.movito.LanguageManager
import com.movito.movito.data.model.Genre
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import com.movito.movito.data.source.remote.TmdbApi
import androidx.compose.runtime.Composable
import android.app.Activity

/**
 * UI state representation for the movie categories/genres screen.
 *
 * This data class holds all the state necessary for displaying movie genres,
 * including loading states and error handling.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @property isLoading Indicates if genres are currently being loaded from the API
 * @property genres List of available movie genres from TMDB API
 * @property error Error message [String], `null` if no error occurred
 *
 * @since 14 Nov 2025
 */
data class CategoriesUiState(
    val isLoading: Boolean = false,
    val genres: List<Genre> = emptyList(),
    val error: String? = null
)

/**
 * [ViewModel] for managing movie categories (genres) screen state and data.
 *
 * This [ViewModel] handles:
 * - Loading movie genres from TMDB API
 * - Managing loading states and error handling
 * - Providing reactive UI state via [StateFlow]
 * - Supporting multi-language genre names
 *
 * The [ViewModel] automatically loads genres when created and updates the UI state
 * reactively. Genres are loaded from TMDB API with language support for both
 * English and Arabic.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @since 14 Nov 2025
 *
 * @see Genre for the data model structure
 * @see TmdbApi.getGenres for the API endpoint
 */
class CategoriesViewModel : ViewModel() {

    /**
     * Internal mutable state flow for categories UI state.
     *
     * This `private` [MutableStateFlow] holds the current state and allows
     * controlled updates from within the [ViewModel].
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     */
    private val _uiState = MutableStateFlow(CategoriesUiState())

    /**
     * `public` immutable [StateFlow] exposing the current categories UI state.
     *
     * Collect this flow in [Composable]s or any [Activity] to reactively update
     * the UI when the state changes.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     *
     * @see CategoriesUiState for the state structure
     */
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    /**
     * TMDB API key retrieved from [BuildConfig].
     *
     * This key is required for authenticating all TMDB API requests.
     * It's stored in the local.properties file and injected via Gradle.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Nov 2025
     */
    private val apiKey = BuildConfig.TMDB_API_KEY

    /**
     * Current language state flow from [LanguageManager].
     *
     * This flow emits the current app language (`"en"` or `"ar"`) and is used
     * to load localized genre names from TMDB API.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    private val currentLanguage = LanguageManager.currentLanguage

    /**
     * Localized error message for genre loading failures.
     *
     * @param errorMsg Original error message from exception
     * @return Localized error message in current language
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    private fun falidToLoadGenresMsg(errorMsg: String?) =
        if (LanguageManager.currentLanguage.value == "ar")
            "تعذر تحميل الفئات: $errorMsg"
        else
            "Failed to load genres: $errorMsg"

    /**
     * Localized generic error message for unexpected failures.
     *
     * @param errorMsg Original error message from exception
     * @return Localized error message in current language
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    private fun unexpectedErrorMsg(errorMsg: String?) =
        if (LanguageManager.currentLanguage.value == "ar")
            "حدث خطأ غير متوقع: $errorMsg"
        else
            "An unexpected error occurred: $errorMsg"

    /**
     * Initializes the [ViewModel] and triggers genre loading.
     *
     * The init block automatically calls [loadGenres] when the [ViewModel]
     * is created, ensuring genres are loaded as soon as the screen appears.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Dec 2025
     */
    init {
        loadGenres()
    }

    /**
     * Loads movie genres from TMDB API.
     *
     * This method:
     * 1. Sets loading state to true
     * 2. Clears any previous errors
     * 3. Makes API call to fetch genres with current language
     * 4. Updates UI state with results or error
     *
     * The method runs in the [viewModelScope] and automatically cancels
     * if the [ViewModel] is cleared, preventing memory leaks.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 14 Dec 2025
     *
     * @see TmdbApi.getGenres for the API implementation
     */
    private fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = RetrofitInstance.api.getGenres(apiKey, currentLanguage.value)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        genres = response.genres
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = falidToLoadGenresMsg(e.message)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = unexpectedErrorMsg(e.message)
                    )
                }
            }
        }
    }
}