package com.movito.movito.ui

import android.content.Intent
import android.os.Bundle
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


class SignUpActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            MovitoTheme {

                val authState by authViewModel.authState.collectAsState()

                LaunchedEffect(authState.user) {
                    if (authState.user != null) {
                        startActivity(Intent(this@SignUpActivity, CategoriesActivity::class.java))
                        finish()
                    }
                }

                Scaffold { padding ->
                    SignUpScreen(
                        modifier = Modifier.padding(padding),
                        onSignInClicked = {
                            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                            finish()
                        },
                        onGoogleSignUpClicked = {}
                    )
                }
            }
        }
    }
}