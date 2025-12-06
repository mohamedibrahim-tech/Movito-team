package com.movito.movito.notifications

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.NetworkType.CONNECTED
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.movito.movito.BuildConfig
import com.movito.movito.LanguageManager
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.tasks.await

/**
 * MAIN PURPOSE: Background worker that generates and sends personalized
 * movie suggestions based on user's favorite movies.
 *
 * KEY FEATURES:
 * - Fetches user's favorite movies from Firestore
 * - Gets recommendations from TMDB API using Retrofit
 * - Implements fallback strategies when primary recommendations fail
 * - Handles battery optimization warnings
 * - Runs on [NetworkType.CONNECTED] constraint
 *
 * WORKFLOW:
 * 1. Check user authentication → skip if no user
 * 2. Fetch user's favorite movies from Firestore
 * 3. Pick random favorite, get TMDB recommendations
 * 4. If successful → send notification via [NotificationHelper]
 * 5. If no recommendations → try fallback strategies
 *
 * SCHEDULING: Controlled by [NotificationScheduler]
 *
 * NETWORK: Requires [NetworkType.CONNECTED]
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 4 Dec 2025
 */
class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params)
{

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val apiKey = BuildConfig.TMDB_API_KEY

    companion object {
        private const val TAG = "NotificationWorker"
    }

    /**
     * Main work method called by [androidx.work.WorkManager] when scheduled.
     *
     * RETRY LOGIC:
     * - Network errors ([java.net.UnknownHostException], [java.net.SocketTimeoutException]): Try next cycle
     * - Other exceptions: Retry with exponential backoff
     *
     * PERFORMANCE: Tries up to 5 different favorites before using fallback
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @return
     * - [Result.success] after attempting to send notification
     * - [Result.retry] for retryable errors
     * - [Result.success] for network errors (try next cycle)
     *
     * @since 4 Dec 2025
     */
    override suspend fun doWork(): Result {
        Log.d(TAG, "🔔 NotificationWorker started")

        // Check if we have battery permission for reliable scheduling
        val hasBatteryPermission = BatteryPermissionHelper.hasUnrestrictedBatteryAccess(applicationContext)
        if (!hasBatteryPermission) {
            Log.d(TAG, "⚠️ Battery optimization restrictions may affect notification timing")
        }

        return try {
            // Get current user
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.d(TAG, "❌ No user logged in, skipping notification")
                return Result.success()
            }

            Log.d(TAG, "👤 Getting favorites for user: $userId")

            // Get user's favorite movies
            val favorites = getFavorites(userId)
            Log.d(TAG, "📚 Found ${favorites.size} favorites")

            var notificationSent = false
            var attempts = 0
            val maxAttempts = 5 // Try up to 5 different favorites

            // Try to find a favorite with recommendations
            while (!notificationSent && attempts < maxAttempts && favorites.isNotEmpty()) {
                attempts++

                // Pick a random favorite movie
                val favoriteMovie = favorites.random()
                Log.d(TAG, "🎯 Attempt $attempts: Selected favorite movie: ${favoriteMovie.title} (ID: ${favoriteMovie.id})")

                // Get recommendations for this favorite movie
                Log.d(TAG, "🔍 Getting recommendations for movie ID: ${favoriteMovie.id}")
                val recommendations = getMovieRecommendations(favoriteMovie.id)
                Log.d(TAG, "📋 Found ${recommendations.size} recommendations")

                if (recommendations.isNotEmpty()) {
                    // Pick a random recommendation from the 3 top rated
                    val recommendedMovie = recommendations.sortedBy { it.voteAverage }.takeLast(5).random()
                    Log.d(TAG, "✅ Sending notification: ${favoriteMovie.title} -> ${recommendedMovie.title}")
                    NotificationHelper.sendMovieSuggestionNotification(
                        context = applicationContext,
                        recommendedMovie = recommendedMovie,
                        customMessage = context.getString(
                            R.string.notification_recommendation_based_on_fav_recommendation,
                            favoriteMovie.title,
                            recommendedMovie.title
                        ),
                    )

                    notificationSent = true
                    Log.d(TAG, "🎉 Notification sent successfully!")
                } else {
                    Log.d(TAG, "❌ No recommendations for ${favoriteMovie.title}, trying next favorite...")
                }
            }

            // If no favorites with recommendations found, try fallback strategies
            if (!notificationSent) {
                Log.d(TAG, "🔄 No recommendations found, trying fallback strategies...")
                sendFallbackNotification(favorites)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error in NotificationWorker: ${e.message}", e)
            // Don't retry on network errors to avoid battery drain
            if (e is java.net.UnknownHostException || e is java.net.SocketTimeoutException) {
                Result.success() // Try again in next cycle
            } else {
                Result.retry() // Retry for other errors
            }
        }
    }

    /**
     * Fallback notification strategies when no direct recommendations found.
     *
     * STRATEGIES (in order):
     * 1. Find a top rated movies from same genre as a favorite movie
     * 2. Get a top rated popular movie from TMDB
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param favorites [List] of user's favorite [Movie]s
     * @since 4 Dec 2025
     */
    private suspend fun sendFallbackNotification(favorites: List<Movie>) {
        try {
            var movieToRecommend: Movie? = null
            var fallbackMessage = context.getString(R.string.notification_general_msg)

            // Strategy 1: If user has favorites, find movies from same genre
            if (favorites.isNotEmpty()) {
                val favoriteMovie = favorites.random()

                // Try to get movies from the same genre
                if (!favoriteMovie.genreIds.isNullOrEmpty()) {
                    val genreId = favoriteMovie.genreIds.random()
                    val genreMovies = getMoviesByGenre(genreId)
                    if (genreMovies.isNotEmpty()) {
                        movieToRecommend = genreMovies.sortedBy { it.voteAverage }.takeLast(5).random()
                        fallbackMessage = context.getString(
                            R.string.notification_recommend_based_on_fav_genre,
                            favoriteMovie.title
                        )
                    } 
                }
            }

            // Strategy 2: If still no movie, get a popular movie
            if (movieToRecommend == null)
                movieToRecommend = getPopularMovie()

            // Send fallback notification
            if (movieToRecommend != null)
                NotificationHelper.sendMovieSuggestionNotification(
                    context = applicationContext,
                    customMessage = fallbackMessage,
                    recommendedMovie = movieToRecommend
                )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in fallback notification: ${e.message}", e)
        }
    }

    /**
     * Fetches user's favorite [Movie]s from Firestore.
     *
     * FIRESTORE PATH: "favorites" collection
     * QUERY: WHERE `userId == currentUserId`
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param userId Firebase Authentication user ID
     * @return [List] of [Movie] objects or empty list on error
     * @since 4 Dec 2025
     */
    private suspend fun getFavorites(userId: String): List<Movie> {
        return try {
            firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Movie::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting favorites: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches movie recommendations from TMDB API.
     *
     * API ENDPOINT: `/movie/{movie_id}/recommendations`
     *
     * LIMIT: Returns the recommendations
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param movieId TMDB movie ID to get recommendations for
     * @return [List] of recommended [Movie]s or empty list on error
     * @since 4 Dec 2025
     */
    private suspend fun getMovieRecommendations(movieId: Int): List<Movie> {
        return try {
            val response = RetrofitInstance.api.getMovieRecommendations(
                movieId = movieId,
                apiKey = apiKey,
                language = LanguageManager.currentLanguage.value,
            )
            response.results
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommendations: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches movies by genre from TMDB API.
     *
     * API ENDPOINT: `/discover/movie`
     *
     * PARAMS: `with_genres={genreId}`
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param genreId TMDB genre ID to filter by
     * @return [List] of [Movie]s in genre or empty list on error
     * @since 4 Dec 2025
     */
    private suspend fun getMoviesByGenre(genreId: Int): List<Movie> {
        return try {
            val response = RetrofitInstance.api.discoverMoviesByGenre(
                apiKey = apiKey,
                page = 1,
                genreId = genreId,
                language = LanguageManager.currentLanguage.value,
            )
            response.results
        } catch (e: Exception) {
            Log.e(TAG, "Error getting movies by genre: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches a popular movie from TMDB as last-resort fallback.
     *
     * API ENDPOINT: `/discover/movie`
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @return Random popular top rated [Movie] from a random [com.movito.movito.data.model.Genre] or `null` on error
     * @since 4 Dec 2025
     */
    private suspend fun getPopularMovie(): Movie? {
        return try {
            // Get a random genre id
            val randomGenreId = RetrofitInstance.api.getGenres(apiKey = apiKey).genres.random().id
            // Use discover movies with popular sorting
            val response = RetrofitInstance.api.discoverMoviesByGenre(
                apiKey = apiKey,
                page = 1,
                genreId = randomGenreId,
                language = LanguageManager.currentLanguage.value,
            )
            response.results.sortedBy { it.voteAverage }.takeLast(5).randomOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting popular movie: ${e.message}", e)
            null
        }
    }
}