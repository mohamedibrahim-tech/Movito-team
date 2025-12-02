package com.movito.movito.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import com.movito.movito.LanguageManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing language-related UI state and business logic.
 *
 * This ViewModel:
 * - Exposes the current language as a [StateFlow] for UI observation
 * - Manages activity restart coordination when language changes
 * - Provides methods to change the language from the UI
 *
 * Usage in Activities:
 * 1. Collect [currentLanguage] to get the current language
 * 2. Collect [shouldRestartActivity] to know when to restart the activity
 * 3. Call [setLanguage] when user changes language (e.g., from Settings)
 * 4. Call [onActivityRestarted] after restarting to reset the flag
 *
 * @see LanguageManager for the underlying data source
 */
class LanguageViewModel : ViewModel() {
    /**
     * The current app language as a reactive StateFlow.
     *
     * Collect this in Composables or Activities to react to language changes.
     * The value will be either "en" (English) or "ar" (Arabic).
     *
     * Note: This flow is backed by [LanguageManager.currentLanguage].
     */
    val currentLanguage: StateFlow<String> = LanguageManager.currentLanguage

    /**
     * Internal mutable state that signals when an activity should restart.
     *
     * This is set to `true` when [setLanguage] is called, and should be
     * set back to `false` by calling [onActivityRestarted].
     *
     * Activities should collect [shouldRestartActivity] and restart when it's true.
     */
    private val _shouldRestartActivity = MutableStateFlow(false)

    /**
     * Public StateFlow that emits `true` when the current activity should restart.
     *
     * Collect this in Activities to know when to trigger a restart.
     * The restart is necessary to apply language changes to the entire
     * activity context and resources.
     */
    val shouldRestartActivity = _shouldRestartActivity.asStateFlow()

    /**
     * Loads the language preference from storage.
     *
     * Call this when an Activity starts to ensure the ViewModel has the
     * current language state. Typically called in `Activity.onCreate()`.
     *
     * @param context The context used to load the preference
     */
    fun loadLanguagePreference(context: Context) {
        LanguageManager.loadLanguagePreference(context)
    }

    /**
     * Changes the app language and triggers activity restart.
     *
     * This method:
     * 1. Updates the language preference via [LanguageManager]
     * 2. Sets [_shouldRestartActivity] to `true` to signal UI to restart
     *
     * Activities observing [shouldRestartActivity] should restart themselves
     * and then call [onActivityRestarted] to reset the flag.
     *
     * @param languageCode The language code to set ("en" or "ar")
     * @param context The context used to save the preference
     */
    fun setLanguage(languageCode: String, context: Context) {
        LanguageManager.setLanguage(languageCode, context)

        // Trigger activity recreation in a coroutine-safe way
        viewModelScope.launch {
            _shouldRestartActivity.value = true
        }
    }

    /**
     * Resets the activity restart flag after an activity has been restarted.
     *
     * Call this method AFTER an activity has restarted itself due to a
     * language change. This ensures the flag doesn't trigger infinite restarts.
     *
     * Typical usage in Activities:
     * ```
     * LaunchedEffect(shouldRestartActivity) {
     *     if (shouldRestartActivity) {
     *         restartActivity()
     *         viewModel.onActivityRestarted() // Reset the flag
     *     }
     * }
     * ```
     */
    fun onActivityRestarted() {
        viewModelScope.launch {
            _shouldRestartActivity.value = false
        }
    }
}