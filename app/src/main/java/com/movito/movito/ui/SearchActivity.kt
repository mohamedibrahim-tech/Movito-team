package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.movito.movito.theme.MovitoTheme

/*
   ملف Activity جديد لشاشة البحث
 */
class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemIsDark = isSystemInDarkTheme()
            MovitoTheme(darkTheme = systemIsDark) {
                //  بنستدعي الـ Composable بتاع شاشة البحث
                SearchScreen()
            }
        }
    }
}