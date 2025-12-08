package com.movito.movito.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.MovitoApplication
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.MoviesByGenreViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import android.app.Activity
import androidx.lifecycle.ViewModel

/**
 * [Activity] for displaying movies filtered by a specific genre.
 *
 * This activity:
 * 1. Receives genre ID and name from the intent that launched it
 * 2. Displays movies belonging to the selected genre in a grid layout
 * 3. Integrates with [MoviesByGenreViewModel] for data management
 * 4. Supports theme and language preferences
 * 5. Provides back navigation with slide animations
 *
 * The [ViewModel] is keyed by the genre ID to ensure separate instances for different genres.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @see MoviesByGenreScreen
 * @see MoviesByGenreViewModel
 *
 * @since 14 Nov 2025
 */
class MoviesByGenreActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    /**
     * Sets up the activity with edge-to-edge display and theme/language preferences.
     * Initializes the [MoviesByGenreViewModel] with the genre ID from the intent.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
     *
     * @param savedInstanceState Previously saved instance state, or null
     *
     * @since 14 Nov 2025
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val genreId = intent.getIntExtra("genreId", -1)//-1 to handle errors from the intent
        val genreName = intent.getStringExtra("genreName") ?: getString(R.string.movies)

        enableEdgeToEdge()
        // Load theme preference
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            // Re-compose when theme changes
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    val viewModel: MoviesByGenreViewModel = viewModel(key = genreId.toString()) {
                        MoviesByGenreViewModel(
                            savedStateHandle = androidx.lifecycle.SavedStateHandle(
                                mapOf("genreId" to genreId)
                            )
                        )
                    }
                    MoviesByGenreScreen(
                        viewModel = viewModel,
                        genreName = genreName,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }

    /**
     * Overrides the finish animation to provide consistent slide-out transition.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
     *
     * @since 19 Nov 2025
     */
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /**
     * Updates the base context with the saved language preference for proper localization.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param newBase The base context
     *
     * @since 1 Dec 2025
     */
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = MovitoApplication.getSavedLanguage(newBase)
        val updatedContext = MovitoApplication.updateBaseContextLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
    }
}