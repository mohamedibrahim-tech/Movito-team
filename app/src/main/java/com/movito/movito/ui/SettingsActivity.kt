package com.movito.movito.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.movito.movito.BuildConfig
import com.movito.movito.NotificationPreferences
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoButton
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.ui.common.SettingsCards
import com.movito.movito.viewmodel.AuthViewModel
import com.movito.movito.viewmodel.ThemeViewModel

class SettingsActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authState by authViewModel.authState.collectAsState()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            var notificationsState by remember {
                mutableStateOf(
                    NotificationManagerCompat.from(this).areNotificationsEnabled()
                )
            }

            LaunchedEffect(authState.user) {
                if (authState.user == null && authState.isInitialCheckDone) {
                    val intent = Intent(this@SettingsActivity, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            MovitoNavBar(selectedItem = "profile")
                        }
                    ) { paddingValues ->
                        SettingsScreen(
                            modifier = Modifier.padding(paddingValues),
                            onThemeToggle = { themeViewModel.toggleTheme(it) },
                            currentThemeIsDark = isDarkTheme,
                            onSignOut = { authViewModel.signOut() },
                            userEmail = authState.user?.email,
                            onChangePassword = { email ->
                                authViewModel.sendPasswordResetEmail(email)
                            },
                            notificationsEnabled = notificationsState,
                            onNotificationsStateUpdate = { notificationsState = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit,
    currentThemeIsDark: Boolean,
    onSignOut: () -> Unit,
    userEmail: String?,
    onChangePassword: (String) -> Unit,
    notificationsEnabled: Boolean = true,
    onNotificationsStateUpdate: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { NotificationPreferences.getInstance(context) }
    val githubUrl = "https://github.com/mohamedibrahim-tech/Movito-team/"

    var notifications by remember { mutableStateOf(notificationsEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.settings_title),
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Account Section
        SettingsCards {
            Text(
                text = stringResource(id = R.string.settings_account),
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {},
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_profile_info),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = userEmail ?: stringResource(id = R.string.settings_not_signed_in),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            MovitoButton(
                text = stringResource(id = R.string.settings_change_password),
                modifier = Modifier.fillMaxWidth(),
                roundedCornerSize = 12.dp,
                isLoading = false,
                onClick = {
                    userEmail?.let {
                        onChangePassword(it)
                        Toast.makeText(
                            context,
                            context.getString(R.string.settings_password_reset_sent, it),
                            Toast.LENGTH_LONG
                        ).show()
                    } ?: Toast.makeText(
                        context,
                        context.getString(R.string.settings_user_email_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            Spacer(Modifier.height(8.dp))

            MovitoButton(
                text = stringResource(id = R.string.settings_sign_out),
                modifier = Modifier.fillMaxWidth(),
                roundedCornerSize = 12.dp,
                isLoading = false,
                onClick = { onSignOut() }
            )
        }

        Spacer(Modifier.height(20.dp))

        // Appearance Section
        SettingsCards {
            Text(
                stringResource(id = R.string.settings_appearance),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(id = R.string.settings_theme_mode),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 20.sp
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = currentThemeIsDark,
                    onCheckedChange = onThemeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Notifications Section
        SettingsCards {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(id = R.string.settings_notifications),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = notifications,
                    onCheckedChange = { newValue ->
                        notifications = newValue

                        prefs.setNotificationsEnabled(newValue)

                        onNotificationsStateUpdate(newValue)

                        //  System Settings
                        val intent = Intent().apply {
                            when {
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                else -> {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            }
                        }

                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Could not open settings",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // About Section
        SettingsCards {
            Text(
                stringResource(id = R.string.settings_about),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(id = R.string.settings_version, BuildConfig.VERSION_NAME),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.settings_github_repository),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                    context.startActivity(intent)
                }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showSystemUi = true, name = "Dark Mode")
@Composable
fun SettingsPreviewDark() {
    var isDark by remember { mutableStateOf(true) }
    MovitoTheme(darkTheme = isDark) {
        SettingsScreen(
            onThemeToggle = { isDark = it },
            currentThemeIsDark = isDark,
            onSignOut = {},
            userEmail = "preview.user@gmail.com",
            onChangePassword = {}
        )
    }
}

@Preview(showSystemUi = true, name = "Light Mode")
@Composable
fun SettingsPreviewLight() {
    var isDark by remember { mutableStateOf(false) }
    MovitoTheme(darkTheme = isDark) {
        SettingsScreen(
            onThemeToggle = { isDark = it },
            currentThemeIsDark = isDark,
            onSignOut = {},
            userEmail = "preview.user@gmail.com",
            onChangePassword = {}
        )
    }
}