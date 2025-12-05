package com.movito.movito.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.movito.movito.ThemeManager
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing app theme state using the singleton ThemeManager.
 *
 * This ViewModel serves as a thin wrapper around [ThemeManager], providing:
 * - Reactive theme state via StateFlow for Compose UI observation
 * - Lifecycle-aware theme preference loading
 * - Consistent theme management across all activities
 *
 * Architecture Note: This ViewModel delegates all theme operations to the singleton
 * [ThemeManager] which handles the actual state management and persistence.
 * This separation allows the ViewModel to handle Android lifecycle concerns
 * while ThemeManager handles business logic and state storage.
 *
 * Usage in Activities:
 * 1. Call [loadThemePreference] in Activity.onCreate() to initialize theme state
 * 2. Collect [isDarkTheme] in Compose UI to reactively update with theme changes
 * 3. Call [toggleTheme] when user changes theme preference (e.g., from Settings)
 */
class ThemeViewModel : ViewModel() {
    /**
     * Reactive StateFlow that emits the current theme state.
     *
     * Collect this in Composables to automatically update UI when theme changes.
     * The value will be:
     * - true: Dark theme is enabled
     * - false: Light theme is enabled
     *
     * This flow is backed by [ThemeManager.isDarkTheme] singleton StateFlow,
     * ensuring consistent theme state across the entire application.
     */
    val isDarkTheme: StateFlow<Boolean> = ThemeManager.isDarkTheme

    /**
     * Loads the saved theme preference into memory.
     *
     * Call this method when an Activity starts to ensure the ViewModel has
     * the current theme state. Typically called in `Activity.onCreate()`.
     *
     * Note: This method reads from SharedPreferences and updates the
     * [ThemeManager] singleton's internal StateFlow.
     *
     * @param context The context used to access SharedPreferences storage
     */
    fun loadThemePreference(context: Context) {
        ThemeManager.loadThemePreference(context)
    }

    /**
     * Changes the app theme and persists the preference.
     *
     * This method:
     * 1. Updates the reactive StateFlow in [ThemeManager]
     * 2. Persists the preference to SharedPreferences for future app sessions
     * 3. Triggers UI recomposition in all observing Composables
     *
     * @param enableDarkTheme Boolean indicating whether to enable dark theme (true) or light theme (false)
     * @param context The context used to save the preference to SharedPreferences
     */
    fun toggleTheme(enableDarkTheme: Boolean, context: Context) {
        ThemeManager.toggleTheme(enableDarkTheme, context)
    }
}