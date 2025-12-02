package com.movito.movito.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseAuth
import com.movito.movito.MovitoApplication
import com.movito.movito.NotificationPreferences
import com.movito.movito.sendWelcomeNotification
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.FavoritesViewModel
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel

@OptIn(ExperimentalPermissionsApi::class)
class SignInActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = MovitoApplication.getSavedLanguage(newBase)
        val updatedContext = MovitoApplication.updateBaseContextLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // SPLASH SCREEN MUST BE INSTALLED BEFORE super.onCreate()
        val splashScreen = installSplashScreen()
        var keepSplashOnScreen by mutableStateOf(true)
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }
        // SUPER MUST BE CALLED AFTER installSplashScreen BUT BEFORE ANYTHING ELSE
        super.onCreate(savedInstanceState)

        //  Notification Channel
        createNotificationChannel()

        // Check if user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        languageViewModel.loadLanguagePreference(this)
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val context = LocalContext.current
            var signInSuccess by remember { mutableStateOf(false) }
            var permissionRequested by remember { mutableStateOf(false) }

            // Permission state
            val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                rememberPermissionState(
                    permission = Manifest.permission.POST_NOTIFICATIONS
                ) { isGranted ->
                    val prefs = NotificationPreferences.getInstance(context)
                    prefs.setNotificationsEnabled(isGranted)

                    if (isGranted && signInSuccess) {
                        sendWelcomeNotification(context)
                    }

                    if (!isGranted) {
                        Toast.makeText(
                            context,
                            "You will not receive notifications",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                null
            }

            LaunchedEffect(Unit) {
                if (!permissionRequested) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when {
                            notificationPermission?.status?.isGranted == true -> {
                                val prefs = NotificationPreferences.getInstance(context)
                                prefs.setNotificationsEnabled(true)
                            }
                            notificationPermission?.status?.shouldShowRationale == true -> {
                                notificationPermission.launchPermissionRequest()
                            }
                            else -> {
                                notificationPermission?.launchPermissionRequest()
                            }
                        }
                    } else {
                        val prefs = NotificationPreferences.getInstance(context)
                        prefs.setNotificationsEnabled(true)
                    }
                    permissionRequested = true
                }
            }

            LaunchedEffect(signInSuccess) {
                if (signInSuccess) {
                    val prefs = NotificationPreferences.getInstance(context)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (notificationPermission?.status?.isGranted == true && prefs.isNotificationsEnabled()) {
                            sendWelcomeNotification(context)
                        }
                    } else {
                        if (prefs.isNotificationsEnabled()) {
                            sendWelcomeNotification(context)
                        }
                    }
                }
            }

            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    Scaffold {
                        SignInScreen(
                            modifier = Modifier.padding(it),
                            onSignInSuccess = {
                                FavoritesViewModel.getInstance().resetForNewUser()
                                signInSuccess = true

                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    val intent = Intent(context, CategoriesActivity::class.java)
                                    context.startActivity(intent)
                                    finish()
                                }, 500)
                            },
                            onSignUpClicked = {
                                startActivity(Intent(this, SignUpActivity::class.java))
                            },
                            onForgotPasswordClicked = {
                                startActivity(Intent(this, ForgotPasswordActivity::class.java))
                            }
                        )
                    }
                }
            }
        }
        keepSplashOnScreen = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Welcome Channel"
            val descriptionText = "Notifications to welcome users"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel("welcome_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}