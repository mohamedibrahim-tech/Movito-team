package com.movito.movito.notifications

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * MAIN PURPOSE: Manages battery optimization permissions for reliable
 * notification delivery on Android devices.
 *
 * PROBLEM ADDRESSED:
 * Android's battery optimization can delay or prevent background work
 * including [androidx.work.WorkManager] notifications, especially on:
 * - Huawei, Xiaomi, Oppo, Vivo devices
 * - Samsung with aggressive power saving
 * - Devices with custom Android skins
 *
 * KEY FEATURES:
 * - Checks if app has unrestricted battery access
 * - Opens battery optimization settings for the app
 * - Provides device-specific fallback intents
 * - Handles Samsung-specific settings screens
 *
 * IMPORTANT NOTE:
 * > This is a "nice to have" permission, not required.
 * > Only affects timing reliability, not functionality.
 * > Some manufacturers may not honor this setting.
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 4 Dec 2025
 */
object BatteryPermissionHelper {

    /**
     * Checks if the app has unrestricted battery access.
     *
     * CHECK LOGIC:
     * - Uses [PowerManager.isIgnoringBatteryOptimizations] function
     * - Returns `true` if app is whitelisted from optimizations
     * - Returns `false` if optimizations may affect the app
     *
     * STATISTICS:
     * - Default: `false` (app is optimized)
     * - After user grant: true
     * - May reset after OS updates or factory resets
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context Application context
     * @return [Boolean] indicating unrestricted battery access
     * @since 4 Dec 2025
     */
    fun hasUnrestrictedBatteryAccess(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Opens battery optimization settings for the app.
     *
     * INTENT STRATEGY (in order):
     * 1. [Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS] (standard)
     * 2. Samsung-specific intent (`com.samsung.android.sm.ACTION_POWER_MANAGER`)
     * 3. [Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS] (list view)
     * 4. [Settings.ACTION_APPLICATION_DETAILS_SETTINGS] (app info)
     * 5. [Settings.ACTION_SETTINGS] (general settings fallback)
     *
     * MANUFACTURER HANDLING:
     * - Samsung: Uses custom intent for better UX
     * - Others: Standard Android intents
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param context Application context
     * @since 4 Dec 2025
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            // Direct intent to battery optimization settings for this app
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fall back to list of battery optimization list

            // Samsung devices has more convenient intent for the user
            val isSamsungDevice =
                Build.MANUFACTURER.equals("samsung", ignoreCase = true) ||
                        Build.BRAND.equals("samsung", ignoreCase = true)
            if(isSamsungDevice){
                try {
                    val intent = Intent().apply {
                        action = "com.samsung.android.sm.ACTION_POWER_MANAGER"
                    }
                    context.startActivity(intent)
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                // Intent to battery optimization list settings for this app
                val intent = Intent().apply {
                    action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)

            } catch(e2: Exception)
            {
                // Fallback to app info settings
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e3: Exception) {
                    // Last resort - open general settings
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }

            }
        }
    }
}