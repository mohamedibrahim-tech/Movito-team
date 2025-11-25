package com.movito.movito.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.data.ThemeDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themeDataStore = ThemeDataStore(application.applicationContext)

    private val _isDarkTheme = MutableStateFlow(themeDataStore.isDarkThemeSync())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            themeDataStore.isDarkTheme.collect { isDark ->
                _isDarkTheme.value = isDark
            }
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeDataStore.saveThemePreference(isDark)
        }
    }
}