package com.movito.movito

import android.content.Context
import com.movito.movito.LanguageManager.currentLanguage
import com.movito.movito.LanguageManager.loadLanguagePreference
import com.movito.movito.LanguageManager.setLanguage
import com.movito.movito.MovitoApplication.LanguageChangeObserver
import com.movito.movito.viewmodel.LanguageViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.Composable

/**
 * Singleton manager for application language state and persistence.
 *
 * This object serves as the single source of truth for language state throughout the app.
 * It provides reactive language updates via StateFlow and persists language preferences
 * using SharedPreferences.
 *
 * Key responsibilities:
 * - Loading and saving language preferences from/to [SharedPreferences]
 * - Providing reactive [StateFlow] for language state observation
 * - Notifying registered observers when language changes
 * - Supporting bidirectional text layout for RTL languages (Arabic)
 *
 * Language codes:
 * - `"en"`: English (left-to-right)
 * - `"ar"`: Arabic (right-to-left)
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @see LanguageViewModel for [ViewModel] layer that uses this manager
 * @see LanguageChangeObserver for activity restart coordination
 *
 * @since 1 Dec 2025
 */
object LanguageManager {
    /**
     * Internal mutable state flow for the current application language.
     *
     * Defaults to `"en"` (English) if no preference is loaded.
     * This is the backing field for [currentLanguage] StateFlow.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     *
     * @see currentLanguage for the public read-only [StateFlow]
     */
    private val _currentLanguage = MutableStateFlow("en")

    /**
     * Public StateFlow that emits the current app language.
     *
     * Collect this flow in [ViewModel]s or [Composable]s to reactively update
     * when the language changes. The value will be either:
     * - `"en"` for English
     * - `"ar"` for Arabic
     *
     * Usage example in Compose:
     * ```kotlin
     * val currentLanguage by LanguageManager.currentLanguage.collectAsState()
     * ```
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    /**
     * Loads the saved language preference from [SharedPreferences] into the [StateFlow].
     *
     * This method should be called during app initialization (typically in
     * [MovitoApplication.onCreate]) and in each activity's [ViewModel] initialization
     * to ensure the [StateFlow] has the latest preference.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context The context used to access SharedPreferences storage
     *
     * @since 1 Dec 2025
     *
     * @see setLanguage to change and persist the language preference
     */
    fun loadLanguagePreference(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _currentLanguage.value = prefs.getString("app_language", "en") ?: "en"
    }

    /**
     * Changes the application language and persists the preference.
     *
     * This method performs three key operations:
     * 1. Updates the reactive [StateFlow] to emit the new language immediately
     * 2. Saves the preference to [SharedPreferences] for persistence across app sessions
     * 3. Notifies all registered observers via [LanguageChangeObserver]
     *
     * Note: Changing the language triggers activity recreation for activities
     * that are observing through [LanguageChangeObserver].
     * This is necessary to update the activity's configuration and resources.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param languageCode The language code to set (`"en"` or `"ar"`)
     * @param context The context used to save the preference to [SharedPreferences]
     *
     * @throws IllegalArgumentException if [languageCode] is not `"en"` or `"ar"`
     *
     * @since 1 Dec 2025
     *
     * @see loadLanguagePreference to load the saved preference
     * @see LanguageChangeObserver for activity restart coordination
     */
    fun setLanguage(languageCode: String, context: Context) {
        require(languageCode in setOf("en", "ar")) {
            "Invalid language code: $languageCode. Must be 'en' or 'ar'."
        }

        // Update reactive state
        _currentLanguage.value = languageCode

        // Persist to storage
        saveLanguage(context, languageCode)

        // Notify activities to restart
        MovitoApplication.LanguageChangeObserver.notifyLanguageChanged()
    }

    /**
     * Persists the language preference to [SharedPreferences].
     *
     * This internal method is called by [setLanguage] to ensure the language
     * preference survives app restarts. It uses the `"app_settings"` shared preferences
     * file with key `"app_language"`.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context The context used to access [SharedPreferences]
     * @param languageCode The language code to save (`"en"` or `"ar"`)
     *
     * @since 1 Dec 2025
     *
     * @see loadLanguagePreference to load the saved preference
     */
    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()
    }
}