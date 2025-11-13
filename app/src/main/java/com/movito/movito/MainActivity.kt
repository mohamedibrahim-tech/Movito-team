package com.movito.movito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
// (1) --- تعديل الـ imports عشان تطابق الباكدجات الجديدة ---
import com.movito.movito.ui.HomeScreen
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    //    الـ ViewModel بقى بيستخدم 'by viewModels()'
    // (ده محتاج مكتبة 'activity-ktx' اللي ضفناها في الـ gradle)
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