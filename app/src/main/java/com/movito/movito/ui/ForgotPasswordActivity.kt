package com.movito.movito.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import com.movito.movito.MovitoApplication
import com.movito.movito.viewmodel.AuthViewModel
import android.app.Activity

/**
 * [Activity] for handling password reset requests.
 *
 * This activity provides a simple interface for users to request a password reset email.
 * Users enter their email address and receive a password reset link via email.
 *
 * Features:
 * - Email validation
 * - Loading state during request
 * - Success/error feedback
 * - Theme and language support
 *
 * * **Author**: Movito Development Team Member [Yossef Sayed](https://github.com/yossefsayedhassan)
 *
 * @since 15 Nov 2025
 *
 * @see ForgotPasswordScreen
 * @see AuthViewModel.sendPasswordResetEmail
 */
class ForgotPasswordActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    /**
     * Sets up the activity with edge-to-edge display and theme/language preferences.
     *
     * * **Author**: Movito Development Team Member [Yossef Sayed](https://github.com/yossefsayedhassan)
     *
     * @since 15 Nov 2025
     *
     * @param savedInstanceState Previously saved instance state, or null
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Load theme/language preference
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            // Re-compose when theme changes
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    Scaffold {
                        ForgotPasswordScreen(
                            modifier = Modifier.padding(it),
                            onPasswordResetSent = {
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Updates the base context with the saved language preference for proper localization.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 1 Dec 2025
     *
     * @param newBase The base context
     */
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = MovitoApplication.getSavedLanguage(newBase)
        val updatedContext = MovitoApplication.updateBaseContextLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
    }
}
