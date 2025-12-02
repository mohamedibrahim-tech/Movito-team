package com.movito.movito.ui

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
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.AuthViewModel
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel

class SettingsActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()
    private val languageChangeListener = {
        // Restart activity when language changes
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.change_language_in,
            R.anim.change_language_out
        )
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MovitoApplication.LanguageChangeObserver.addListener(languageChangeListener)
        enableEdgeToEdge()
        languageViewModel.loadLanguagePreference(this)
        setContent {
            val authState by authViewModel.authState.collectAsState()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            var notificationsState by remember {
                mutableStateOf(
                    NotificationManagerCompat.from(this).areNotificationsEnabled()
                )
            }

            LaunchedEffect(authState.user) {
                if (authState.user == null && authState.isInitialCheckDone) {
                    val intent = Intent(this@SettingsActivity, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    SettingsScreen(
                        onThemeToggle = { newTheme ->
                            themeViewModel.toggleTheme(newTheme)
                        },
                        onSignOut = {
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

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = MovitoApplication.getSavedLanguage(newBase)
        val updatedContext = MovitoApplication.updateBaseContextLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        MovitoApplication.LanguageChangeObserver.removeListener(languageChangeListener)
    }
}