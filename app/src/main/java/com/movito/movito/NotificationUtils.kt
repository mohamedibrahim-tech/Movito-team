package com.movito.movito


import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

fun sendWelcomeNotification(context: Context) {
    val prefs = NotificationPreferences.getInstance(context)
    if (!prefs.isNotificationsEnabled()) return

    val builder = NotificationCompat.Builder(context, "welcome_channel")
        .setSmallIcon(R.drawable.movito_logo)
        .setContentTitle("Welcome to Movito!")
        .setContentText("Glad to have you in Movito app 🎬")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    val notificationManager = NotificationManagerCompat.from(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(1, builder.build())
        }
    } else {
        notificationManager.notify(1, builder.build())
    }
}

class NotificationPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        @Volatile
        private var INSTANCE: NotificationPreferences? = null

        fun getInstance(context: Context): NotificationPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationPreferences(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
}