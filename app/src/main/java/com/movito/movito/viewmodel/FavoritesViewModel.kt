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

    private val _pendingOperations = MutableStateFlow<Set<Int>>(emptySet())

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
                        // دمج البيانات من Firestore مع العمليات المعلقة
                        _uiState.update { currentState ->
                            // خلي الـ favorites اللي عندنا محلياً ولسه بتتضاف
                            val localPendingFavorites = currentState.favorites.filter {
                                _pendingOperations.value.contains(it.movieId)
                            }

                            // دمجهم مع البيانات من Firestore
                            val mergedFavorites = (localPendingFavorites + favorites)
                                .distinctBy { it.movieId }
                                .sortedByDescending { it.addedAt }

                            currentState.copy(
                                favorites = mergedFavorites,
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

            // 1. حدّث الـ UI فوراً (Optimistic Update)
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
                currentState.copy(
                    favorites = listOf(newFavorite) + currentState.favorites
                )
            }

            // 2. بعدين احفظ في Firestore
            val result = repository.addToFavorites(movie)

            // 3. لو فشل، ارجع للحالة القديمة
            if (result.isFailure) {
                _uiState.update { currentState ->
                    currentState.copy(
                        favorites = currentState.favorites.filter { it.movieId != movie.id }
                    )
                }
            }
        }
    }

    fun removeFromFavorites(movieId: Int) {
        viewModelScope.launch {
            // 1. حدّث الـ UI فوراً (Optimistic Update)
            val removedFavorite = _uiState.value.favorites.find { it.movieId == movieId }

            _uiState.update { currentState ->
                currentState.copy(
                    favorites = currentState.favorites.filter { it.movieId != movieId }
                )
            }

            // 2. بعدين احذف من Firestore
            val result = repository.removeFromFavorites(movieId)

            // 3. لو فشل، ارجع الـ favorite تاني
            if (result.isFailure && removedFavorite != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        favorites = listOf(removedFavorite) + currentState.favorites
                    )
                }
            }
        }
    }

    suspend fun isFavorite(movieId: Int): Boolean {
        // تحقق من الـ state المحلي الأول (أسرع)
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