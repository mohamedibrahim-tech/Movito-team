package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.navigation.AppNavigation
import com.movito.movito.ui.navigation.Screen
import com.movito.movito.viewmodel.AuthViewModel
import com.movito.movito.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !authViewModel.authState.value.isInitialCheckDone
        }

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val authState by authViewModel.authState.collectAsState()

            MovitoTheme(darkTheme = isDarkTheme) {
                if (authState.isInitialCheckDone) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        startDestination = if (authState.user != null) Screen.Categories.route else Screen.SignIn.route
                    )
                }
            }
        }
    }
}
