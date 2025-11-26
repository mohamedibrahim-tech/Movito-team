package com.movito.movito

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.movito.movito.ui.HomeScreen
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()

        //  Notification Channel
        createNotificationChannel()

        enableEdgeToEdge()

        setContent {
            MovitoTheme {
                NotificationPermissionHandler(
                    onPermissionResult = { granted ->
                        if (granted) {
                            val prefs = NotificationPreferences.getInstance(this)
                            prefs.setNotificationsEnabled(true)

                            sendWelcomeNotification(this)
                        } else {
                            val prefs = NotificationPreferences.getInstance(this)
                            prefs.setNotificationsEnabled(false)
                        }
                    }
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.isLoading
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Welcome Channel"
            val descriptionText = "Notifications to welcome users"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel("welcome_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionHandler(
    onPermissionResult: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = NotificationPreferences.getInstance(context)

    var permissionRequested by remember { mutableStateOf(false) }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        LaunchedEffect(Unit) {
            if (!permissionRequested) {
                if (prefs.isNotificationsEnabled()) {
                    sendWelcomeNotification(context)
                }
                onPermissionResult(true)
                permissionRequested = true
            }
        }
        content()
        return
    }

    val notificationPermission = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    ) { isGranted ->
        onPermissionResult(isGranted)
        if (!isGranted) {
            Toast.makeText(
                context,
                "You will not receive notifications",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        when {
            notificationPermission.status.isGranted -> {
                if (!permissionRequested) {
                    if (prefs.isNotificationsEnabled()) {
                        sendWelcomeNotification(context)
                    }
                    onPermissionResult(true)
                    permissionRequested = true
                }
            }
            notificationPermission.status.shouldShowRationale -> {
                notificationPermission.launchPermissionRequest()
            }
            else -> {
                notificationPermission.launchPermissionRequest()
            }
        }
    }

    content()
}


fun sendWelcomeNotification(context: Context) {
    val prefs = NotificationPreferences.getInstance(context)
    if (!prefs.isNotificationsEnabled()) {
        return
    }

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


fun sendNotification(
    context: Context,
    title: String,
    message: String,
    notificationId: Int = System.currentTimeMillis().toInt()
) {
    val prefs = NotificationPreferences.getInstance(context)

    if (!prefs.isNotificationsEnabled()) {
        return
    }

    val builder = NotificationCompat.Builder(context, "welcome_channel")
        .setSmallIcon(R.drawable.movito_logo)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    val notificationManager = NotificationManagerCompat.from(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, builder.build())
        }
    } else {
        notificationManager.notify(notificationId, builder.build())
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