package com.movito.movito.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.FavoritesViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import androidx.compose.runtime.key

class SignInActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var keepSplashOnScreen by mutableStateOf(true)
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            // User is logged in, navigate to CategoriesActivity
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish() // Finish SignInActivity so user can't go back to it
            return // Stop further execution of onCreate
        }

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    Scaffold {
                        SignInScreen(
                            modifier = Modifier.padding(it),
                            onSignInSuccess = {
                                FavoritesViewModel.getInstance()
                                    .resetForNewUser() // reset FavoritesViewMode
                                val intent = Intent(this, CategoriesActivity::class.java)
                                startActivity(intent)
                                finish()
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
}