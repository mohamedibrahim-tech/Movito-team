package com.movito.movito

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton manager for app theme state and persistence.
 *
 * This object serves as the single source of truth for theme state, providing:
 * - Reactive theme state via StateFlow
 * - Theme preference persistence using SharedPreferences
 * - App-wide theme consistency
 *
 * Why use a singleton instead of DataStore/ViewModel only?
 * 1. **App-wide consistency**: All activities observe the same theme state
 * 2. **Simple persistence**: SharedPreferences is sufficient for simple boolean preference
 * 3. **Reduced complexity**: Avoids unnecessary ViewModel/DataStore overhead for simple feature
 * 4. **Performance**: Direct SharedPreferences access is fast for single boolean value
 *
 * Architecture: This singleton pattern is appropriate because theme state is truly
 * global - all parts of the app need consistent access to the current theme.
 * ViewModels wrap this singleton to provide lifecycle-aware observation.
 */
object ThemeManager {
    /**
     * Internal mutable state flow for the current theme.
     * Initialized to light theme (false) and updated when preferences are loaded.
     */
    private val _isDarkTheme = MutableStateFlow(false)

    /**
     * Public StateFlow that emits the current theme state.
     *
     * Activities and Composables should collect this flow (through ThemeViewModel)
     * to reactively update UI when theme changes.
     */
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    /**
     * Loads the theme preference from SharedPreferences into the StateFlow.
     *
     * Call this during app startup (MovitoApplication.onCreate) and in each
     * Activity's onCreate to ensure the StateFlow has the latest preference.
     *
     * @param context The context used to access SharedPreferences
     */
    fun loadThemePreference(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("dark_theme", false)
    }

    /**
     * Updates the theme preference and persists it to SharedPreferences.
     *
     * This method updates the reactive StateFlow immediately (for instant UI updates)
     * and persists the change to SharedPreferences (for future app sessions).
     *
     * @param enableDarkTheme Boolean indicating whether to enable dark theme
     * @param context The context used to save the preference to SharedPreferences
     */
    fun toggleTheme(enableDarkTheme: Boolean, context: Context) {
        _isDarkTheme.value = enableDarkTheme
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_theme", enableDarkTheme).apply()
    }
}