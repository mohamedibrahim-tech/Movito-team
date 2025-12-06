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

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF84342),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF7B1E21),    // darker red container
    onPrimaryContainer = Color.White,

    secondary = Color(0xFFD30E77),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF6A0040),  // darker magenta container
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFF336DD3),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0C4AA0),
    onTertiaryContainer = Color.White,

    background = Color(0xFF121212),
    onBackground = Color(0xFFE8E8E8),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE8E8E8),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFFA08C8B)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFFF84342),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFB4A8),
    onPrimaryContainer = Color(0xFF410001),

    secondary = Color(0xFFD30E77),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFC4DD),
    onSecondaryContainer = Color(0xFF370027),

    tertiary = Color(0xFF336DD3),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD7E2FF),
    onTertiaryContainer = Color(0xFF001A42),

    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A1A),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFF4DDDC),
    onSurfaceVariant = Color(0xFF534342),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF857372)
)

@Composable
fun MovitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        content = content
    )
}