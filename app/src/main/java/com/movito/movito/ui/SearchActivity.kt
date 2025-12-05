package com.movito.movito.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.movito.movito.MovitoApplication
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.LanguageViewModel
import com.movito.movito.viewmodel.ThemeViewModel

class SearchActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()
    private val languageChangeListener = {
        // Restart activity when language changes
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MovitoApplication.LanguageChangeObserver.addListener(languageChangeListener)
        enableEdgeToEdge()
        // Load theme preference
        themeViewModel.loadThemePreference(this)
        languageViewModel.loadLanguagePreference(this)

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    SearchScreen()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = MovitoApplication.getSavedLanguage(newBase)
        val updatedContext = MovitoApplication.updateBaseContextLocale(newBase, savedLanguage)
        super.attachBaseContext(updatedContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        MovitoApplication.LanguageChangeObserver.removeListener(languageChangeListener)
    }
}