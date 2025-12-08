package com.movito.movito

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.movito.movito.viewmodel.ThemeViewModel
import androidx.lifecycle.ViewModel
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import android.app.Activity

/**
 * Singleton manager for application theme state and persistence.
 *
 * This object serves as the single source of truth for theme state throughout the app,
 * providing:
 * - Reactive theme state via [StateFlow] for real-time UI updates
 * - Theme preference persistence using [SharedPreferences]
 * - App-wide theme consistency across all activities and fragments
 *
 * Theme states:
 * - `false`: Light theme
 * - `true`: Dark theme
 *
 * Architecture rationale: This singleton pattern is appropriate because theme state is
 * truly global - all parts of the app need consistent access to the current theme.
 * [ViewModel]s wrap this singleton to provide lifecycle-aware observation.
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @see ThemeViewModel for [ViewModel] layer that uses this manager
 *
 * @since 2 Dec 2025
 */
object ThemeManager {
    /**
     * Internal mutable state flow for the current theme.
     *
     * Initialized to light theme (`false`) and updated when preferences are loaded.
     * This is the backing field for [isDarkTheme] StateFlow.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @see isDarkTheme for the public read-only StateFlow
     * @since 2 Dec 2025
     */
    private val _isDarkTheme = MutableStateFlow(false)

    /**
     * `Public` [StateFlow] that emits the current theme state.
     *
     * any [Activity] and [Composable] should collect this flow (typically through
     * [ThemeViewModel]) to reactively update UI when the theme changes.
     *
     * Flow values:
     * - `true`: Dark theme is enabled
     * - `false`: Light theme is enabled (default)
     *
     * Usage example in Compose:
     * ```kotlin
     * val isDarkTheme by ThemeManager.isDarkTheme.collectAsState()
     * MaterialTheme(
     *     colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
     * ) { /* Content */ }
     * ```
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 2 Dec 2025
     */
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    /**
     * Loads the theme preference from [SharedPreferences] into the [StateFlow].
     *
     * This method should be called during app startup ([MovitoApplication.onCreate])
     * and in each Activity's onCreate to ensure the StateFlow has the latest
     * preference value. It reads from the "app_settings" shared preferences file
     * with key "dark_theme".
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context The context used to access SharedPreferences storage
     *
     * @see toggleTheme to update and persist theme preference
     *
     * @since 2 Dec 2025
     */
    fun loadThemePreference(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("dark_theme", false)
    }

    /**
     * Updates the theme preference and persists it to [SharedPreferences].
     *
     * This method performs two key operations:
     * 1. Updates the reactive [StateFlow] immediately (for instant UI updates)
     * 2. Persists the change to [SharedPreferences] (for future app sessions)
     *
     * The method uses `apply()` for [SharedPreferences] to write asynchronously
     * without blocking the UI thread.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param enableDarkTheme Boolean indicating whether to enable dark theme
     *                        - `true`: Enable dark theme
     *                        - `false`: Enable light theme
     * @param context The context used to save the preference to [SharedPreferences]
     *
     * @see loadThemePreference to load the saved preference
     *
     * @since 2 Dec 2025
     */
    fun toggleTheme(enableDarkTheme: Boolean, context: Context) {
        _isDarkTheme.value = enableDarkTheme
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_theme", enableDarkTheme).apply()
    }
}