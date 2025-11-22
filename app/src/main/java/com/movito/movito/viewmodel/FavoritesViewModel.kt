package com.movito.movito.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.data.model.Movie
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

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
            val userId = repository.currentUserId()
            if (userId.isEmpty()) return@launch

            // 1. Optimistic Update - حدث الـ UI فوراً
            val newFavorite = FavoriteMovie(
                id = "${userId}_${movie.id}",
                movieId = movie.id,
                title = movie.title,
                releaseDate = movie.releaseDate,
                posterPath = movie.posterPath,
                voteAverage = movie.voteAverage,
                overview = movie.overview,
                userId = userId,
                addedAt = Date()
            )

            _uiState.update { currentState ->
                val alreadyExists = currentState.favorites.any { it.movieId == movie.id }
                if (alreadyExists) {
                    currentState
                } else {
                    currentState.copy(
                        favorites = listOf(newFavorite) + currentState.favorites
                    )
                }
            }

            // 2. احفظ في Firestore
            repository.addToFavorites(movie)
        }
    }

    fun removeFromFavorites(movieId: Int) {
        viewModelScope.launch {
            // 1. Optimistic Update - احذف من الـ UI فوراً
            _uiState.update { currentState ->
                currentState.copy(
                    favorites = currentState.favorites.filter { it.movieId != movieId }
                )
            }

            // 2. احذف من Firestore
            repository.removeFromFavorites(movieId)
        }
    }

    suspend fun isFavorite(movieId: Int): Boolean {
        return _uiState.value.favorites.any { it.movieId == movieId }
    }

    companion object {
        @Volatile
        private var instance: FavoritesViewModel? = null

        fun getInstance(): FavoritesViewModel {
            return instance ?: synchronized(this) {
                instance ?: FavoritesViewModel().also { instance = it }
            }
        }
    }
}