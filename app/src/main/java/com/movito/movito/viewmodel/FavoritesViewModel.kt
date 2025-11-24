package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.movito.movito.data.model.Movie
import com.movito.movito.favorites.FavoriteMovie
import com.movito.movito.favorites.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private var listener: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        viewModelScope.launch {
            // Get current user ID, return if no user is logged in
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            // Create Firestore listener for real-time updates
            listener = FirebaseFirestore.getInstance().collection("favorites")
                .whereEqualTo("userId", userId).addSnapshotListener { snapshot, error ->
                    // Handle errors
                    if (error != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Unknown error"
                            )
                        }
                        return@addSnapshotListener
                    }

                    // Convert documents to FavoriteMovie objects
                    val list =
                        snapshot?.documents?.mapNotNull { it.toObject(FavoriteMovie::class.java) }
                            ?: emptyList()

                    // Update UI state with new favorites list
                    _uiState.update {
                        it.copy(
                            favorites = list,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun resetForNewUser() {
        // Remove old listener
        listener?.remove()
        listener = null

        // Clear current state
        _uiState.value = FavoritesUiState(isLoading = true)

        // Restart listening for new user
        startListening()
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

    override fun onCleared() {
        // Remove Firestore listener to prevent memory leaks
        listener?.remove()
        super.onCleared()
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