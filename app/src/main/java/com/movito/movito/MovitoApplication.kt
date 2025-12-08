package com.movito.movito

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import com.movito.movito.LanguageManager.currentLanguage
import android.content.res.Resources
import android.app.Activity
import com.movito.movito.viewmodel.LanguageViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Main Application class for the Movito movie discovery app.
 *
 * This class serves as the entry point for the application and is responsible for:
 * - Initializing app-wide components (theme, language managers)
 * - Managing application-level configuration
 * - Providing locale/context configuration utilities
 * - Coordinating activity restarts on language changes
 *
 * The class is annotated with [HiltAndroidApp] to enable Hilt dependency injection
 * throughout the application.
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 1 Dec 2025
 *
 * @see ThemeManager for theme management
 * @see LanguageManager for language management
 */
@HiltAndroidApp
class MovitoApplication : Application() {

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     * This method initializes application-wide components:
     * 1. Loads the saved theme preference via [ThemeManager]
     * 2. Loads the saved language preference via [LanguageManager]
     *
     * Note: This method is called only once during the application lifecycle.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    override fun onCreate() {
        super.onCreate()
        // Initialize theme and language when app starts
        ThemeManager.loadThemePreference(this)
        LanguageManager.loadLanguagePreference(this)
    }

    companion object {
        /**
         * Gets the currently saved language preference.
         *
         * This method retrieves the language from [LanguageManager] which
         * maintains the single source of truth for language state. Use this
         * method when you need the current language outside of a reactive
         * context (i.e., when you can't observe the [StateFlow]).
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param context The context (parameter maintained for API consistency)
         * @return The current language code (`"en"` or `"ar"`)
         *
         * @since 1 Dec 2025
         *
         * @see currentLanguage for reactive observation
         */
        fun getSavedLanguage(context: Context): String {
            return LanguageManager.currentLanguage.value
        }

        /**
         * Creates a new Context with the specified locale configuration applied.
         *
         * This method is used to wrap an existing context with updated locale
         * settings. It's typically called in [Activity.attachBaseContext]
         * to ensure activities start with the correct language configuration.
         *
         * Implementation notes:
         * - For API 24+ (Nougat): Uses [Context.createConfigurationContext] (recommended)
         * - For API < 24: Uses deprecated [Resources.updateConfiguration]
         * - Sets both locale and layout direction for RTL languages
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param context The original context to wrap
         * @param languageCode The language code (`"en"` or `"ar"`)
         * @return A new Context configured with the specified locale
         *
         * @since 1 Dec 2025
         *
         * @see Locale for locale configuration
         * @see Configuration for system configuration
         */
        fun updateBaseContextLocale(context: Context, languageCode: String): Context {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = Configuration(resources.configuration)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Modern approach for API 24+ (Nougat)
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
     * Observer pattern implementation for coordinating activity restarts on language changes.
     *
     * This object manages a list of listeners (typically Activities) that need to
     * restart when the language changes. When [notifyLanguageChanged] is called,
     * all registered listeners are invoked.
     *
     * Usage pattern in Activities:
     * ```
     * // Register in onCreate():
     * MovitoApplication.LanguageChangeObserver.addListener { restartActivity() }
     *
     * // Unregister in onDestroy():
     * MovitoApplication.LanguageChangeObserver.removeListener(listener)
     * ```
     *
     * > Note: This is an alternative to using [LanguageViewModel.shouldRestartActivity]
     * > and is useful for non-Compose activities or when [ViewModel] isn't available.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     *
     * @see LanguageManager.setLanguage which triggers notifications
     */
    object LanguageChangeObserver {
        /**
         * Set of listeners to notify when language changes.
         *
         * Using a [MutableSet] ensures:
         * - Each listener is only added once (prevents duplicate notifications)
         * - Fast O(1) lookup for removal
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @since 1 Dec 2025
         */
        private val listeners = mutableSetOf<() -> Unit>()

        /**
         * Registers a listener to be notified when the application language changes.
         *
         * The listener is typically a lambda that restarts the calling activity
         * (e.g., `{ activity.recreate() }`).
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param listener A lambda that will be invoked when language changes
         *
         * @since 1 Dec 2025
         *
         * @see removeListener to unregister the listener
         */
        fun addListener(listener: () -> Unit) {
            listeners.add(listener)
        }

        /**
         * Unregisters a previously registered listener.
         *
         * Important: Always unregister listeners in [Activity.onDestroy]
         * to prevent memory leaks and ensure proper cleanup.
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param listener The listener to remove from notification list
         *
         * @since 1 Dec 2025
         *
         * @see addListener to register a listener
         */
        fun removeListener(listener: () -> Unit) {
            listeners.remove(listener)
        }

        /**
         * Notifies all registered listeners that the language has changed.
         *
         * This method is called by [LanguageManager.setLanguage] after the language
         * preference has been updated. Each listener should handle the restart
         * appropriately (typically by calling [Activity.recreate]).
         *
         * > Note: Notifications are delivered synchronously in the order listeners
         * > were added.
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @since 1 Dec 2025
         */
        fun notifyLanguageChanged() {
            listeners.forEach { it.invoke() }
        }
    }
}