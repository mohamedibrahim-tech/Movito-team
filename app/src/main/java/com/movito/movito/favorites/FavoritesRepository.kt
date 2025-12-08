package com.movito.movito.favorites

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.movito.movito.data.model.Movie
import kotlinx.coroutines.tasks.await
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

/**
 * Repository class for managing favorite movies in Firestore database.
 *
 * This repository handles all CRUD operations for user favorites, including:
 * - Adding movies to favorites
 * - Removing movies from favorites
 * - Checking if a movie is favorited
 * - Retrieving current user ID
 *
 * The repository uses Firestore with the following structure:
 * - Collection: `"favorites"`
 * - Document ID: `"${userId}_${movieId}"` (composite key)
 * - Fields: [Movie] object + userId field
 *
 * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
 *
 * @property firestore Firestore database ([FirebaseFirestore]) instance
 * @property auth Firebase Authentication ([FirebaseAuth]) instance
 * @property favoritesCollection Reference to the `"favorites"` collection
 *
 * @since 15 Nov 2025
 *
 * @see Movie for the data model structure
 */
class FavoritesRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    /**
     * Reference to the `"favorites"` collection in Firestore.
     *
     * All favorite movie operations are performed on this collection.
     * Documents are stored with composite IDs in the format: `${userId}_${movieId}`
     */
    private val favoritesCollection = firestore.collection("favorites")

    init {
        // Configure Firestore settings (optional, can be used for enabling persistence)
        try {
            firestore.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .build()
        } catch (e: Exception) {
            // Silent catch - settings might already be configured
        }
    }

    /**
     * Adds a movie to the current user's favorites.
     *
     * This method performs the following operations:
     * 1. Validates the user is authenticated
     * 2. Creates a composite document ID: `${userId}_${movieId}`
     * 3. Stores the movie object in Firestore
     * 4. Adds userId as a separate field for querying
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @param movie The [Movie] object to add to favorites
     * @return [Result] containing:
     * - [success] with [Unit] on successful addition
     * - [failure] with [Exception] if operation fails or user is not logged in
     *
     * @throws FirebaseFirestoreException if Firestore operation fails
     *
     * @since 15 Nov 2025
     */
    suspend fun addToFavorites(movie: Movie): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not logged in"))

            val docId = "${userId}_${movie.id}"

            // Store the movie object
            favoritesCollection
                .document(docId)
                .set(movie)
                .await()

            // Add userId as a separate field for querying
            favoritesCollection
                .document(docId)
                .update("userId", userId)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Removes a movie from the current user's favorites.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @param movieId The ID of the movie to remove
     * @return [Result] containing:
     * - [success] with [Unit] on successful removal
     * - [failure] with [Exception] if operation fails or user is not logged in
     *
     * @throws FirebaseFirestoreException if Firestore operation fails
     *
     * @since 15 Nov 2025
     */
    suspend fun removeFromFavorites(movieId: Int): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not logged in"))

            val docId = "${userId}_$movieId"

            favoritesCollection
                .document(docId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Retrieves the current authenticated user's ID.
     *
     * **Author**: Movito Development Team Member [Basmala Wahid](http://github.com/basmala-wahid)
     *
     * @return The current user's Firebase UID, or empty [String] if no user is logged in
     *
     * @since 22 Nov 2025
     */
    fun currentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
}