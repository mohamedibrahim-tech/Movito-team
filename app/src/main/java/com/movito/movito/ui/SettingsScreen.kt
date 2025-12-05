package com.movito.movito.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.movito.movito.BuildConfig
import com.movito.movito.R
import com.movito.movito.notifications.BatteryPermissionHelper
import com.movito.movito.notifications.NotificationHelper
import com.movito.movito.notifications.NotificationPreferences
import com.movito.movito.notifications.NotificationScheduler
import com.movito.movito.notifications.NotificationWorker
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoButton
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.ui.common.SettingsCards
import com.movito.movito.viewmodel.LanguageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * COMPREHENSIVE APP SETTINGS INTERFACE
 *
 * PURPOSE: Provides users with complete control over app configuration,
 * preferences, and personalized features. This composable screen organizes
 * settings into logical sections with intuitive controls and real-time feedback.
 *
 * ARCHITECTURE:
 * - Material 3 Scaffold with consistent navigation patterns
 * - Section-based organization for clarity and discoverability
 * - Real-time preference synchronization with ViewModels
 * - Comprehensive notification management with validation
 *
 * SECTION ORGANIZATION:
 *
 * 1. ACCOUNT SECTION:
 *    - Displays current user email and authentication status
 *    - Password reset functionality with email confirmation
 *    - Secure sign-out with notification cleanup
 *
 * 2. LANGUAGE SECTION:
 *    - Bilingual support (English/Arabic) with visual selection
 *    - Activity restart on language change for complete localization
 *    - Visual feedback for current selection
 *
 * 3. APPEARANCE SECTION:
 *    - Dark/Light theme toggle with Material 3 theming
 *    - System theme synchronization
 *    - Immediate visual feedback on theme changes
 *
 * 4. NOTIFICATIONS SECTION (COMPREHENSIVE):
 *    - Master toggle with smart permission handling
 *    - Customizable frequency controls (minutes/hours/days)
 *    - Notification history limit setting (1-20)
 *    - Real-time validation and feedback
 *    - Battery optimization guidance
 *    - Testing tools for debugging
 *
 * 5. ABOUT SECTION:
 *    - App version information
 *    - GitHub repository link
 *    - Development team attribution
 *
 * NOTIFICATION SYSTEM FEATURES:
 *
 * MASTER SWITCH INTELLIGENCE:
 * - Android 13+: Verifies permission before enabling
 * - Shows contextual permission requests when needed
 * - Opens system settings with one-tap access
 * - Maintains state across permission dialogs
 *
 * FREQUENCY CUSTOMIZATION:
 * - Number input with digit validation (1-99)
 * - Unit selection dropdown (minutes/hours/days)
 * - Minimum interval enforcement (15min/1hr/1day)
 * - Real-time display of current vs pending settings
 *
 * NOTIFICATION LIMIT:
 * - Maximum number of notifications to keep in history (1-20)
 * - Oldest notifications are removed when limit is reached
 * - Helps manage notification clutter
 *
 * VALIDATION AND FEEDBACK:
 * - Visual indicators for invalid inputs
 * - Color-coded status messages
 * - Snackbar confirmations for actions
 * - Progressive disclosure of complex options
 *
 * BATTERY OPTIMIZATION GUIDANCE:
 * - Detects device battery restrictions
 * - Provides educational explanations
 * - Offers one-tap access to battery settings
 * - Device-specific handling (Google vs manufacturer devices)
 *
 * TESTING TOOLS:
 * - Immediate notification testing
 * - Repeated notification scheduling for debugging
 * - Complete notification cancellation
 * - Visual feedback for all test operations
 *
 * USER EXPERIENCE PRINCIPLES:
 * 1. Progressive Disclosure: Complex options revealed gradually
 * 2. Immediate Feedback: All actions provide visual confirmation
 * 3. Error Prevention: Validation prevents invalid configurations
 * 4. Educational Content: Explanations for technical settings
 * 5. Consistency: Material Design 3 patterns throughout
 *
 * TECHNICAL INTEGRATION:
 * - WorkManager: Background notification scheduling
 * - SharedPreferences: User preference persistence
 * - LiveData: Real-time status observation
 * - Permission APIs: Android 13+ compatibility
 *
 * ACCESSIBILITY FEATURES:
 * - Proper content descriptions for all controls
 * - Sufficient color contrast ratios
 * - Logical focus navigation order
 * - Screen reader compatibility
 */
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

    val languageViewModel: LanguageViewModel = viewModel()
    val shouldRestartActivity by languageViewModel.shouldRestartActivity.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var notifications by remember { mutableStateOf(notificationsEnabled) }

    /*
     * REAL-TIME NOTIFICATION STATUS CHECKING
     *
     * Observes WorkManager schedule status and updates UI accordingly.
     * Ensures switch state accurately reflects actual notification scheduling.
     */
    LaunchedEffect(Unit) {
        checkNotificationStatus(context) { isScheduled ->
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
            notifications = isScheduled && hasPermission
        }
    }

    /*
     * LANGUAGE CHANGE HANDLING
     *
     * Restarts activity when language is changed to apply new locale
     * to all resources and layouts.
     */
    LaunchedEffect(shouldRestartActivity) {
        if (shouldRestartActivity) languageViewModel.onActivityRestarted()
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
                    Column(modifier = Modifier.weight(1f)) {
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
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    message = context.getString(
                                        R.string.settings_password_reset_sent,
                                        it
                                    ),
                                    withDismissAction = true
                                )
                            }
                        } ?: scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
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

            // LANGUAGE SECTION
            LanguageSection(languageViewModel = languageViewModel)

            // APPEARANCE SECTION
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

            // NOTIFICATIONS SECTION
            NotificationSection(
                notifications = notifications,
                onNotificationsChange = { newValue ->
                    handleNotificationToggle(
                        newValue = newValue,
                        context = context,
                        prefs = prefs,
                        snackbarHostState = snackbarHostState,
                        scope = scope,
                        onNotificationsStateUpdate = onNotificationsStateUpdate,
                        updateNotificationState = { notifications = it }
                    )
                },
                prefs = prefs,
                snackbarHostState = snackbarHostState,
                scope = scope,
                context = context,
                onNotificationsStateUpdate = onNotificationsStateUpdate
            )

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

/**
 * LANGUAGE SECTION COMPOSABLE
 *
 * PURPOSE: Provides language selection between English and Arabic
 * with visual feedback for the current selection.
 *
 * FEATURES:
 * - Bilingual support with proper localization
 * - Activity restart on language change
 * - Visual border indication for selected language
 * - Proper RTL/LTR layout switching
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 1 Dec 2025
 */
@Composable
fun LanguageSection(
    languageViewModel: LanguageViewModel
) {
    val context = LocalContext.current
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
            @Composable
            fun EnglishButton() =
                LanguageButton(
                    displayText = stringResource(R.string.english),
                    isSelected = currentLanguage == "en",
                    onClick = { languageViewModel.setLanguage("en", context) },
                    modifier = Modifier.weight(1f)
                )

            // Arabic Button
            @Composable
            fun ArabicButton() =
                LanguageButton(
                    displayText = stringResource(R.string.arabic),
                    isSelected = currentLanguage == "ar",
                    onClick = { languageViewModel.setLanguage("ar", context) },
                    modifier = Modifier.weight(1f)
                )

            // To keep the buttons at the same place when language change
            if (languageViewModel.currentLanguage.value == "ar") {
                EnglishButton()
                ArabicButton()
            } else {
                ArabicButton()
                EnglishButton()
            }
        }
    }
}

/**
 * REUSABLE LANGUAGE BUTTON COMPOSABLE
 *
 * PURPOSE: Individual language button with selection visual feedback
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 4 Dec 2025
 */
@Composable
private fun LanguageButton(
    displayText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) =
    Box(
        modifier = modifier.border(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else Color.Transparent,
            shape = RoundedCornerShape(12.dp)
        )
    ) {
        MovitoButton(
            text = displayText,
            modifier = Modifier.fillMaxWidth(),
            roundedCornerSize = 12.dp,
            enabled = !isSelected,
            onClick = onClick
        )
    }

/**
 * NOTIFICATION SECTION COMPOSABLE
 *
 * PURPOSE: Comprehensive notification management interface with
 * master control, frequency customization, and testing tools.
 *
 * ARCHITECTURE: Organized into sub-components:
 * 1. NotificationHeader: Master switch with explanation
 * 2. NotificationSettings: Frequency customization (when enabled)
 * 3. TestNotificationSection: Debugging tools (when enabled)
 */
@Composable
fun NotificationSection(
    notifications: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    prefs: NotificationPreferences,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    context: Context,
    onNotificationsStateUpdate: (Boolean) -> Unit
) =
    SettingsCards {
        // Header with master switch
        NotificationHeader(
            notifications = notifications,
            onNotificationsChange = onNotificationsChange,
            context = context,
            prefs = prefs,
            snackbarHostState = snackbarHostState,
            scope = scope,
            onNotificationsStateUpdate = onNotificationsStateUpdate,
        )

        Spacer(Modifier.height(16.dp))

        // Show settings only when notifications are enabled
        if (notifications) {
            NotificationSettings(
                prefs = prefs,
                snackbarHostState = snackbarHostState,
                scope = scope,
                context = context
            )

            Spacer(Modifier.height(20.dp))

            TestNotificationSection(
                snackbarHostState = snackbarHostState,
                scope = scope,
                context = context,
                prefs = prefs,
                onNotificationsStateUpdate = onNotificationsStateUpdate,
                updateNotificationState = { onNotificationsChange(false) }
            )
        } else Text( // Message when notifications are disabled
            stringResource(R.string.notification_not_enabled_message),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

/**
 * NOTIFICATION HEADER COMPOSABLE
 *
 * PURPOSE: Master notification control with intelligent permission handling
 * and contextual explanations.
 */
@Composable
fun NotificationHeader(
    notifications: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    context: Context,
    prefs: NotificationPreferences,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onNotificationsStateUpdate: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(id = R.string.settings_notifications),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.notification_explaination),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Justify,
                fontSize = 16.sp
            )
        }

        NotificationMasterSwitch(
            notifications = notifications,
            onNotificationsChange = onNotificationsChange,
            context = context,
            prefs = prefs,
            snackbarHostState = snackbarHostState,
            scope = scope,
            onNotificationsStateUpdate = onNotificationsStateUpdate
        )
    }
}

/**
 * NOTIFICATION MASTER SWITCH COMPOSABLE
 *
 * PURPOSE: Intelligent switch that handles Android 13+ permissions
 * and provides contextual user guidance.
 */
@Composable
fun NotificationMasterSwitch(
    notifications: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    context: Context,
    prefs: NotificationPreferences,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onNotificationsStateUpdate: (Boolean) -> Unit
) {
    Switch(
        checked = notifications,
        onCheckedChange = { newValue ->
            handleNotificationToggle(
                newValue = newValue,
                context = context,
                prefs = prefs,
                snackbarHostState = snackbarHostState,
                scope = scope,
                onNotificationsStateUpdate = onNotificationsStateUpdate,
                updateNotificationState = onNotificationsChange
            )
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

/**
 * NOTIFICATION SETTINGS COMPOSABLE
 *
 * PURPOSE: Frequency customization interface with real-time validation
 * and battery optimization guidance.
 */
@Composable
fun NotificationSettings(
    prefs: NotificationPreferences,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    context: Context
) {
    // State for interval settings
    var intervalText by remember { mutableStateOf(prefs.getNotificationInterval().toString()) }
    var currentUnit by remember { mutableStateOf(prefs.getNotificationIntervalUnit()) }

    //  State for notification limit
    var limitText by remember { mutableStateOf(prefs.getMaxNotificationCount().toString()) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val intervalValue = intervalText.toIntOrNull() ?: 0
    val limitValue = limitText.toIntOrNull() ?: 0

    val minuteDisplay = pluralStringResource(R.plurals.minutes, 0)
    val hourDisplay = pluralStringResource(R.plurals.hours, 0)
    val dayDisplay = pluralStringResource(R.plurals.days, 0)
    val listOfUnitsToDisplay = listOf(minuteDisplay, hourDisplay, dayDisplay)
    val currentUnitToDisplay = prefs.getLocalUnit(context, currentUnit, intervalValue)

    // Validation for interval
    val isIntervalValid = currentUnit != "minutes" && intervalValue >= 1 || intervalValue >= 15
    val minIntervalValue = if (currentUnit == "minutes") 15 else 1

    //  Validation for limit
    val minLimit = NotificationPreferences.MIN_MAX_NOTIFICATION_COUNT
    val maxLimit = NotificationPreferences.MAX_MAX_NOTIFICATION_COUNT
    val isLimitValid = prefs.isNotificationCountValid(limitValue)

    // Track unsaved changes (both interval and limit)
    LaunchedEffect(intervalText, currentUnit, limitText) {
        val savedInterval = prefs.getNotificationInterval()
        val savedUnit = prefs.getNotificationIntervalUnit()
        val savedLimit = prefs.getMaxNotificationCount()

        hasUnsavedChanges = intervalText != savedInterval.toString() ||
                currentUnit != savedUnit ||
                limitText != savedLimit.toString()
    }

    // Frequency title
    Text(
        stringResource(R.string.notification_frequency),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium
    )

    Text(
        stringResource(R.string.notification_frequency_description),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 2.dp)
    )

    Spacer(Modifier.height(12.dp))

    // Interval input row
    IntervalInputRow(
        intervalText = "%d".format(intervalText.toIntOrNull() ?: 0),
        onIntervalTextChange = { newValue ->
            // Convert to Western digits first
            val westernDigits = newValue.map { char ->
                Character.getNumericValue(char).digitToChar()
            }.joinToString("")

            if (westernDigits.all { it.isDigit() } && westernDigits.length <= 2) {
                if (newValue.isEmpty()) intervalText = "0"
                // Check if the user is typing after a "0"
                else if (intervalText == "0" && newValue.length == 2) {
                    // User typed a digit after "0", replace "0" with the new digit
                    val lastChar = newValue.first()
                    intervalText = "%d".format(lastChar.toString().toInt())
                } else {
                    // Normal case
                    val intValue = westernDigits.toIntOrNull() ?: 0
                    intervalText = "%d".format(intValue)
                }
            }
        },
        currentUnitToDisplay = currentUnitToDisplay,
        listOfUnitsToDisplay = listOfUnitsToDisplay,
        onUnitSelected = { selectedUnit ->
            currentUnit = when (selectedUnit) {
                minuteDisplay -> "minutes"
                hourDisplay -> "hours"
                dayDisplay -> "days"
                else -> "hours"
            }
        }
    )

    // Status and validation messages for interval
    IntervalStatusDisplay(
        prefs = prefs,
        currentUnit = currentUnit,
        intervalValue = intervalValue,
        hasUnsavedChanges = hasUnsavedChanges,
        isValid = isIntervalValid,
        minValue = minIntervalValue,
        context = context
    )

    Spacer(Modifier.height(20.dp))

    //  NOTIFICATION LIMIT SETTINGS
    Text(
        stringResource(R.string.notification_limit),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )

    Text(
        stringResource(R.string.notification_limit_desc),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 4.dp)
    )

    Spacer(Modifier.height(12.dp))

    // Limit input row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number input field for limit
        OutlinedTextField(
            value = "%d".format(limitText.toIntOrNull() ?: 0),
            onValueChange = { newValue ->
                // Convert to Western digits first
                val westernDigits = newValue.map { char ->
                    Character.getNumericValue(char).digitToChar()
                }.joinToString("")

                if (westernDigits.all { it.isDigit() } && westernDigits.length <= 2) {
                    if (newValue.isEmpty()) limitText = "0"
                    // Check if the user is typing after a "0"
                    else if (limitText == "0" && newValue.length == 2) {
                        // User typed a digit after "0", replace "0" with the new digit
                        val lastChar = newValue.first()
                        limitText = "%d".format(lastChar.toString().toInt())
                    } else {
                        // Normal case
                        val intValue = westernDigits.toIntOrNull() ?: 0
                        limitText = "%d".format(intValue)
                    }
                }
            },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.maximum_notifications_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = !isLimitValid && limitValue > 0
        )

        // Info chip showing valid range
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                "${String.format("%d", minLimit)} - ${String.format("%d", maxLimit)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    //  Status and validation messages for limit
    Column {
        // Current settings
        Text(
            stringResource(
                R.string.currently_concurrent_notifications,
                prefs.getMaxNotificationCount(),
                pluralStringResource(R.plurals.notifications, prefs.getMaxNotificationCount())
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Pending changes
        if (hasUnsavedChanges && limitText != prefs.getMaxNotificationCount().toString()) {
            Text(
                stringResource(
                    R.string.will_be_concurrent_notifications, limitValue,
                    pluralStringResource(R.plurals.notifications, limitValue)
                ),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Validation status
        if (!isLimitValid) {
            Text(
                context.getString(
                    R.string.notification_limit_must_be_between_and,
                    NotificationPreferences.MIN_MAX_NOTIFICATION_COUNT,
                    NotificationPreferences.MAX_MAX_NOTIFICATION_COUNT
                ),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        } else {
            Text(
                stringResource(R.string.oldest_notifications_removed),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // Save button (saves both interval and limit)
    SaveSettingsButton(
        hasUnsavedChanges = hasUnsavedChanges,
        isIntervalValid = isIntervalValid,
        isLimitValid = isLimitValid,
        intervalText = intervalText,
        intervalValue = intervalValue,
        limitValue = limitValue,
        currentUnit = currentUnit,
        prefs = prefs,
        snackbarHostState = snackbarHostState,
        scope = scope,
        context = context,
        onSaveComplete = { hasUnsavedChanges = false }
    )
}

/**
 * INTERVAL INPUT ROW COMPOSABLE
 */
@Composable
fun IntervalInputRow(
    intervalText: String,
    onIntervalTextChange: (String) -> Unit,
    currentUnitToDisplay: String,
    listOfUnitsToDisplay: List<String>,
    onUnitSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Number input field
        IntervalTextField(
            value = intervalText,
            onValueChange = onIntervalTextChange,
            modifier = Modifier.weight(1f)
        )

        // Unit dropdown
        IntervalUnitDropdown(
            currentUnitToDisplay = currentUnitToDisplay,
            listOfUnitsToDisplay = listOfUnitsToDisplay,
            onUnitSelected = onUnitSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * INTERVAL TEXT FIELD COMPOSABLE
 */
@Composable
fun IntervalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(stringResource(R.string.interval)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

/**
 * INTERVAL UNIT DROPDOWN COMPOSABLE
 */
@Composable
fun IntervalUnitDropdown(
    currentUnitToDisplay: String,
    listOfUnitsToDisplay: List<String>,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(currentUnitToDisplay.replaceFirstChar { it.uppercase() })
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.select_time_unit)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOfUnitsToDisplay.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.replaceFirstChar { it.uppercase() }) },
                    onClick = { onUnitSelected(unit); expanded = false }
                )
            }
        }
    }
}

/**
 * INTERVAL STATUS DISPLAY COMPOSABLE
 */
@Composable
fun IntervalStatusDisplay(
    prefs: NotificationPreferences,
    currentUnit: String,
    intervalValue: Int,
    hasUnsavedChanges: Boolean,
    isValid: Boolean,
    minValue: Int,
    context: Context
) =
    Column {
        // Current settings
        Text(
            stringResource(
                R.string.currently_every,
                prefs.getNotificationInterval(),
                prefs.getLocalUnit(
                    context,
                    prefs.getNotificationIntervalUnit(),
                    prefs.getNotificationInterval()
                )
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Pending changes
        if (hasUnsavedChanges && intervalValue != prefs.getNotificationInterval()) {
            Text(
                stringResource(
                    R.string.will_be_every,
                    intervalValue,
                    prefs.getLocalUnit(context, currentUnit, intervalValue)
                ),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Validation status
        if (!isValid) Text(
            stringResource(
                R.string.must_be_at_least,
                minValue,
                prefs.getLocalUnit(context, currentUnit, minValue)
            ),
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        ) else Text(
            stringResource(R.string.minimum_15_minutes_1_hour_or_1_day),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }

/**
 * SAVE SETTINGS BUTTON COMPOSABLE
 */
@Composable
fun SaveSettingsButton(
    hasUnsavedChanges: Boolean,
    isIntervalValid: Boolean,
    isLimitValid: Boolean,
    intervalText: String,
    intervalValue: Int,
    limitValue: Int,
    currentUnit: String,
    prefs: NotificationPreferences,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    context: Context,
    onSaveComplete: () -> Unit
) {
    MovitoButton(
        text = if (hasUnsavedChanges) context.getString(R.string.save_changes)
        else stringResource(R.string.settings_applied),
        modifier = Modifier.fillMaxWidth(),
        roundedCornerSize = 12.dp,
        enabled = hasUnsavedChanges,
        onClick = {
            if (intervalText.isNotEmpty() && intervalValue > 0 && limitValue > 0) {
                if (isIntervalValid && isLimitValid) {
                    // Save interval settings
                    prefs.setNotificationInterval(intervalValue)
                    prefs.setNotificationIntervalUnit(currentUnit)

                    //  Save limit settings
                    val oldLimit = prefs.getMaxNotificationCount()
                    prefs.setMaxNotificationCount(limitValue)

                    // If limit decreased, cancel excess notifications
                    if (limitValue < oldLimit) {
                        NotificationHelper.cancelAllMovieSuggestions(context)
                    }

                    // Check for battery optimization guidance
                    val hasBatteryPermission =
                        BatteryPermissionHelper.hasUnrestrictedBatteryAccess(context)
                    val isGoogleDevice = Build.MANUFACTURER.equals(
                        "google", ignoreCase = true
                    )

                    if (!isGoogleDevice && !hasBatteryPermission)
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            when (
                                val result = snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.battery_permission),
                                    actionLabel = context.getString(R.string.grant),
                                    withDismissAction = true
                                )
                            ) {
                                SnackbarResult.ActionPerformed ->
                                    BatteryPermissionHelper.openBatteryOptimizationSettings(context)

                                SnackbarResult.Dismissed -> {
                                    NotificationScheduler.scheduleNotifications(context)
                                    onSaveComplete()
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.settings_applied_snackbar),
                                        withDismissAction = true
                                    )
                                }
                            }
                        }
                    else {
                        NotificationScheduler.scheduleNotifications(context)
                        onSaveComplete()
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.settings_applied_snackbar),
                                withDismissAction = true
                            )
                        }
                    }
                } else {
                    // Show error for invalid settings
                    scope.launch {
                        val errorMessage = buildString {
                            if (!isIntervalValid) {
                                append(context.getString(R.string.please))
                                append(" ")
                                append(context.getString(R.string.confirm_to_the_interval_constraints))
                            }
                            if (!isIntervalValid && !isLimitValid) {
                                append(" ")
                                append(context.getString(R.string.and))
                                append(" ")
                            }
                            if (!isLimitValid) {
                                append(context.getString(R.string.please))
                                append(" ")
                                append(context.getString(R.string.confirm_to_the_limit_constraints))
                            }
                        }
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = "❌ $errorMessage",
                            withDismissAction = true
                        )
                    }
                }
            } else scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.please_enter_a_valid_interval),
                    withDismissAction = true
                )
            }
        }
    )
}

/**
 * TEST NOTIFICATION SECTION COMPOSABLE
 *
 * PURPOSE: Provides debugging tools for notification system testing
 * and troubleshooting.
 */
@Composable
fun TestNotificationSection(
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    context: Context,
    prefs: NotificationPreferences,
    onNotificationsStateUpdate: (Boolean) -> Unit,
    updateNotificationState: () -> Unit
) =
    Column {
        // Section title
        Text(
            stringResource(R.string.test_notifications_title),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            stringResource(R.string.test_notifications_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Test buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Immediate test button
            MovitoButton(
                text = stringResource(R.string.test_notifications_notify_right_now_btn),
                onClick = {
                    NotificationScheduler.scheduleImmediateNotification(context)
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.test_notifications_now_msg),
                            withDismissAction = true
                        )
                    }
                },
                roundedCornerSize = 8.dp,
                modifier = Modifier.weight(1f)
            )

            // Repeated test button
            MovitoButton(
                text = stringResource(R.string.test_notifications_repeat_btn),
                onClick = {
                    NotificationScheduler.scheduleRepeatingTestNotifications(context)
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.test_notifications_repeat_snackbar_msg),
                            withDismissAction = true
                        )
                    }
                },
                roundedCornerSize = 8.dp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Cancel all button
        MovitoButton(
            text = stringResource(R.string.test_notifications_cancel_all_btn),
            modifier = Modifier.fillMaxWidth(),
            roundedCornerSize = 8.dp,
            onClick = {
                NotificationScheduler.cancelNotifications(context)
                updateNotificationState()
                prefs.setNotificationsEnabled(false)
                onNotificationsStateUpdate(false)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.cancelle_notifications_msg),
                        withDismissAction = true
                    )
                }
            }
        )
    }

/*
 * HELPER FUNCTIONS FOR NOTIFICATION MANAGEMENT
 *
 * PURPOSE: Contains shared logic for notification state management
 * and permission handling.
 */

/**
 * Handles notification toggle with permission checking
 */
private fun handleNotificationToggle(
    newValue: Boolean,
    context: Context,
    prefs: NotificationPreferences,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onNotificationsStateUpdate: (Boolean) -> Unit,
    updateNotificationState: (Boolean) -> Unit
) {
    if (newValue) {
        // User wants to enable notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - enable notifications
                enableNotifications(
                    context,
                    prefs,
                    onNotificationsStateUpdate,
                    updateNotificationState
                )
            } else {
                // Show snackbar and open notification settings
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.notification_permission_request),
                        actionLabel = context.getString(R.string.grant),
                        withDismissAction = true
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            NotificationHelper.openNotificationSettings(
                                context,
                                prefs,
                                onNotificationsStateUpdate
                            )
                        }

                        SnackbarResult.Dismissed -> {
                            // User dismissed, keep notifications disabled
                            updateNotificationState(false)
                        }
                    }
                }
            }
        } else {
            // Android 12 and below - no permission needed
            enableNotifications(context, prefs, onNotificationsStateUpdate, updateNotificationState)
        }
    } else {
        // User wants to disable notifications
        disableNotifications(context, prefs, onNotificationsStateUpdate, updateNotificationState)
    }
}

/**
 * Enables notifications and schedules them
 */
private fun enableNotifications(
    context: Context,
    prefs: NotificationPreferences,
    onNotificationsStateUpdate: (Boolean) -> Unit,
    updateNotificationState: (Boolean) -> Unit
) {
    prefs.setNotificationsEnabled(true)
    onNotificationsStateUpdate(true)
    updateNotificationState(true)
    NotificationScheduler.scheduleNotifications(context)
}

/**
 * Disables notifications and cancels all scheduling
 */
private fun disableNotifications(
    context: Context,
    prefs: NotificationPreferences,
    onNotificationsStateUpdate: (Boolean) -> Unit,
    updateNotificationState: (Boolean) -> Unit
) {
    prefs.setNotificationsEnabled(false)
    onNotificationsStateUpdate(false)
    updateNotificationState(false)
    NotificationScheduler.cancelNotifications(context)
}

/**
 * Checks current notification scheduling status
 */
private fun checkNotificationStatus(
    context: Context,
    onStatusChecked: (Boolean) -> Unit
) {
    val workManager = WorkManager.getInstance(context)
    val liveData: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(NotificationScheduler.WORK_NAME)

    liveData.observeForever { workInfos ->
        val isScheduled = workInfos.any { it.state == WorkInfo.State.ENQUEUED }
        onStatusChecked(isScheduled)
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