package com.movito.movito.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result.retry
import androidx.work.ListenableWorker.Result.success
import androidx.work.NetworkType.CONNECTED
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.movito.movito.BuildConfig
import com.movito.movito.LanguageManager
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.RetrofitInstance
import kotlinx.coroutines.tasks.await
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 *
 * KEY FEATURES:
 * - Fetches user's favorite movies from Firestore
 * - Gets recommendations from TMDB API using Retrofit
 * - Implements fallback strategies when primary recommendations fail
 * - Handles battery optimization warnings
 * - Runs on [CONNECTED] constraint
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
 * NETWORK: Requires [CONNECTED]
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 5 Dec 2025
 */
class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val apiKey = BuildConfig.TMDB_API_KEY

    companion object {
        private const val TAG = "NotificationWorker"
    }

    /**
     * Main work method called by [WorkManager] when scheduled.
     *
     * RETRY LOGIC:
     * - Network errors ([UnknownHostException], [SocketTimeoutException]): Try next cycle
     * - Other exceptions: Retry with exponential backoff
     *
     * PERFORMANCE: Tries up to 5 different favorites before using fallback
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @return
     * - [success] after attempting to send notification
     * - [retry] for retryable errors
     * - [success] for network errors (try next cycle)
     *
     * @since 5 Dec 2025
     */
    override suspend fun doWork(): Result =
        try {
            // Get current user
            val userId = auth.currentUser?.uid ?: return success()
            // Get user's favorite movies
            val favorites = getFavorites(userId)
            var notificationSent = false
            var attempts = 0
            val maxAttempts = 5 // Try up to 5 different favorites
            // Try to find a favorite with recommendations
            while (!notificationSent && attempts < maxAttempts && favorites.isNotEmpty()) {
                attempts++
                // Pick a random favorite movie
                val favoriteMovie = favorites.random()
                // Get recommendations for this favorite movie
                val recommendations = getMovieRecommendations(favoriteMovie.id)
                if (recommendations.isNotEmpty()) {
                    // Pick a random recommendation from the 3 top rated
                    val recommendedMovie =
                        recommendations.sortedBy { it.voteAverage }.takeLast(5).random()
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
                }
            }
            // If no favorites with recommendations found, try fallback strategies
            if (!notificationSent) sendFallbackNotification(favorites)
            success()
        } catch (e: Exception) {
            // Don't retry on network errors to avoid battery drain
            if (e is UnknownHostException || e is SocketTimeoutException) success() // Try again in next cycle
            else retry() // Retry for other errors
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
     * @since 5 Dec 2025
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
                        movieToRecommend =
                            genreMovies.sortedBy { it.voteAverage }.takeLast(5).random()
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
     * @since 5 Dec 2025
     */
    private suspend fun getFavorites(userId: String): List<Movie> =
        try {
            firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Movie::class.java) }
        } catch (e: Exception) {
            emptyList()
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
     * @since 5 Dec 2025
     */
    private suspend fun getMovieRecommendations(movieId: Int): List<Movie> =
        try {
            val response = RetrofitInstance.api.getMovieRecommendations(
                movieId = movieId,
                apiKey = apiKey,
                language = LanguageManager.currentLanguage.value,
            )
            response.results
        } catch (e: Exception) {
            emptyList()
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
     * @since 5 Dec 2025
     */
    private suspend fun getMoviesByGenre(genreId: Int): List<Movie> =
        try {
            val response = RetrofitInstance.api.discoverMoviesByGenre(
                apiKey = apiKey,
                page = 1,
                genreId = genreId,
                language = LanguageManager.currentLanguage.value,
            )
            response.results
        } catch (e: Exception) {
            emptyList()
        }

    /**
     * Fetches a popular movie from TMDB as last-resort fallback.
     *
     * API ENDPOINT: `/discover/movie`
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @return Random popular top rated [Movie] from a random [com.movito.movito.data.model.Genre] or `null` on error
     * @since 5 Dec 2025
     */
    private suspend fun getPopularMovie(): Movie? =
        try {
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
            null
        }

}