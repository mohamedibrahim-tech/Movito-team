package com.movito.movito.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.movito.movito.ThemeManager
import kotlinx.coroutines.flow.StateFlow
import android.content.SharedPreferences
import android.app.Activity
import androidx.compose.runtime.Composable

/**
 * [ViewModel] for managing app theme state using the singleton [ThemeManager].
 *
 * This [ViewModel] serves as a thin wrapper around [ThemeManager], providing:
 * - Reactive theme state via [StateFlow] for Compose UI observation
 * - Lifecycle-aware theme preference loading
 * - Consistent theme management across all activities
 * - Clean separation between UI logic and business logic
 *
 * Architecture Rationale:
 * - [ThemeManager] handles business logic and state persistence ([SharedPreferences])
 * - [ThemeViewModel] handles Android lifecycle concerns and UI state observation
 * - This separation allows for testable business logic and lifecycle-aware UI components
 *
 * Usage in Activities:
 * 1. Call [loadThemePreference] in [Activity.onCreate]`()` to initialize theme state
 * 2. Collect [isDarkTheme] in Compose UI to reactively update with theme changes
 * 3. Call [toggleTheme] when user changes theme preference (e.g., from Settings screen)
 *
 * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid) <- original author
 *
 * @see ThemeManager for the underlying theme state management
 *
 * @since 26 Nov 2025
 */
class ThemeViewModel : ViewModel() {
    /**
     * Reactive StateFlow that emits the current theme state.
     *
     * Collect this flow in [Composable]s to automatically update UI when theme changes.
     * Flow values:
     * - `true`: Dark theme is enabled
     * - `false`: Light theme is enabled (default)
     *
     * This flow is backed by [ThemeManager.isDarkTheme] singleton [StateFlow],
     * ensuring consistent theme state across the entire application.
     *
     * Usage example in Compose:
     * ```kotlin
     * val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
     * MaterialTheme(
     *     colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
     * ) {
     *     // App content
     * }
     * ```
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid) <- original author
     *
     * @since 26 Nov 2025
     */
    val isDarkTheme: StateFlow<Boolean> = ThemeManager.isDarkTheme

    /**
     * Loads the saved theme preference from storage into memory.
     *
     * Call this method when an [Activity] starts to ensure the [ViewModel] has
     * the current theme state. Typically called in [Activity.onCreate]`()`.
     *
     * This method reads from [SharedPreferences] via [ThemeManager] and updates
     * the singleton's internal [StateFlow] with the saved preference.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context The context used to access [SharedPreferences] storage
     *
     * @since 2 Dec 2025
     *
     * @see toggleTheme to change and save theme preference
     */
    fun loadThemePreference(context: Context) {
        ThemeManager.loadThemePreference(context)
    }

    /**
     * Changes the app theme and persists the preference.
     *
     * This method performs two operations:
     * 1. Updates the reactive [StateFlow] in [ThemeManager] for immediate UI updates
     * 2. Persists the preference to [SharedPreferences] for future app sessions
     *
     * The change triggers UI recomposition in all observing [Composable]s,
     * providing instant visual feedback to the user.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid) <- original author
     *
     * @param enableDarkTheme [Boolean] indicating theme preference:
     *                        - `true`: Enable dark theme
     *                        - `false`: Enable light theme
     * @param context The context used to save the preference to [SharedPreferences]
     *
     * @since 26 Nov 2025
     *
     * @see loadThemePreference to load the saved preference
     */
    fun toggleTheme(enableDarkTheme: Boolean, context: Context) {
        ThemeManager.toggleTheme(enableDarkTheme, context)
    }
}