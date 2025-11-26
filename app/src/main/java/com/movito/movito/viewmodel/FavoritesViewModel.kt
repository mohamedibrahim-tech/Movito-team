package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.movito.movito.data.model.Movie
import com.movito.movito.favorites.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI state representing the current state of the favorites screen.
 *
 * @property favorites List of favorite movies for the current user
 * @property isLoading Whether the data is currently being loaded
 * @property error Optional error message if something went wrong
 */
data class FavoritesUiState(
    val favorites: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel responsible for managing favorite movies data.
 *
 * This ViewModel:
 * - Listens to real-time Firestore updates for the current user's favorites
 * - Handles optimistic UI updates for better user experience
 * - Provides methods to add/remove favorites with immediate UI feedback
 * - Uses singleton pattern to share state across the app
 *
 * @property repository The repository handling Firestore operations
 */
class FavoritesViewModel(
    private val repository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    // Mutable state flow for internal state management
    private val _uiState = MutableStateFlow(FavoritesUiState())

    /**
     * Exposed immutable state flow for UI observation.
     * Collect this in your Composables to get real-time favorites updates.
     */
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    // Firestore listener registration for cleanup
    private var listener: ListenerRegistration? = null

    init {
        // Start listening to Firestore updates when ViewModel is created
        startListening()
    }

    /**
     * Starts listening to Firestore for real-time favorites updates.
     *
     * This method:
     * - Gets the current user ID from Firebase Auth
     * - Sets up a snapshot listener on the favorites collection
     * - Updates UI state when favorites change
     * - Handles errors gracefully
     */
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
                        snapshot?.documents?.mapNotNull { it.toObject(Movie::class.java) }
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

    /**
     * Resets the ViewModel when a new user signs in.
     *
     * This method:
     * - Removes the existing Firestore listener
     * - Clears the current favorites state
     * - Restarts listening for the new user's favorites
     *
     * Call this method after sign-in/sign-out to ensure proper user separation.
     */
    fun resetForNewUser() {
        // Remove old listener to prevent memory leaks
        listener?.remove()
        listener = null

        // Clear current state and show loading
        _uiState.value = FavoritesUiState(isLoading = true)

        // Restart listening for the new user
        startListening()
    }

    /**
     * Adds a movie to favorites with optimistic UI update.
     *
     * This method provides immediate UI feedback by:
     * 1. First updating the UI optimistically
     * 2. Then persisting to Firestore
     *
     * @param movie The movie to add to favorites
     */
    fun addToFavorites(movie: Movie) {
        viewModelScope.launch {
            val userId = repository.currentUserId()
            // Return if no user is logged in
            if (userId.isEmpty()) return@launch

            // 1. OPTIMISTIC UPDATE - Update UI immediately for better UX
            _uiState.update { currentState ->
                val alreadyExists = currentState.favorites.any { it.id == movie.id }
                // Don't add duplicate if already exists
                if (alreadyExists) {
                    currentState
                } else {
                    // Add new favorite to the beginning of the list (most recent first)
                    currentState.copy(
                        favorites = listOf(movie) + currentState.favorites
                    )
                }
            }

            // 2. PERSIST TO FIRESTORE - Save to database after UI update
            repository.addToFavorites(movie)
        }
    }

    /**
     * Removes a movie from favorites with optimistic UI update.
     *
     * This method provides immediate UI feedback by:
     * 1. First removing from UI
     * 2. Then removing from Firestore
     *
     * @param movieId The ID of the movie to remove from favorites
     */
    fun removeFromFavorites(movieId: Int) {
        viewModelScope.launch {
            // 1. OPTIMISTIC UPDATE - Remove from UI immediately
            _uiState.update { currentState ->
                currentState.copy(
                    favorites = currentState.favorites.filter { it.id != movieId }
                )
            }

            // 2. REMOVE FROM FIRESTORE - Delete from database after UI update
            repository.removeFromFavorites(movieId)
        }
    }

    /**
     * Checks if a movie is currently in favorites.
     *
     * This is a local check only - it doesn't query Firestore.
     * Use this for quick UI state checks.
     *
     * @param movieId The ID of the movie to check
     * @return true if the movie is in the current favorites list, false otherwise
     */
    suspend fun isFavorite(movieId: Int): Boolean {
        return _uiState.value.favorites.any { it.id == movieId }
    }

    /**
     * Clean up resources when ViewModel is destroyed.
     */
    override fun onCleared() {
        // Remove Firestore listener to prevent memory leaks
        listener?.remove()
        super.onCleared()
    }

    /**
     * Companion object implementing the singleton pattern.
     *
     * This ensures the same ViewModel instance is used across the app
     * for consistent state management.
     */
    companion object {
        @Volatile
        private var instance: FavoritesViewModel? = null

        /**
         * Returns the singleton instance of FavoritesViewModel.
         *
         * @return The shared FavoritesViewModel instance
         */
        fun getInstance(): FavoritesViewModel {
            return instance ?: synchronized(this) {
                instance ?: FavoritesViewModel().also { instance = it }
            }
        }
    }
}