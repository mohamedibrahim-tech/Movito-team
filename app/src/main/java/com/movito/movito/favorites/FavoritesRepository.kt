package com.movito.movito.favorites

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.movito.movito.data.model.Movie
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    suspend fun signInAnonymously(): Result<String> {
        return try {
            if (auth.currentUser == null) {
                val result = auth.signInAnonymously().await()
                Result.success(result.user?.uid ?: "")
            } else {
                Result.success(auth.currentUser?.uid ?: "")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeFavorites(): Flow<List<FavoriteMovie>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            val userId = auth.currentUser?.uid

            if (userId == null) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            listenerRegistration = favoritesCollection
                .whereEqualTo("userId", userId)
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->

                    if (exception != null) {
                        close(exception)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val favorites = snapshot.documents.mapNotNull { document ->
                            try {
                                val favorite = document.toObject(FavoriteMovie::class.java)
                                favorite?.copy(id = document.id)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(favorites)
                    } else {
                        trySend(emptyList())
                    }
                }

        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            listenerRegistration?.remove()
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