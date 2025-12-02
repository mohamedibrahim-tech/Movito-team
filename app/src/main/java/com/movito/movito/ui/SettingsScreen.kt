package com.movito.movito.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.BuildConfig
import com.movito.movito.NotificationPreferences
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoButton
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.ui.common.SettingsCards
import com.movito.movito.viewmodel.LanguageViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeToggle: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    userEmail: String?,
    onChangePassword: (String) -> Unit,
    notificationsEnabled: Boolean = true,
    onNotificationsStateUpdate: (Boolean) -> Unit = {},
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val prefs = remember { NotificationPreferences.getInstance(context) }
    val githubUrl = "https://github.com/mohamedibrahim-tech/Movito-team/"
    val scope = rememberCoroutineScope()
    // Add language viewmodel
    val languageViewModel: LanguageViewModel = viewModel()
    val shouldRestartActivity by languageViewModel.shouldRestartActivity.collectAsState()
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    var notifications by remember { mutableStateOf(notificationsEnabled) }

    LaunchedEffect(shouldRestartActivity) {
        if (shouldRestartActivity)
            languageViewModel.onActivityRestarted()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            MovitoNavBar(selectedItem = "profile")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,

        ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Settings Title
            Text(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.settings_title),
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

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
                            text = userEmail
                                ?: stringResource(id = R.string.settings_not_signed_in),
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
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(
                                        R.string.settings_password_reset_sent,
                                        it
                                    ),
                                    withDismissAction = true
                                )
                            }
                        } ?: scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.settings_user_email_not_found),
                                withDismissAction = true
                            )
                        }
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

            // Language Section
            LanguageSection()

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
                        checked = isDarkTheme,
                        onCheckedChange = {
                            onThemeToggle(!isDarkTheme)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f
                            )
                        )
                    )
                }
            }

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
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f
                            )
                        )
                    )
                }
            }

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
        }


    }
}

@Composable
fun LanguageSection() {
    val context = LocalContext.current
    val languageViewModel: LanguageViewModel = viewModel()

    // Observe current language state
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    SettingsCards {
        Text(
            stringResource(R.string.language),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // English Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = if (currentLanguage == "en") 2.dp else 0.dp,
                        color = if (currentLanguage == "en") MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                MovitoButton(
                    text = stringResource(R.string.english),
                    modifier = Modifier.fillMaxWidth(),
                    roundedCornerSize = 12.dp,
                    enabled = currentLanguage != "en",
                    onClick = {
                        languageViewModel.setLanguage("en", context)
                        // The activity will automatically restart due to the key change
                    }
                )
            }

            // Arabic Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = if (currentLanguage == "ar") 2.dp else 0.dp,
                        color = if (currentLanguage == "ar") MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                MovitoButton(
                    text = stringResource(R.string.arabic),
                    modifier = Modifier.fillMaxWidth(),
                    roundedCornerSize = 12.dp,
                    enabled = currentLanguage != "ar",
                    onClick = {
                        languageViewModel.setLanguage("ar", context)
                        // The activity will automatically restart due to the key change
                    }
                )
            }
        }
    }
}

@Preview(showSystemUi = true, name = "Dark Mode")
@Composable
fun SettingsPreviewDark() {
    var isDark by remember { mutableStateOf(true) }
    MovitoTheme(darkTheme = isDark) {
        SettingsScreen(
            onThemeToggle = { isDark = it },
            onSignOut = {},
            userEmail = "preview.user@gmail.com",
            onChangePassword = {},
            notificationsEnabled = true,
            onNotificationsStateUpdate = {},
            isDark
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
            onSignOut = {},
            userEmail = "preview.user@gmail.com",
            onChangePassword = {},
            notificationsEnabled = true,
            onNotificationsStateUpdate = {},
            isDark
        )
    }
}