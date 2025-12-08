package com.movito.movito.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import com.movito.movito.MovitoApplication
import com.movito.movito.R
import com.movito.movito.notifications.NotificationScheduler
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.AuthViewModel
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import android.app.Activity

/**
 * [Activity] for managing application settings and user preferences.
 *
 * This [Activity] provides access to:
 * - Account management (email display, password reset, sign out)
 * - Language selection (English/Arabic)
 * - Theme toggling (dark/light mode)
 * - Notification settings (frequency, limits, testing)
 * - About information (version, GitHub repository)
 *
 * The [Activity] automatically redirects to [SignInActivity] if the user is not authenticated.
 *
 * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
 *
 * @since 11 Nov 2025
 *
 * @see SettingsScreen
 * @see AuthViewModel
 * @see ThemeViewModel
 * @see LanguageViewModel
 *
 */
class SettingsActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()
    private val languageChangeListener = {
        // Restart activity when language changes
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        this.overridePendingTransition(
            R.anim.change_language_in,
            R.anim.change_language_out
        )
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.change_language_in,
            R.anim.change_language_out
        )
        finish()
    }

    /**
     * Sets up the [Activity] with edge-to-edge display and theme/language preferences.
     * Observes authentication state and redirects to sign-in if user is not authenticated.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @since 11 Nov 2025
     *
     * @param savedInstanceState Previously saved instance state, or null
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MovitoApplication.LanguageChangeObserver.addListener(languageChangeListener)
        enableEdgeToEdge()
        // Load theme and language preferences
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)
        setContent {
            val authState by authViewModel.authState.collectAsState()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            var notificationsState by remember {
                mutableStateOf(
                    NotificationManagerCompat.from(this).areNotificationsEnabled()
                )
            }

            /*
             * Observes authentication state and redirects to sign-in if user is not authenticated.
             */
            LaunchedEffect(authState.user) {
                if (authState.user == null && authState.isInitialCheckDone) {
                    val intent = Intent(this@SettingsActivity, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            // Force recomposition when theme/language changes
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    SettingsScreen(
                        onThemeToggle = { newTheme ->
                            themeViewModel.toggleTheme(newTheme, this@SettingsActivity)
                        },
                        onSignOut = {
                            NotificationScheduler.cancelNotifications(this@SettingsActivity)
                            val notificationManager =
                                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancelAll()
                            authViewModel.signOut()
                        },
                        userEmail = authState.user?.email,
                        onChangePassword = { email ->
                            authViewModel.sendPasswordResetEmail(email)
                        },
                        notificationsEnabled = notificationsState,
                        onNotificationsStateUpdate = { notificationsState = it },
                        isDarkTheme = isDarkTheme,
                    )
                }
            }

        }
    }

    /**
     * Updates the base context with the saved language preference for proper localization.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param newBase The base context
     *
     * @since 1 Dec 2025
     */
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = MovitoApplication.getSavedLanguage(newBase)
        val updatedContext = MovitoApplication.updateBaseContextLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
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