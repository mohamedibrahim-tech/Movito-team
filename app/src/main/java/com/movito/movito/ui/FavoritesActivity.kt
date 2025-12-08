package com.movito.movito.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.movito.movito.MovitoApplication
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import android.app.Activity
import com.movito.movito.viewmodel.FavoritesViewModel

/**
 * [Activity] for displaying the user's favorite movies.
 *
 * This activity:
 * 1. Shows a grid of movies the user has marked as favorites
 * 2. Integrates with [FavoritesViewModel] for real-time updates
 * 3. Supports theme and language preferences
 * 4. Handles language changes with activity restart
 * 5. Provides empty state and error handling
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @see FavoritesScreen
 * @see FavoritesViewModel
 *
 * @since 13 Nov 2025
 */
class FavoritesActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()
    private val languageChangeListener = {
        // Restart activity when language changes
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Sets up the activity with edge-to-edge display and theme/language preferences.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
     *
     * @param savedInstanceState Previously saved instance state, or `null`
     *
     * @since 13 Nov 2025
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MovitoApplication.LanguageChangeObserver.addListener(languageChangeListener)
        enableEdgeToEdge()
        // Load theme and language preferences
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            // Re-compose when theme changes
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    FavoritesScreen()
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

    /**
     * Removes the language change listener when activity is destroyed.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    override fun onDestroy() {
        super.onDestroy()
        MovitoApplication.LanguageChangeObserver.removeListener(languageChangeListener)
    }
}