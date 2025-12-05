package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.BuildConfig
import com.movito.movito.LanguageManager
import com.movito.movito.data.model.Genre
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class CategoriesUiState(
    val isLoading: Boolean = false,
    val genres: List<Genre> = emptyList(),
    val error: String? = null
)

class CategoriesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val apiKey = BuildConfig.TMDB_API_KEY
    private val currentLanguage = LanguageManager.currentLanguage
    private fun falidToLoadGenresMsg(errorMsg: String?) = if(LanguageManager.currentLanguage.value == "ar")  "تعذر تحميل الفئات: $errorMsg" else "Failed to load genres: $errorMsg"
    private fun unexpectedErrorMsg(errorMsg: String?) = if(LanguageManager.currentLanguage.value == "ar")  "حدث خطأ غير متوقع: $errorMsg" else "An unexpected error occurred: $errorMsg"

    init {
        loadGenres()
    }

    private fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = RetrofitInstance.api.getGenres(apiKey, currentLanguage.value)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        genres = response.genres
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = falidToLoadGenresMsg(e.message)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = unexpectedErrorMsg(e.message)
                    )
                }
            }
        }
    }
}