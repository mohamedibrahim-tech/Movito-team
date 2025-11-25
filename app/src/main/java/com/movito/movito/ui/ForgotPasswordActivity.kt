package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.ThemeViewModel
import androidx.compose.runtime.key

class ForgotPasswordActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
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
}
