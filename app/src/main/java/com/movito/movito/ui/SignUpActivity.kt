package com.movito.movito.ui

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
import androidx.compose.ui.Modifier
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.AuthViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import androidx.compose.runtime.key

class SignUpActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel.signOut()

        enableEdgeToEdge()

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {

                    val authState by authViewModel.authState.collectAsState()
                    val user = authState.user

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
}