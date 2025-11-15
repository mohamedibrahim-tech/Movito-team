package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.movito.movito.theme.MovitoTheme

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovitoTheme {
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
