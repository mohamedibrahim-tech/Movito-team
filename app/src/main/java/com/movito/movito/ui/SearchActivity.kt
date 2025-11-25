package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.ThemeViewModel
import androidx.compose.runtime.key

class SearchActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    SearchScreen()
                }
            }

        }
    }
}