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
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
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
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    onError = Color.White
)

@Composable
fun MovitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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