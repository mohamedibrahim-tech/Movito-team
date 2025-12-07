package com.movito.movito.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import com.movito.movito.LanguageManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.movito.movito.MovitoApplication.LanguageChangeObserver
import android.app.Activity
import androidx.compose.runtime.Composable
import android.content.SharedPreferences

/**
 * [ViewModel] for managing language-related UI state and business logic.
 *
 * This [ViewModel] serves as a bridge between the UI layer and [LanguageManager],
 * providing lifecycle-aware language state management with the following features:
 * - Reactive language state observation via StateFlow
 * - [Activity] restart coordination for language changes
 * - Lifecycle-aware preference loading
 * - Thread-safe language updates
 *
 * The [ViewModel] delegates actual state management to [LanguageManager] singleton
 * while handling Android lifecycle concerns and UI coordination.
 *
 * Usage in Activities:
 * 1. Collect [currentLanguage] to observe language changes
 * 2. Collect [shouldRestartActivity] to know when to restart activity
 * 3. Call [setLanguage] when user changes language preference
 * 4. Call [onActivityRestarted] after restarting to reset the flag
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @see LanguageManager for the underlying data source
 * @see LanguageChangeObserver for alternative restart mechanism
 *
 * @since 1 Dec 2025
 */
class LanguageViewModel : ViewModel() {
    /**
     * The current app language as a reactive StateFlow.
     *
     * Collect this flow in [Composable]s or [Activity] to reactively update
     * UI when the language changes. The flow emits either:
     * - "en" for English (left-to-right)
     * - "ar" for Arabic (right-to-left)
     *
     * This flow is backed by [LanguageManager.currentLanguage], ensuring
     * consistent language state across the entire application.
     *
     * Usage example in Compose:
     * ```kotlin
     * val currentLanguage by viewModel.currentLanguage.collectAsState()
     * Text(
     *     text = localizedString,
     *     textDirection = if (currentLanguage == "ar")
     *         TextDirection.Rtl
     *     else
     *         TextDirection.Ltr
     * )
     * ```
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    val currentLanguage: StateFlow<String> = LanguageManager.currentLanguage

    /**
     * Internal mutable state that signals when an activity should restart.
     *
     * This state is set to `true` when [setLanguage] is called, and should
     * be reset to `false` by calling [onActivityRestarted].
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    private val _shouldRestartActivity = MutableStateFlow(false)

    /**
     * Public [StateFlow] that emits `true` when the current activity should restart.
     *
     * Activities should collect this flow and trigger a restart when it emits `true`.
     * Restarting is necessary to apply language changes to the entire activity
     * context and resources, including layout direction for RTL languages.
     *
     * Usage example in Compose Activity:
     * ```kotlin
     * LaunchedEffect(viewModel.shouldRestartActivity) {
     *     viewModel.shouldRestartActivity.collect { shouldRestart ->
     *         if (shouldRestart) {
     *             restartActivity() // Custom activity restart method
     *             viewModel.onActivityRestarted()
     *         }
     *     }
     * }
     * ```
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    val shouldRestartActivity = _shouldRestartActivity.asStateFlow()

    /**
     * Loads the language preference from storage into memory.
     *
     * Call this method when an [Activity] starts to ensure the [ViewModel] has
     * the current language state. Typically called in [Activity.onCreate]`()`.
     *
     * This method reads from [SharedPreferences] via [LanguageManager] and
     * updates the internal [StateFlow] with the saved preference.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context The context used to load the preference from [SharedPreferences]
     *
     * @see setLanguage to change and save language preference
     *
     * @since 1 Dec 2025
     */
    fun loadLanguagePreference(context: Context) {
        LanguageManager.loadLanguagePreference(context)
    }

    /**
     * Changes the app language and triggers activity restart coordination.
     *
     * This method performs two key operations:
     * 1. Updates the language preference via [LanguageManager] (persists to storage)
     * 2. Sets [shouldRestartActivity] to `true` to signal UI layer to restart
     *
     * Activities observing [shouldRestartActivity] should restart themselves
     * and then call [onActivityRestarted] to reset the flag.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param languageCode The language code to set (`"en"` or `"ar"`)
     * @param context The context used to save the preference to [SharedPreferences]
     *
     * @throws IllegalArgumentException if [languageCode] is not `"en"` or `"ar"`
     *
     * @since 1 Dec 2025
     */
    fun setLanguage(languageCode: String, context: Context) {
        require(languageCode in setOf("en", "ar")) {
            "Invalid language code: $languageCode. Must be 'en' or 'ar'."
        }

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
     * Typical usage pattern:
     * 1. Activity observes [shouldRestartActivity] flow
     * 2. When flow emits `true`, activity calls `recreate()`
     * 3. After recreation, activity calls [onActivityRestarted]
     *
     * > Note: This method should be called from the restarted activity's
     * > initialization code (e.g., `onCreate` or a `LaunchedEffect`).
     *
     * **example:**
     *
     * ```Kotlin
     * LaunchedEffect(shouldRestartActivity) {
     *     if (shouldRestartActivity) {
     *         restartActivity()
     *         viewModel.onActivityRestarted() // Reset the flag
     *     }
     * }
     * ```
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    fun onActivityRestarted() {
        viewModelScope.launch {
            _shouldRestartActivity.value = false
        }
    }
}