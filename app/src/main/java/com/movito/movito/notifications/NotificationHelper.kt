package com.movito.movito.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.ui.CategoriesActivity
import com.movito.movito.ui.DetailsActivity
import com.movito.movito.ui.SettingsScreen
import com.movito.movito.ui.SignInActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS

/**
 * MAIN PURPOSE: Centralized utility for managing all notification-related operations
 * including channel creation, notification sending, and settings navigation.
 *
 * KEY FEATURES:
 * - Creates and manages notification channels (Android O+)
 * - Sends welcome notifications to new users
 * - Sends personalized movie suggestion notifications
 * - Handles navigation to system notification settings
 * - Builds proper task stacks for notification navigation
 *
 * DEPENDENCIES: Android [NotificationManager], [TaskStackBuilder]
 * INTEGRATION: Used by [SignInActivity], [SettingsScreen], [NotificationWorker]
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 5 Dec 2025
 *
 * @see android.app.NotificationManager
 * @see android.app.TaskStackBuilder
 * @see com.movito.movito.ui.SignInActivity
 * @see com.movito.movito.ui.SettingsScreen
 * @see NotificationWorker
 */
object NotificationHelper {

    private const val CHANNEL_ID = "movie_suggestions"
    private const val BASE_NOTIFICATION_ID = 1000 // Start from 1000 to avoid conflicts
    private const val DEFAULT_MAX_NOTIFICATIONS = 5

    /**
     * Creates all required notification channels for the app.
     *
     * CHANNELS CREATED:
     * 1. "movie_suggestions" - For personalized movie recommendations
     * 2. "welcome_channel" - For welcome messages to new users
     *
     * ⚠️ REQUIRED: Must be called early in app lifecycle (i.e., [SignInActivity.onCreate], [CategoriesActivity.onCreate])
     *
     * **Author**: Movito Development Team Members [Ahmed Essam](https://github.com/ahmed-essam-dev/), [Alyaa Osama](https://github.com/AlyaaOsamaZaki)
     *
     * @param context Application context for accessing [NotificationManager]
     * @see com.movito.movito.ui.SignInActivity.onCreate
     * @see com.movito.movito.ui.CategoriesActivity.onCreate
     * @since 5 Dec 2025
     */
    fun createAllNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        // 1. Movie Suggestions Channel
        val movieChannel = NotificationChannel(
            "movie_suggestions",
            "Movie Suggestions",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Get personalized movie suggestions based on your favorites"
            enableVibration(true)
            setShowBadge(true)
        }

        // 2. Welcome Channel
        val welcomeChannel = NotificationChannel(
            "welcome_channel",
            "Welcome Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications to welcome users"
            enableVibration(true)
            setShowBadge(true)
        }

        // Create both channels
        notificationManager.createNotificationChannel(movieChannel)
        notificationManager.createNotificationChannel(welcomeChannel)
    }

    /**
     * Gets the next notification ID in a rotating manner.
     * Uses a circular buffer approach to avoid overwriting recent notifications.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 5 Dec 2025
     * @param context Context for accessing shared preferences
     * @return Notification ID to use for the next notification
     */
    fun getNextNotificationId(context: Context): Int {
        val prefs = NotificationPreferences.getInstance(context)
        val maxNotifications = prefs.getMaxNotificationCount()
        var currentIndex = prefs.getCurrentNotificationIndex()

        // Calculate next ID (base + currentIndex)
        val nextId = BASE_NOTIFICATION_ID + currentIndex

        // Update index for next notification (circular buffer)
        currentIndex = (currentIndex + 1) % maxNotifications
        prefs.setCurrentNotificationIndex(currentIndex)

        return nextId
    }

    /**
     * Sends a welcome notification to newly signed-in users.
     *
     * BEHAVIOR:
     * - Only sends if notifications are enabled in preferences
     * - Uses the "welcome_channel" channel
     * - Automatically cancels when tapped
     * - Checks permission for Android 13+
     *
     * **Author**: Movito Development Team Member [Alyaa Osama](https://github.com/AlyaaOsamaZaki)
     *
     * @param context Application context for notification display
     * @since 27 Nov 2025
     */
    fun sendWelcomeNotification(context: Context) {
        val prefs = NotificationPreferences.getInstance(context)
        if (!prefs.isNotificationsEnabled()) return

        val builder = NotificationCompat.Builder(context, "welcome_channel")
            .setSmallIcon(R.drawable.movito_logo)
            .setContentTitle(context.getString(R.string.notification_welcome_title))
            .setContentText(context.getString(R.string.notification_welcome_msg))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled())
                notificationManager.notify(1, builder.build())
        } else
            notificationManager.notify(1, builder.build())
    }

    /**
     * Sends a personalized movie suggestion notification based on user's favorites.
     *
     * RECOMMENDATION LOGIC:
     * - Called by NotificationWorker when scheduled
     * - Creates deep link to movie details with proper back navigation
     * - Includes custom message explaining the recommendation
     *
     * NAVIGATION:
     * - Tapping notification opens [DetailsActivity] for recommended movie
     * - Back button leads to [CategoriesActivity] (proper task stack)
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context Application context
     * @param recommendedMovie Movie object to suggest
     * @param customMessage Personalized explanation for the recommendation
     * @since 5 Dec 2025
     */
    fun sendMovieSuggestionNotification(
        context: Context,
        recommendedMovie: Movie,
        customMessage: String,
    ) {
        val prefs = NotificationPreferences.getInstance(context)

        // Check if notifications are enabled
        if (!prefs.isNotificationsEnabled()) return

        // Get next notification ID (rotating system)
        val notificationId = getNextNotificationId(context)

        // Create proper task stack for navigation
        val detailsIntent = Intent(context, DetailsActivity::class.java).apply {
            putExtra("movie", recommendedMovie)
        }

        // Build the task stack to ensure proper back navigation
        val stackBuilder = TaskStackBuilder.create(context).apply {
            // Add CategoriesActivity as the parent
            addNextIntent(Intent(context, CategoriesActivity::class.java))
            // Add DetailsActivity on top
            addNextIntent(detailsIntent)
        }

        val pendingIntent = stackBuilder.getPendingIntent(
            notificationId, // Use notification ID as request code
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create views for the custom notification
        val collapsedView = RemoteViews(
            context.packageName,
            R.layout.notification_movie_collapsed
        ).apply{
                setTextViewText(R.id.rating, "%.1f".format(recommendedMovie.voteAverage))
            }

        val expandedView = RemoteViews(
            context.packageName,
            R.layout.notification_movie_expanded
        ).apply{
            setTextViewText(R.id.recommendation_msg, customMessage)
            setTextViewText(R.id.rating, "%.2f".format(recommendedMovie.voteAverage))
        }

        // Build notification with custom message
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.movito_logo)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // timestamp
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .build()

        // Show notification
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        // Then load images asynchronously
        loadImagesAndUpdateNotification(context, customMessage, recommendedMovie, pendingIntent, notificationId = notificationId)
    }

    /**
     * Asynchronously loads movie poster images and updates an existing notification with the loaded images.
     *
     * This function performs the following operations:
     * 1. Loads two sizes of movie poster images (small for collapsed view, large for expanded view)
     * 2. Updates the notification with loaded images while preserving the original notification structure
     * 3. Handles errors gracefully without disrupting the existing notification
     *
     * ***Example of usage***
     *
     * ```
     * // Called after initial notification is shown
     * loadImagesAndUpdateNotification(
     *     context = context,
     *     customMessage = "We think you'll love this!",
     *     movie = recommendedMovie,
     *     pendingIntent = pendingIntent
     * )
     * ```
     *
     * ***Notes:***
     * > Images are loaded from TMDB (The Movie Database) API
     * > The function runs asynchronously using CoroutineScope on Dispatchers.IO
     * > If image loading fails, the notification remains with default/previous state
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context The application context used for resources and services
     * @param customMessage A custom recommendation message to display in the expanded notification
     * @param movie The Movie object containing movie details and poster path
     * @param pendingIntent The PendingIntent to handle notification clicks, ensuring navigation works
     * @param notificationId the id of the notification to load its image and update it
     *
     * @see ImageLoader For asynchronous image loading with Coil
     * @see RemoteViews For custom notification layout updates
     * @see NotificationCompat.Builder For building notifications
     *
     * @since 3 Dec 2025
     */
    private fun loadImagesAndUpdateNotification(
        context: Context,
        customMessage: String,
        movie: Movie,
        pendingIntent: PendingIntent,
        notificationId: Int
    ) {
        // Initialize Coil ImageLoader for efficient image loading and caching
        val imageLoader = ImageLoader.Builder(context).crossfade(true).build()

        // Launch coroutine in background thread for network operations
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Construct URLs for different poster sizes from TMDB
                val smallPosterUrl = "https://image.tmdb.org/t/p/w154${movie.posterPath}"
                val largePosterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"

                // Load small poster for collapsed notification view (40x60 dp)
                // w154: TMDB small poster size (154px wide, ~2:3 ratio)
                val smallRequest = ImageRequest.Builder(context)
                    .data(smallPosterUrl)
                    .size(2 * 40, 3 * 40)  // 80x120 pixels (converted from dp: 40dp × 2.5 density factor)
                    .build()
                val smallResult = imageLoader.execute(smallRequest)
                val smallBitmap = if (smallResult is SuccessResult) {
                    smallResult.drawable.toBitmap()  // Convert to Bitmap for RemoteViews
                } else null  // Null-safe fallback if loading fails

                // Load large poster for expanded notification view (150x225 dp)
                // w500: TMDB medium poster size (500px wide, ~2:3 ratio)
                val largeRequest = ImageRequest.Builder(context)
                    .data(largePosterUrl)
                    .size(2 * 150, 3 * 150)  // 300x450 pixels (converted from dp)
                    .build()
                val largeResult = imageLoader.execute(largeRequest)
                val largeBitmap = if (largeResult is SuccessResult) {
                    largeResult.drawable.toBitmap()
                } else null

                // Switch to main thread to update UI components
                withContext(Dispatchers.Main) {
                    // Get NotificationManager to update existing notification
                    val notificationManager =
                        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                    // Rebuild notification with original properties to maintain consistency
                    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.movito_logo)  // App logo as small icon
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())  // System-styled custom layout
                        .setContentIntent(pendingIntent)  // Preserve original click action
                        .setAutoCancel(true)  // Dismiss when tapped
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // Standard priority
                        // Add timestamp
                        .setWhen(System.currentTimeMillis())
                        .setShowWhen(true)

                    // Create updated collapsed view with loaded image
                    // R.layout.notification_movie_collapsed should contain:
                    // - ImageView with id @+id/poster
                    // - TextView with id @+id/rating
                    val updatedCollapsedView = RemoteViews(
                        context.packageName,
                        R.layout.notification_movie_collapsed
                    ).apply {
                        // Format rating to one decimal place (e.g., 7.5)
                        setTextViewText(R.id.rating, "%.1f".format(movie.voteAverage))
                        // Set loaded image or keep existing if null
                        smallBitmap?.let { setImageViewBitmap(R.id.poster, it) }
                    }

                    // Create updated expanded view with large image and custom message
                    // R.layout.notification_movie_expanded should contain:
                    // - ImageView with id @+id/poster_large
                    // - TextView with id @+id/rating
                    // - TextView with id @+id/recommendation_msg
                    val updatedExpandedView = RemoteViews(
                        context.packageName,
                        R.layout.notification_movie_expanded
                    ).apply {
                        // Display personalized recommendation message
                        setTextViewText(R.id.recommendation_msg, customMessage)
                        // Format rating to two decimal places (e.g., 7.53)
                        setTextViewText(R.id.rating, "%.2f".format(movie.voteAverage))
                        // Set loaded large image or keep existing if null
                        largeBitmap?.let { setImageViewBitmap(R.id.poster_large, it) }
                    }

                    // Apply updated views to notification
                    notification
                        .setCustomContentView(updatedCollapsedView)    // View when notification is collapsed
                        .setCustomBigContentView(updatedExpandedView)  // View when notification is expanded

                    // Update existing notification with same ID (replaces previous)
                    // NOTIFICATION_ID should be unique per movie or use movie.id for uniqueness
                    notificationManager.notify(notificationId, notification.build())
                }
            } catch (e: Exception) {
                // Graceful error handling: If image loading fails, keep original notification
                // No notification update occurs, maintaining existing functionality
                // Consider logging for debugging: Log.e("Notification", "Image load failed", e)
            }
        }
    }

    /**
     * Opens system notification settings for the app.
     *
     * BEHAVIOR:
     * - Directly opens app-specific notification settings
     * - Temporarily sets notifications to false until user confirms
     * - Catches exceptions and shows error snackbar (caller responsibility)
     *
     * ANDROID VERSIONS:
     * - Android O+: Uses [ACTION_APP_NOTIFICATION_SETTINGS]
     * - Below O: Uses [ACTION_APPLICATION_DETAILS_SETTINGS]
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context Application context
     * @param prefs NotificationPreferences instance
     * @param onStateUpdate Callback to update UI switch state
     * @since 5 Dec 2025
     */
    fun openNotificationSettings(
        context: Context,
        prefs: NotificationPreferences,
        onStateUpdate: (Boolean) -> Unit
    ) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        try {
            context.startActivity(intent)
            // Keep switch off until permission is actually granted
            prefs.setNotificationsEnabled(false)
            onStateUpdate(false)
        } catch (e: Exception) {
            // Show error snackbar would be handled by the caller
            prefs.setNotificationsEnabled(false)
            onStateUpdate(false)
        }
    }

    /**
     * Cancels all movie suggestion notifications (up to the max limit)
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 5 Dec 2025
     */
    fun cancelAllMovieSuggestions(context: Context) {
        val prefs = NotificationPreferences.getInstance(context)
        val maxNotifications = prefs.getMaxNotificationCount()
        val notificationManager = NotificationManagerCompat.from(context)

        for (i in 0 until maxNotifications) {
            val notificationId = BASE_NOTIFICATION_ID + i
            notificationManager.cancel(notificationId)
        }

        // Reset index
        prefs.setCurrentNotificationIndex(0)
    }
}


