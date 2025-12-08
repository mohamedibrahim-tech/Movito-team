package com.movito.movito.notifications

import android.content.Context
import android.content.SharedPreferences
import com.movito.movito.R
import androidx.core.content.edit
import androidx.work.WorkManager
import com.movito.movito.ui.CategoriesScreen
import com.movito.movito.ui.SettingsScreen

/**
 * MAIN PURPOSE: Manages persistent storage for all notification-related
 * user preferences and scheduling state.
 *
 * STORAGE: Android [SharedPreferences] (singleton pattern)
 *
 * PREFERENCES MANAGED:
 * - Notification enabled/disabled state
 * - Custom interval (number + unit)
 * - Last schedule timestamp
 * - Permission reminder flags
 * - Battery notice flags
 * - Schedule persistence state
 *
 * VALIDATION:
 * - Minimum interval enforcement (15min/1hour/1day)
 * - Unit conversion (minutes/hours/days → milliseconds)
 * - WorkManager constraint compliance (minimum 15 minutes)
 *
 * LOCALIZATION:
 * - Proper plural string handling
 * - RTL support for Arabic intervals
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 5 Dec 2025
 */
class NotificationPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SHOULD_SHOW_PERMISSION_REMINDER = "should_show_permission_reminder"
        private const val KEY_BATTERY_RESTRICTION_NOTICE_SHOWN = "battery_restriction_notice_shown"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_INTERVAL = "notification_interval"
        private const val KEY_NOTIFICATION_INTERVAL_UNIT = "notification_interval_unit"
        private const val KEY_LAST_SCHEDULE_TIME = "last_schedule_time"
        private const val KEY_IS_SCHEDULED = "is_scheduled"
        private const val KEY_MAX_NOTIFICATION_COUNT = "max_notification_count"
        private const val KEY_CURRENT_NOTIFICATION_INDEX = "current_notification_index"

        // Default values
        const val DEFAULT_INTERVAL = 8
        const val DEFAULT_INTERVAL_UNIT = "hours"
        const val DEFAULT_MAX_NOTIFICATION_COUNT = 5
        const val MIN_MAX_NOTIFICATION_COUNT = 1
        const val MAX_MAX_NOTIFICATION_COUNT = 20

        // Minimum values for validation
        const val MIN_INTERVAL_MINUTES = 15
        const val MIN_INTERVAL_HOURS = 1
        const val MIN_INTERVAL_DAYS = 1

        @Volatile
        private var INSTANCE: NotificationPreferences? = null

        /**
         * Singleton accessor for [NotificationPreferences].
         *
         * PATTERN: Double-checked locking singleton
         *
         * SCOPE: Application-level (context.applicationContext)
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param context Application or Activity context
         * @return Singleton [NotificationPreferences] instance
         * @since 5 Dec 2025
         */
        fun getInstance(context: Context): NotificationPreferences =
            INSTANCE ?: synchronized(this) {
                // SECOND CHECK HERE is crucial!
                INSTANCE ?: NotificationPreferences(context.applicationContext).also {
                    INSTANCE = it
                }
            }
    }

    // ========== PERMISSION REMINDERS ==========

    /**
     * Controls whether to show permission reminder in [CategoriesScreen].
     *
     * LOGIC:
     * - Set to `true` when user dismisses without granting
     * - Set to `false` after showing reminder
     * - Prevents nagging users repeatedly
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 5 Dec 2025
     */
    fun setShouldShowPermissionReminder(shouldShow: Boolean) {
        prefs.edit { putBoolean(KEY_SHOULD_SHOW_PERMISSION_REMINDER, shouldShow) }
    }

    fun getShouldShowPermissionReminder(): Boolean {
        return prefs.getBoolean(KEY_SHOULD_SHOW_PERMISSION_REMINDER, false)
    }

    fun setBatteryRestrictionNoticeShown(shown: Boolean) {
        prefs.edit { putBoolean(KEY_BATTERY_RESTRICTION_NOTICE_SHOWN, shown) }
    }

    // ========== NOTIFICATION ENABLED STATE ==========

    /**
     * Stores whether notifications are enabled by user.
     *
     * DEFAULT: `true` (opt-out model)
     *
     * SYNC: Mirrors switch state in [SettingsScreen]
     *
     * PERMISSION: Independent of system permission state
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 5 Dec 2025
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun hasUserSetPreference(): Boolean {
        return prefs.contains(KEY_NOTIFICATIONS_ENABLED)
    }

    // ========== INTERVAL SETTINGS ==========

    fun setNotificationInterval(interval: Int) {
        prefs.edit { putInt(KEY_NOTIFICATION_INTERVAL, interval) }
    }

    fun getNotificationInterval(): Int {
        return prefs.getInt(KEY_NOTIFICATION_INTERVAL, DEFAULT_INTERVAL)
    }

    fun setNotificationIntervalUnit(unit: String) {
        prefs.edit { putString(KEY_NOTIFICATION_INTERVAL_UNIT, unit) }
    }

    fun getNotificationIntervalUnit(): String {
        return prefs.getString(KEY_NOTIFICATION_INTERVAL_UNIT, DEFAULT_INTERVAL_UNIT) ?: DEFAULT_INTERVAL_UNIT
    }

    fun setLastScheduleTime(timeMillis: Long) {
        prefs.edit { putLong(KEY_LAST_SCHEDULE_TIME, timeMillis) }
    }

    fun getLastScheduleTime(): Long {
        return prefs.getLong(KEY_LAST_SCHEDULE_TIME, 0L)
    }

    fun setScheduled(scheduled: Boolean) {
        prefs.edit { putBoolean(KEY_IS_SCHEDULED, scheduled) }
    }

    fun isScheduled(): Boolean {
        return prefs.getBoolean(KEY_IS_SCHEDULED, false)
    }

    /**
     * Gets the minimum allowed interval for current unit.
     *
     * VALIDATION:
     * - Minutes: `15` ([WorkManager] minimum)
     * - Hours: `1`
     * - Days: `1`
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @return Minimum [Int] value for current unit
     * @since 5 Dec 2025
     */
    fun getMinIntervalForCurrentUnit(): Int {
        return when (getNotificationIntervalUnit()) {
            "minutes" -> MIN_INTERVAL_MINUTES
            "hours" -> MIN_INTERVAL_HOURS
            "days" -> MIN_INTERVAL_DAYS
            else -> MIN_INTERVAL_HOURS
        }
    }

    fun isIntervalValid(interval: Int): Boolean {
        return interval >= getMinIntervalForCurrentUnit()
    }


    // ========== UNIT CONVERSION ==========

    /**
     * Converts stored interval to milliseconds for scheduling.
     *
     * @return Interval in milliseconds for [WorkManager]
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @since 5 Dec 2025
     */
    fun getIntervalInMillis(): Long {
        val interval = getNotificationInterval()
        return when (getNotificationIntervalUnit()) {
            "minutes" -> interval * 60 * 1000L
            "hours" -> interval * 60 * 60 * 1000L
            "days" -> interval * 24 * 60 * 60 * 1000L
            else -> DEFAULT_INTERVAL * 60 * 60 * 1000L
        }
    }

    /**
     * Converts interval to minutes for [WorkManager] constraints.
     *
     * CONSTRAINT: [WorkManager] requires minimum `15` minutes
     *
     * ADJUSTMENT: Coerces values below `15` minutes to `15`
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @return Interval in minutes (minimum `15`)
     * @since 5 Dec 2025
     */
    fun getWorkManagerInterval(): Long {
        val intervalMillis = getIntervalInMillis()
        val intervalMinutes = intervalMillis / (60 * 1000)
        return intervalMinutes.coerceAtLeast(15)
    }

    // ========== LOCALIZATION ==========

    /**
     * Gets localized plural [String] for interval unit.
     *
     * SUPPORTED LANGUAGES:
     * - ***English***
     * - ***Arabic***
     *
     * PLURAL RULES:
     * - Uses Android plurals resources
     * - Handles quantity-specific [String]s
     * - Respects locale formatting
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context Context for resource resolution
     * @param unit Internal unit (`"minutes"`, `"hours"`, `"days"`)
     * @param count Quantity for plural selection
     * @return Localized plural [String]
     * @since 5 Dec 2025
     */
    fun getLocalUnit(context: Context, unit: String, count: Int): String = when (unit) {
        "minutes" -> context.resources.getQuantityString(R.plurals.minutes, count)
        "hours" -> context.resources.getQuantityString(R.plurals.hours, count)
        "days" -> context.resources.getQuantityString(R.plurals.days, count)
        else -> context.resources.getQuantityString(R.plurals.hours, count)
    }

    /**
     * Maximum notification count
     */
    fun setMaxNotificationCount(count: Int) {
        val validatedCount = count.coerceIn(
            MIN_MAX_NOTIFICATION_COUNT,
            MAX_MAX_NOTIFICATION_COUNT
        )
        prefs.edit { putInt(KEY_MAX_NOTIFICATION_COUNT, validatedCount) }
    }

    fun getMaxNotificationCount(): Int {
        return prefs.getInt(KEY_MAX_NOTIFICATION_COUNT, DEFAULT_MAX_NOTIFICATION_COUNT)
    }

    /**
     * Current notification index (for rotating IDs)
     */
    fun setCurrentNotificationIndex(index: Int) {
        prefs.edit { putInt(KEY_CURRENT_NOTIFICATION_INDEX, index) }
    }

    fun getCurrentNotificationIndex(): Int {
        return prefs.getInt(KEY_CURRENT_NOTIFICATION_INDEX, 0)
    }

    /**
     *  Validate notification count
     */
    fun isNotificationCountValid(count: Int): Boolean {
        return count in MIN_MAX_NOTIFICATION_COUNT..MAX_MAX_NOTIFICATION_COUNT
    }

}
