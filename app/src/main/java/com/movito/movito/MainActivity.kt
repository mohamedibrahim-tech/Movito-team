package com.movito.movito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.movito.movito.ui.HomeScreen
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()

        enableEdgeToEdge()
        setContent {
            MovitoTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.isLoading
        }
    }
}