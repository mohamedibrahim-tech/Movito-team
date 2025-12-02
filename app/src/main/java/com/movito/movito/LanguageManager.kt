package com.movito.movito

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the application's language state and provides reactive updates.
 *
 * This singleton object serves as the single source of truth for the current app language.
 * It uses Kotlin Flow to provide reactive updates when the language changes.
 *
 * Key responsibilities:
 * - Loading language preferences from SharedPreferences
 * - Updating and saving language preferences
 * - Notifying observers about language changes
 * - Providing a reactive StateFlow for language state
 *
 * @see LanguageViewModel for the ViewModel layer that uses this manager
 * @see MovitoApplication.LanguageChangeObserver for activity restart coordination
 */
object LanguageManager {
    /**
     * Internal mutable state flow for the current language.
     * Defaults to English ("en") if no preference is set.
     *
     * Use [currentLanguage] to observe changes reactively.
     */
    private val _currentLanguage = MutableStateFlow("en")

    /**
     * Public StateFlow that emits the current app language.
     *
     * Collect this flow in ViewModels or Composables to react to language changes.
     * The value will be either "en" (English) or "ar" (Arabic).
     */
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    /**
     * Loads the saved language preference from SharedPreferences.
     *
     * Call this method when the app starts or when a context becomes available.
     * Typically called from [MovitoApplication.onCreate] and activity ViewModels.
     *
     * @param context The context used to access SharedPreferences.
     */
    fun loadLanguagePreference(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _currentLanguage.value = prefs.getString("app_language", "en") ?: "en"
    }

    /**
     * Changes the app language and persists the preference.
     *
     * This method:
     * 1. Updates the internal [StateFlow] to emit the new language
     * 2. Saves the preference to SharedPreferences for persistence
     * 3. Notifies all registered observers via [LanguageChangeObserver]
     *
     * Note: Changing the language triggers activity recreation in activities
     * that are observing through [LanguageChangeObserver].
     *
     * @param languageCode The language code to set ("en" or "ar")
     * @param context The context used to save the preference
     */
    fun setLanguage(languageCode: String, context: Context) {
        // Update reactive state
        _currentLanguage.value = languageCode

        // Persist to storage
        saveLanguage(context, languageCode)

        // Notify activities to restart
        MovitoApplication.LanguageChangeObserver.notifyLanguageChanged()
    }

    /**
     * Persists the language preference to SharedPreferences.
     *
     * This is called internally by [setLanguage] to ensure the preference
     * survives app restarts.
     *
     * @param context The context used to access SharedPreferences
     * @param languageCode The language code to save ("en" or "ar")
     */
    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()
    }
}