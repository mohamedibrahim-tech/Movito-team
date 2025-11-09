package com.movito.movito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.movito.movito.ui.HomeScreen
import com.movito.movito.theme.MovitoTheme

class MainActivity : ComponentActivity() {

    // استدعينا الـ ViewModel هنا عشان نراقبه
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  خدنا نسخة من الـ splashScreen
        val splashScreen = installSplashScreen()

        enableEdgeToEdge()
        setContent {
            MovitoTheme {
                //  مررنا الـ ViewModel للـ HomeScreen
                HomeScreen(viewModel = viewModel)
            }
        }

        //  خلي الـ Splash معروض طول ما الـ ViewModel بيحمل
        // (isLoading = true)
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.isLoading
        }
    }
}