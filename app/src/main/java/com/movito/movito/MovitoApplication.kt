package com.movito.movito

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * The main Application class for the Movito app.
 *
 * Responsibilities:
 * - Initializes app-wide components (theme, language)
 * - Provides helper methods for locale/context configuration
 * - Manages language change observers for activity coordination
 *
 * Note: This class is created when the app process starts and exists
 * for the entire lifecycle of the app.
 */
class MovitoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize theme and language when app starts
        LanguageManager.loadLanguagePreference(this)
    }

    companion object {
        /**
         * Gets the currently saved language preference.
         *
         * This method retrieves the language from [LanguageManager] which
         * is the single source of truth. Use this when you need the current
         * language outside of a reactive context.
         *
         * @param context The context (unused, but required for consistency)
         * @return The current language code ("en" or "ar")
         */
        fun getSavedLanguage(context: Context): String {
            return LanguageManager.currentLanguage.value
        }

        /**
         * Creates a new Context with the specified locale configuration.
         *
         * This method:
         * 1. Creates a Locale from the language code
         * 2. Updates the Configuration with the new locale
         * 3. Creates and returns a new Context with updated configuration
         *
         * This is used in [android.app.Activity.attachBaseContext] to ensure
         * activities start with the correct language configuration.
         *
         * Note: Different implementation for different API levels:
         * - API 24+ (Nougat): Uses createConfigurationContext (recommended)
         * - API <24: Uses deprecated updateConfiguration
         *
         * @param newBase The original context to wrap
         * @param languageCode The language code ("en" or "ar")
         * @return A new Context configured with the specified locale
         */
        fun updateBaseContextLocale(context: Context, languageCode: String): Context {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = Configuration(resources.configuration)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Modern approach for API 24+
                configuration.setLocale(locale)
                configuration.setLayoutDirection(locale)
                return context.createConfigurationContext(configuration)
            } else {
                // Legacy approach for older APIs
                @Suppress("DEPRECATION")
                configuration.locale = locale
                @Suppress("DEPRECATION")
                configuration.setLayoutDirection(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(configuration, resources.displayMetrics)
                return context
            }
        }
    }

    /**
     * Observer pattern implementation for coordinating activity restarts.
     *
     * This object manages a list of listeners (typically Activities) that
     * need to restart when the language changes. When [notifyLanguageChanged]
     * is called, all registered listeners are invoked.
     *
     * Usage in Activities:
     * 1. Register in onCreate(): LanguageChangeObserver.addListener { restartActivity() }
     * 2. Unregister in onDestroy(): LanguageChangeObserver.removeListener(listener)
     *
     * Note: This is an alternative to using [LanguageViewModel.shouldRestartActivity]
     * and is useful for non-Compose activities or when ViewModel isn't available.
     */
    object LanguageChangeObserver {
        /**
         * Set of listeners to notify when language changes.
         * Using a Set ensures each listener is only added once.
         */
        private val listeners = mutableSetOf<() -> Unit>()

        /**
         * Registers a listener to be notified when language changes.
         *
         * @param listener A lambda that typically restarts the calling activity
         */
        fun addListener(listener: () -> Unit) {
            listeners.add(listener)
        }

        /**
         * Unregisters a previously registered listener.
         *
         * Important: Always unregister in Activity.onDestroy() to prevent memory leaks.
         *
         * @param listener The listener to remove
         */
        fun removeListener(listener: () -> Unit) {
            listeners.remove(listener)
        }

        /**
         * Notifies all registered listeners that the language has changed.
         *
         * This is called by [LanguageManager.setLanguage] after the language
         * preference has been updated. Each listener should handle the restart
         * appropriately (usually by calling Activity.recreate()).
         */
        fun notifyLanguageChanged() {
            listeners.forEach { it.invoke() }
        }
    }
}