package com.movito.movito.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.data.model.Movie
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<FavoriteMovie> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class FavoritesViewModel(
    private val repository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        startListening()
    }

    private fun startListening() {
        viewModelScope.launch {
            val result = repository.signInAnonymously()

            if (result.isSuccess) {
                repository.observeFavorites()
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Unknown error"
                            )
                        }
                    }
                    .collect { favorites ->
                        _uiState.update {
                            it.copy(
                                favorites = favorites,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Authentication failed"
                    )
                }
            }
        }
    }

    fun addToFavorites(movie: Movie) {
        viewModelScope.launch {
            repository.addToFavorites(movie)
        }
    }

    fun removeFromFavorites(movieId: Int) {
        viewModelScope.launch {
            repository.removeFromFavorites(movieId)
        }
    }

    suspend fun isFavorite(movieId: Int): Boolean {
        return repository.isFavorite(movieId)
    }
}