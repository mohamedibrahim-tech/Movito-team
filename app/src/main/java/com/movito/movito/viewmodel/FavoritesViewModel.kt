package com.movito.movito.viewmodel

import android.app.Activity
import androidx.compose.runtime.Composable
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

/**
 * UI state representing the current state of the favorites screen.
 *
 * This data class holds all state needed for displaying user's favorite movies,
 * including the list of favorites, loading states, and error handling.
 *
 * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
 *
 * @property favorites [List] of favorite movies for the current user
 * @property isLoading Whether the data is currently being loaded
 * @property error Optional error message if something went wrong
 *
 * @since 15 Nov 2025
 */
data class FavoritesUiState(
    val favorites: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * [ViewModel] responsible for managing favorite movies data and UI state.
 *
 * This [ViewModel] provides real-time Firestore synchronization for user favorites
 * with the following features:
 * - Real-time Firestore updates via snapshot listeners
 * - Optimistic UI updates for immediate user feedback
 * - User-specific favorite isolation
 * - Singleton pattern for app-wide state consistency
 *
 * The [ViewModel] uses a singleton pattern to ensure consistent favorite state
 * across the entire application. It automatically cleans up Firestore listeners
 * when destroyed to prevent memory leaks.
 *
 * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
 *
 * @property repository The repository handling Firestore operations
 *
 * @since 15 Nov 2025
 *
 * @see FavoritesRepository for data layer operations
 */
class FavoritesViewModel(
    private val repository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    /**
     * Internal mutable state flow for favorites UI state.
     *
     * This private flow holds the current state and allows controlled updates
     * from within the [ViewModel].
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @since 15 Nov 2025
     */
    private val _uiState = MutableStateFlow(FavoritesUiState())

    /**
     * Public immutable StateFlow exposing the current favorites UI state.
     *
     * Collect this flow in [Composable]s or any [Activity] to get real-time
     * favorites updates as they change in Firestore.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @since 15 Nov 2025
     */
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    /**
     * Firestore listener registration for cleanup.
     *
     * This holds the reference to the active Firestore snapshot listener
     * and is used to properly remove the listener when the ViewModel is cleared.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 24 Nov 2025
     */
    private var listener: ListenerRegistration? = null

    /**
     * Initializes the [ViewModel] and starts listening to Firestore updates.
     *
     * The init block automatically calls [startListening] to establish
     * real-time Firestore synchronization when the [ViewModel] is created.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @since 15 Nov 2025
     */
    init {
        startListening()
    }

    /**
     * Starts listening to Firestore for real-time favorites updates.
     *
     * This method establishes a snapshot listener on the Firestore `"favorites"`
     * collection filtered by the current user ID. It provides:
     * - Real-time updates when favorites are added/removed
     * - Automatic error handling
     * - UI state updates with the latest favorites list
     *
     * The listener is established in [viewModelScope] and automatically
     * cancels when the [ViewModel] is cleared.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @since 15 Nov 2025
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
     * Resets the [ViewModel] when a new user signs in or out.
     *
     * This method ensures proper user separation by:
     * 1. Removing existing Firestore listener to prevent memory leaks
     * 2. Clearing current favorites state
     * 3. Restarting listening for the new user's favorites
     *
     * Call this method after sign-in/sign-out operations to maintain
     * data isolation between users.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 24 Nov 2025
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
     * This method provides immediate UI feedback (optimistic update) before
     * persisting to Firestore, resulting in a smooth user experience.
     *
     * Process:
     * 1. Optimistically updates UI by adding movie to local state
     * 2. Persists to Firestore asynchronously
     * 3. New movies are added to beginning of list (most recent first)
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @param movie The movie to add to favorites
     *
     * @since 15 Nov 2025
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
     * This method provides immediate UI feedback by removing the movie
     * from local state before deleting from Firestore.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @param movieId The ID of the movie to remove from favorites
     *
     * @since 15 Nov 2025
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
     * This is a local check only - it doesn't query Firestore. Use this
     * for quick UI state checks like showing/hoving favorite icons.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @param movieId The ID of the movie to check
     * @return `true` if the movie is in the current favorites list, `false` otherwise
     *
     * @since 15 Nov 2025
     */
    suspend fun isFavorite(movieId: Int): Boolean {
        return _uiState.value.favorites.any { it.id == movieId }
    }

    /**
     * Cleans up resources when [ViewModel] is destroyed.
     *
     * This override ensures proper cleanup by removing the Firestore
     * listener to prevent memory leaks.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 24 Nov 2025
     */
    override fun onCleared() {
        // Remove Firestore listener to prevent memory leaks
        listener?.remove()
        super.onCleared()
    }

    /**
     * Companion object implementing the singleton pattern for [FavoritesViewModel].
     *
     * This ensures the same [ViewModel] instance is used across the app
     * for consistent state management. The singleton pattern is appropriate
     * here because favorite state needs to be consistent across all screens.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @since Nov 22 2025
     */
    companion object {
        /**
         * [Volatile] instance reference for thread-safe singleton initialization.
         *
         * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
         *
         * @since Nov 22 2025
         */
        @Volatile
        private var instance: FavoritesViewModel? = null

        /**
         * Returns the singleton instance of [FavoritesViewModel].
         *
         * This method uses double-checked locking for thread safety:
         * 1. First check without synchronization (performance)
         * 2. Synchronized block for thread-safe initialization
         * 3. Second check inside synchronized block
         *
         * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
         *
         * @return The shared [FavoritesViewModel] instance
         *
         * @since Nov 22 2025
         */
        fun getInstance(): FavoritesViewModel {
            return instance ?: synchronized(this) {
                instance ?: FavoritesViewModel().also { instance = it }
            }
        }
    }
}