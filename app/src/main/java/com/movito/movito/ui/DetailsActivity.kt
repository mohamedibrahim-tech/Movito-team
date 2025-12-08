package com.movito.movito.ui

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.movito.movito.MovitoApplication
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.DetailsViewModel
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel


/**
 * [Activity] for displaying detailed information about a movie.
 *
 * This activity:
 * 1. Receives a [Movie] object as an intent extra
 * 2. Displays comprehensive movie details including poster, rating, overview, genres, and recommendations
 * 3. Integrates with [DetailsViewModel] for data management
 * 4. Supports theme and language preferences
 * 5. Provides back navigation with proper transitions
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 14 Nov 2025
 * @see DetailsScreen
 * @see DetailsViewModel
 * @see Movie
 */
class DetailsActivity : ComponentActivity() {

    private val viewModel: DetailsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    /**
     * Sets up the activity with edge-to-edge display and theme/language preferences.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param savedInstanceState Previously saved instance state, or null
     * @since 14 Nov 2025
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Load theme preference
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val movie: Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getParcelableExtra("movie", Movie::class.java)
            else @Suppress("DEPRECATION") intent.getParcelableExtra("movie")

            // Re-compose when theme changes
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    DetailsScreen(
                        viewModel = viewModel,
                        movie = movie ?: Movie(),
                        onClickBackButton = { onBackPressedDispatcher.onBackPressed() }
                    )
                }
            }

        }
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