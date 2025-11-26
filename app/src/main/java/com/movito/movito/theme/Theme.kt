package com.movito.movito.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkButtonColor1,
    secondary = DarkButtonColor2,
    tertiary = Pink80,
    background = DarkBlueBackground,
    surface = DarkBlueBackground,
    onPrimary = Color.White,  // Changed from Black for better contrast
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    // NEW: Add missing Material 3 color roles
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = DarkTextSecondary,
    error = ErrorColor,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightButtonColor1,
    secondary = LightButtonColor2,
    tertiary = Pink40,
    background = LightBackground,
    surface = LightBackground,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,
    // NEW: Add missing Material 3 color roles
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    onError = Color.White
)

@Composable
fun MovitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Changed default to true for better user experience
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        content()
    }
}

// Extension functions for easy access to custom colors
val MaterialTheme.customColors
    @Composable
    get() = if (isSystemInDarkTheme()) DarkCustomColors else LightCustomColors

object DarkCustomColors {
    val star = StarColor
    val heart = HeartColor
    val success = SuccessColor
    val warning = WarningColor
    val error = ErrorColor
    val info = InfoColor
}

object LightCustomColors {
    val star = StarColor
    val heart = HeartColor
    val success = SuccessColor
    val warning = WarningColor
    val error = ErrorColor
    val info = InfoColor
}

// Utility composable for conditional theming
@Composable
fun WithMovitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    MovitoTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor
    ) {
        content()
    }
}

// Preview theme for easier preview development
@Composable
fun MovitoPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MovitoTheme(
        darkTheme = darkTheme,
        dynamicColor = false, // Disable dynamic color in previews for consistency
        content = content
    )
}