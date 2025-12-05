package com.movito.movito.ui

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.movito.movito.MovitoApplication
import com.movito.movito.notifications.BatteryPermissionHelper
import com.movito.movito.notifications.NotificationHelper
import com.movito.movito.notifications.NotificationPreferences
import com.movito.movito.notifications.NotificationScheduler
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.FavoritesViewModel
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * USER AUTHENTICATION AND ONBOARDING ACTIVITY
 *
 * PURPOSE: Handles user authentication, notification permission setup,
 * and initial app configuration. This activity manages the complete
 * onboarding flow including permission requests and notification scheduling.
 *
 * ARCHITECTURE:
 * - Integrates Firebase Authentication for secure user management
 * - Coordinates notification permission flow with Android's permission system
 * - Sets up notification channels and scheduling for new users
 * - Manages splash screen display during initialization
 *
 * KEY FEATURES:
 *
 * AUTHENTICATION FLOW:
 * - Email/password authentication with Firebase
 * - Automatic redirection to [CategoriesActivity] for logged-in users
 * - Password reset functionality
 * - Account creation navigation
 *
 * PERMISSION MANAGEMENT:
 * - Context-aware notification permission requests
 * - Permission requests only after successful authentication
 * - Smart handling of Android 13+ permission requirements
 * - Fallback flows for older Android versions
 *
 * NOTIFICATION SETUP:
 * - Creates notification channels for welcome and suggestion notifications
 * - Sends welcome notification on successful sign-in
 * - Auto-enables notifications for new users (opt-out model)
 * - Checks battery optimization status for reliable delivery
 *
 * USER PREFERENCE MANAGEMENT:
 * - Stores notification preferences for first-time users
 * - Sets permission reminder flags for [CategoriesActivity]
 * - Persists battery optimization status for Settings guidance
 *
 * SPLASH SCREEN INTEGRATION:
 * - Professional splash screen during initialization
 * - Smooth transition to authentication UI
 * - Maintains visual consistency during loading
 *
 * FLOW DESCRIPTION:
 * - 1.Splash screen displays during initialization
 * - 2.Check if user is already authenticated → redirect to Categories
 * - 3.Display authentication UI (email/password)
 * - 4.On successful authentication:
 * + a. Create notification channels
 * + b. Check battery optimization status
 * + c. Request notification permission (Android 13+)
 * + d. Send welcome notification
 * + e. Schedule personalized notifications (if enabled)
 * + f. Navigate to [CategoriesActivity]
 *
 * PERMISSION STRATEGY:
 * - Android 13+: Request [POST_NOTIFICATIONS] permission after sign-in
 * - Android 12 and below: No permission required
 * - Permission granted: Enable notifications and schedule
 * - Permission denied: Set reminder flag for [CategoriesActivity]
 *
 * ERROR HANDLING:
 * - Graceful handling of network authentication errors
 * - Recovery from permission request interruptions
 * - Fallback navigation if critical failures occur
 *
 * SECURITY NOTES:
 * - Uses Firebase Authentication for secure credential management
 * - All network communication uses HTTPS
 * - No sensitive data stored locally
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @since 14 Nov 2025
 */

@OptIn(ExperimentalPermissionsApi::class)
@AndroidEntryPoint
class SignInActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    /**
     * Applies saved language preference before activity creation.
     * Ensures authentication UI displays in user's preferred language.
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
        // Install splash screen before any UI initialization
        val splashScreen = installSplashScreen()
        var keepSplashOnScreen by mutableStateOf(true)
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        super.onCreate(savedInstanceState)

        // Redirect authenticated users directly to home screen
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        /*
         * PERMISSION REMINDER SETUP
         *
         * Sets flag to remind users about notifications in CategoriesActivity
         * if system notifications are disabled and user hasn't been reminded yet.
         */
        if (!NotificationPreferences.getInstance(this).getShouldShowPermissionReminder() &&
            !NotificationManagerCompat.from(this).areNotificationsEnabled()
        )
            NotificationPreferences.getInstance(this).setShouldShowPermissionReminder(true)

        // Initialize notification channels for the app
        NotificationHelper.createAllNotificationChannels(this)
        enableEdgeToEdge()

        // Load user preferences
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val shouldRestartActivity by languageViewModel.shouldRestartActivity.collectAsState()
            val context = LocalContext.current
            var signInSuccess by remember { mutableStateOf(false) }

            /*
             * NOTIFICATION PERMISSION STATE
             *
             * Manages Android 13+ notification permission request flow.
             * Permission is only requested after successful authentication
             * to provide context about why notifications are valuable.
             */
            val notificationPermission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    rememberPermissionState(
                        permission = POST_NOTIFICATIONS
                    ) { isGranted ->
                        val prefs = NotificationPreferences.getInstance(context)
                        prefs.setNotificationsEnabled(isGranted)
                        navigateToCategories(context)
                    }
                } else null

            /*
             * POST-AUTHENTICATION FLOW
             *
             * Handles complete setup after successful user authentication:
             * 1. Check battery optimization status
             * 2. Request notification permission (if needed)
             * 3. Send welcome notification
             * 4. Schedule personalized notifications
             * 5. Navigate to main app
             */
            LaunchedEffect(signInSuccess) {
                if (signInSuccess) {
                    val prefs = NotificationPreferences.getInstance(context)

                    // Record battery optimization status for Settings guidance
                    prefs.setBatteryRestrictionNoticeShown(
                        !BatteryPermissionHelper.hasUnrestrictedBatteryAccess(context)
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission =
                            NotificationManagerCompat.from(context).areNotificationsEnabled() ||
                                    notificationPermission?.status?.isGranted == true

                        if (!hasPermission) {
                            // Request permission with context (post-authentication)
                            notificationPermission?.launchPermissionRequest()
                        } else {
                            // Permission already granted - complete setup
                            completeNotificationSetup(context, prefs)
                        }
                    } else {
                        // Android 12 and below - no permission needed
                        completeNotificationSetup(context, prefs)
                    }
                }
            }

            // Apply theme and render authentication UI
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    SignInScreen(
                        onSignInSuccess = {
                            FavoritesViewModel.getInstance().resetForNewUser()
                            signInSuccess = true
                        },
                        onSignUpClicked = {
                            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
                        },
                        onForgotPasswordClicked = {
                            startActivity(
                                Intent(
                                    this@SignInActivity,
                                    ForgotPasswordActivity::class.java
                                )
                            )
                        },
                        onLanguageChange = { langCode ->
                            languageViewModel.setLanguage(langCode, context)
                        }
                    )
                }
            }
        }
        keepSplashOnScreen = false
    }

    /**
     * Completes notification setup after authentication.
     * This includes sending welcome notification and scheduling
     * personalized notifications based on user preferences.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 3 Dec 2025
     */
    private fun completeNotificationSetup(context: Context, prefs: NotificationPreferences) {
        NotificationHelper.sendWelcomeNotification(context)
        prefs.setNotificationsEnabled(true)

        // Auto-enable notifications for new users
        if (shouldAutoEnableNotifications()) {
            NotificationScheduler.scheduleNotifications(context)
        }

        navigateToCategories(context)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Welcome Channel"
            val descriptionText = "Notifications to welcome users"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel("welcome_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Determines if notifications should be auto-enabled for the user.
     * New users (no preference set) get notifications by default (opt-out model).
     * Existing users keep their previous preference.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 3 Dec 2025
     */
    private fun shouldAutoEnableNotifications(): Boolean {
        val prefs = NotificationPreferences.getInstance(this)
        return !prefs.hasUserSetPreference()
    }

    /**
     * Navigates to [CategoriesActivity] after authentication setup.
     * Includes brief delay to allow permission dialogs to close cleanly
     * and provide smooth transition experience.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 3 Dec 2025
     */
    private fun navigateToCategories(context: Context) {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(context, CategoriesActivity::class.java)
            context.startActivity(intent)
            finish()
        }, 500)
    }
}
