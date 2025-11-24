package com.movito.movito.favorites

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.movito.movito.data.model.Movie
import kotlinx.coroutines.tasks.await

class FavoritesRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val favoritesCollection = firestore.collection("favorites")

    init {
        try {
            firestore.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .build()
        } catch (e: Exception) {
        }
    }

    suspend fun addToFavorites(movie: Movie): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not logged in"))

            val favoriteMovie = FavoriteMovie(
                movieId = movie.id,
                title = movie.title,
                releaseDate = movie.releaseDate,
                posterPath = movie.posterPath,
                voteAverage = movie.voteAverage,
                overview = movie.overview,
                userId = userId,
                addedAt = com.google.firebase.Timestamp.now().toDate()
            )

            val docId = "${userId}_${movie.id}"

            favoritesCollection
                .document(docId)
                .set(favoriteMovie)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun isFavorite(movieId: Int): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val docId = "${userId}_$movieId"

            favoritesCollection
                .document(docId)
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            false
        }
    }
    fun currentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
}