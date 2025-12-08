package com.movito.movito.notifications

import android.content.Context
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.movito.movito.R
import java.util.concurrent.TimeUnit


/**
 * MAIN PURPOSE: Manages [WorkManager] scheduling for notification delivery
 * with customizable intervals and constraints.
 *
 * KEY FEATURES:
 * - Schedules recurring notifications with custom intervals
 * - Provides immediate test notifications
 * - Cancels all scheduled notifications
 * - Validates interval constraints
 * - Persists schedule state
 *
 * INTERVAL HANDLING:
 * - Minimum: 15 minutes (enforced by [WorkManager] constraint)
 * - Maximum: Unlimited (practical limits apply)
 * - Units: Minutes, Hours, Days (converted to [WorkManager] constraints)
 *
 * WORK MANAGER:
 * - Uses [PeriodicWorkRequest] for recurring notifications
 * - Uses [OneTimeWorkRequest] for test notifications
 * - Unique work names prevent duplicates
 * - Network constraint: Requires [NetworkType.CONNECTED]
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 5 Dec 2025
 */
class NotificationScheduler {

    companion object {
        const val WORK_NAME = "movie_suggestion_notifications"
        private const val REPEATING_TEST_WORK_NAME = "repeating_test_notifications"

        /**
         * Schedules recurring notifications based on user preferences.
         *
         * SCHEDULING LOGIC:
         * 1. Checks if notifications are enabled
         * 2. Validates interval against minimum requirements
         * 3. Creates [PeriodicWorkRequest] with network constraint
         * 4. Enqueues unique work (cancels existing if any)
         * 5. Persists schedule time and state
         *
         * VALIDATION:
         * - Minutes: Minimum `15`
         * - Hours: Minimum `1`
         * - Days: Minimum `1`
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param context Application context for [WorkManager]
         * @since 5 Dec 2025
         */
        fun scheduleNotifications(context: Context) {
            try {
                val prefs = NotificationPreferences.getInstance(context)

                if (!prefs.isNotificationsEnabled()) {
                    cancelNotifications(context)
                    return
                }

                // Validate interval
                if (!prefs.isIntervalValid(prefs.getNotificationInterval())) {
                    val minValue = prefs.getMinIntervalForCurrentUnit()
                    val unit = prefs.getNotificationIntervalUnit()
                    Toast.makeText(
                        context,
                        context.resources.getString(
                            R.string.interval_must_be_at_least,
                            minValue,
                            unit
                        ),
                        Toast.LENGTH_LONG
                    ).apply {
                        setText(
                            context.resources.getString(
                                R.string.interval_must_be_at_least,
                                minValue,
                                unit
                            )
                        ) /* This ensures multi-line display*/
                    }
                        .show()
                    return
                }

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val intervalMinutes = prefs.getWorkManagerInterval()

                val notificationWork: PeriodicWorkRequest =
                    PeriodicWorkRequestBuilder<NotificationWorker>(
                        intervalMinutes,
                        TimeUnit.MINUTES
                    )
                        .setInitialDelay(intervalMinutes, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    notificationWork
                )

                // PERSIST: Save schedule time and state
                prefs.setLastScheduleTime(System.currentTimeMillis())
                prefs.setScheduled(true)

                Toast.makeText(
                    context,
                    context.getString(
                        R.string.notifications_scheduled_every,
                        prefs.getNotificationInterval(),
                        prefs.getLocalUnit(
                            context,
                            prefs.getNotificationIntervalUnit(),
                            prefs.getNotificationInterval()
                        )

                    ),
                    Toast.LENGTH_SHORT
                )
                    .apply {
                        setText(
                            context.getString(
                                R.string.notifications_scheduled_every,
                                prefs.getNotificationInterval(),
                                prefs.getLocalUnit(
                                    context,
                                    prefs.getNotificationIntervalUnit(),
                                    prefs.getNotificationInterval()
                                )
                            )
                        ) /* This ensures multi-line display*/
                    }
                    .show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_to_schedule_notifications), Toast.LENGTH_SHORT
                ).apply {
                    setText(context.getString(R.string.failed_to_schedule_notifications)) /* This ensures multi-line display*/
                }.show()
            }
        }

        /**
         * Schedules an immediate one-time notification for testing.
         *
         * USE CASE:
         * - Debugging notification appearance
         * - Testing deep linking
         * - Verifying permission flow
         *
         * BEHAVIOR:
         * - Runs immediately (0 second delay)
         * - Requires network connection
         * - Uses [OneTimeWorkRequest]
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param context Application context
         * @since 5 Dec 2025
         */
        fun scheduleImmediateNotification(context: Context) {
            try {
                val prefs = NotificationPreferences.getInstance(context)
                if (!prefs.isNotificationsEnabled()) {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.please_enable_notifications_first),
                        Toast.LENGTH_SHORT
                    ).apply {
                        setText(context.resources.getString(R.string.please_enable_notifications_first))
                    }.show()
                    return
                }

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val immediateWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(0, TimeUnit.SECONDS)
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "immediate_${System.currentTimeMillis()}",
                    ExistingWorkPolicy.REPLACE,
                    immediateWorkRequest
                )

                Toast.makeText(
                    context,
                    context.getString(R.string.sending_notification_now_mgs), Toast.LENGTH_SHORT
                ).apply {
                    setText(context.resources.getString(R.string.sending_notification_now_mgs))
                }.show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_to_send_notification_msg), Toast.LENGTH_SHORT
                ).apply {
                    setText(context.resources.getString(R.string.failed_to_send_notification_msg))
                }.show()
            }
        }

        /**
         * Schedules 5 test notifications 30 seconds apart.
         *
         * USE CASE:
         * - Testing notification frequency
         * - Verifying Worker execution
         * - Debugging recommendation logic
         *
         * BEHAVIOR:
         * - Creates 5 [OneTimeWorkRequest] instances
         * - Each delayed by 30 seconds more than previous
         * - Tagged for easy cancellation
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param context Application context
         * @since 5 Dec 2025
         */
        fun scheduleRepeatingTestNotifications(context: Context) {
            try {
                val prefs = NotificationPreferences.getInstance(context)
                if (!prefs.isNotificationsEnabled()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_enable_notifications_first),
                        Toast.LENGTH_SHORT
                    ).apply {
                        setText(context.getString(R.string.please_enable_notifications_first))
                    }.show()
                    return
                }

                // Schedule 5 notifications, 30 seconds apart
                for (i in 1..5) {
                    val testWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(i * 30L, TimeUnit.SECONDS)
                        .addTag(REPEATING_TEST_WORK_NAME)
                        .build()

                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "${REPEATING_TEST_WORK_NAME}_${System.currentTimeMillis()}_$i",
                        ExistingWorkPolicy.KEEP,
                        testWorkRequest
                    )
                }

                Toast.makeText(
                    context,
                    context.getString(R.string.notifications_30s_5x_btn_msg),
                    Toast.LENGTH_SHORT
                ).apply {
                    setText(context.getString(R.string.notifications_30s_5x_btn_msg))
                }.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Cancels all scheduled notifications.
         *
         * BEHAVIOR:
         * - Cancels main periodic work
         * - Cancels all test work by tag
         * - Cancels all work (cleanup)
         * - Updates persisted state
         *
         * USE CASES:
         * - User turns off notifications
         * - User signs out
         * - App uninstallation (handled by [WorkManager])
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param context Application context
         * @since 5 Dec 2025
         */
        fun cancelNotifications(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                WorkManager.getInstance(context).cancelAllWorkByTag(REPEATING_TEST_WORK_NAME)
                WorkManager.getInstance(context).cancelAllWork()

                // PERSIST: Clear schedule state
                val prefs = NotificationPreferences.getInstance(context)
                prefs.setScheduled(false)

                Toast.makeText(
                    context,
                    context.getString(R.string.cancelle_notifications_msg), Toast.LENGTH_SHORT
                ).apply {
                    setText(context.getString(R.string.cancelle_notifications_msg))
                }.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}