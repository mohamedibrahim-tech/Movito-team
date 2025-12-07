package com.movito.movito.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.NotificationManagerCompat
import com.movito.movito.MovitoApplication
import com.movito.movito.R
import com.movito.movito.notifications.NotificationHelper
import com.movito.movito.notifications.NotificationPreferences
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch
import com.movito.movito.MovitoApplication.LanguageChangeObserver

/**
 * MAIN HOME SCREEN ACTIVITY
 *
 * PURPOSE: Serves as the primary entry point after user authentication,
 * displaying movie categories in a grid layout. This activity coordinates
 * theme management, language switching, and notification permission reminders.
 *
 * ARCHITECTURE:
 * - Hosts [CategoriesScreen] composable with Compose integration
 * - Manages language change listeners for seamless locale switching
 * - Coordinates notification permission flow between SignIn and Settings
 * - Handles activity restart on language/theme changes
 *
 * KEY FEATURES:
 *
 * LANGUAGE MANAGEMENT:
 * - Listens for language changes via [LanguageChangeObserver]
 * - Restarts activity with proper animations when language changes
 * - Preserves language preference across app restarts
 * - Attaches base context with correct locale configuration
 *
 * THEME COORDINATION:
 * - Integrates with [ThemeViewModel] for dark/light theme persistence
 * - Uses Material 3 theming system
 * - Re-composes UI on theme changes using key function
 *
 * PERMISSION REMINDER SYSTEM:
 * - Displays non-intrusive snackbar when notifications are disabled
 * - Only shows reminder once per app installation (persistent flag)
 * - Opens system notification settings when user taps "ENABLE"
 * - Coordinates with [SignInActivity] to set reminder flags
 *
 * NAVIGATION:
 * - Launches [MoviesByGenreActivity] with genre data
 * - Uses activity transitions for smooth navigation
 * - Handles back navigation with proper task management
 *
 * INTEGRATION POINTS:
 * - [SignInActivity]: Receives users after successful authentication
 * - [SettingsActivity]: Returns users after configuration changes
 * - [NotificationHelper]: Opens system settings for permission management
 * - [NotificationPreferences]: Stores permission reminder state
 *
 * LIFECYCLE MANAGEMENT:
 * - [onCreate]: Initializes theme, language, and permission checks
 * - [attachBaseContext]: Applies saved language preference
 * - [onDestroy]: Removes language change listeners
 *
 * ERROR HANDLING:
 * - Gracefully handles missing notification permission
 * - Recovers from language change interruptions
 * - Maintains state across configuration changes
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @since 14 Nov 2025
 */
class CategoriesActivity : ComponentActivity() {
    // Theme and language view models
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    /**
     * Language change listener that restarts activity with animations
     * when user changes language in SettingsScreen. This ensures all
     * resources are reloaded with the new locale.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    private val languageChangeListener = {
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Applies saved language preference to activity context.
     * This method is called before onCreate to ensure proper
     * localization from the start.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = MovitoApplication.getSavedLanguage(newBase)
        val updatedContext = MovitoApplication.updateBaseContextLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for language change notifications
        MovitoApplication.LanguageChangeObserver.addListener(languageChangeListener)
        enableEdgeToEdge()

        // Load user preferences
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val shouldRestartActivity by languageViewModel.shouldRestartActivity.collectAsState()

            /*
             * PERMISSION REMINDER SNACKBAR
             *
             * Displays a gentle reminder when system notifications are disabled.
             * This reminder appears only once and provides direct access to
             * system settings for enabling notifications.
             */
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                if (!NotificationManagerCompat.from(this@CategoriesActivity).areNotificationsEnabled()) {
                    val prefs = NotificationPreferences.getInstance(this@CategoriesActivity)
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = getString(R.string.enable_notifications_riminder),
                            actionLabel = getString(R.string.enable),
                            withDismissAction = true
                        ).apply {
                            if (this == SnackbarResult.ActionPerformed) {
                                NotificationHelper.openNotificationSettings(
                                    context = this@CategoriesActivity,
                                    prefs = prefs,
                                    onStateUpdate = { }
                                )
                            }
                        }
                        // Prevent repeat reminders
                        prefs.setShouldShowPermissionReminder(false)
                    }
                }
            }


            /* LANGUAGE CHANGE HANDLER
             *
             * Restarts activity when language is changed to reload all
             * resources with the new locale. This ensures consistent
             * localization across the entire UI.
             */
            LaunchedEffect(shouldRestartActivity) {
                if (shouldRestartActivity) {
                    // Consume the event first to prevent a restart loop
                    languageViewModel.onActivityRestarted()

                    // Restart this activity
                    val intent = Intent(this@CategoriesActivity, CategoriesActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }


            /* THEME MANAGEMENT
             *
             * Uses key() function to force recomposition when theme changes.
             * This ensures all colors and styles are updated correctly.
             */
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    CategoriesScreen(snackbarHost = { SnackbarHost(hostState = snackbarHostState) })
                }
            }
        }
    }

    /**
     * Removes the language change listener when activity is destroyed.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     */
    override fun onDestroy() {
        super.onDestroy()
        MovitoApplication.LanguageChangeObserver.removeListener(languageChangeListener)
    }
}