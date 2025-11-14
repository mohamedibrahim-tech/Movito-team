package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.movito.movito.theme.MovitoTheme

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovitoTheme {
                SignUpScreen(
                    onSignUp = { _, _ -> },
                    onSignInClicked = { finish() }
                )
            }
        }
    }
}