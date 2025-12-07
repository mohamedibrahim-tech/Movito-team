package com.movito.movito.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.movito.movito.MovitoApplication
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.AuthViewModel
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import android.app.Activity

/**
 * [Activity] responsible for user registration/sign-up flow.
 *
 * This [Activity]:
 * 1. Provides a sign-up form for new users
 * 2. Signs out any existing user for clean registration flow
 * 3. Observes authentication state and navigates to [CategoriesActivity] upon successful registration
 * 4. Supports theme and language preferences
 * 5. Provides navigation to [SignInActivity] for existing users
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
 *
 * @since 14 Nov 2025
 *
 * @see SignUpScreen
 * @see AuthViewModel
 * @see ThemeViewModel
 * @see LanguageViewModel
 */
class SignUpActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    /**
     * Sets up the activity with edge-to-edge display and theme/language preferences.
     * Also signs out any existing user to ensure clean registration flow.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @param savedInstanceState Previously saved instance state, or null
     *
     * @since 14 Nov 2025
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sign out existing user to ensure clean registration flow
        authViewModel.signOut()

        enableEdgeToEdge()
        // Load theme preference
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            // Re-compose when theme changes
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {

                    val authState by authViewModel.authState.collectAsState()
                    val user = authState.user

                    /**
                     * Observes user authentication state and navigates to [CategoriesActivity]
                     * when a verified user is detected.
                     */
                    LaunchedEffect(user) {
                        Log.d("DEBUG", "User = $user | Verified = ${user?.isEmailVerified}")

                        if (user != null && user.isEmailVerified) {
                            startActivity(
                                Intent(
                                    this@SignUpActivity,
                                    CategoriesActivity::class.java
                                )
                            )
                            finish()
                        }
                    }

                    Scaffold { padding ->
                        SignUpScreen(
                            modifier = Modifier.padding(padding),
                            onSignUpSuccess = {
                                // Empty success handler as navigation is handled by LaunchedEffect
                            },
                            onSignInClicked = {
                                startActivity(
                                    Intent(
                                        this@SignUpActivity,
                                        SignInActivity::class.java
                                    )
                                )
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
     * @param newBase The base context
     *
     * @since 1 Dec 2025
     */
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = MovitoApplication.getSavedLanguage(newBase)
        val updatedContext = MovitoApplication.updateBaseContextLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
    }
}