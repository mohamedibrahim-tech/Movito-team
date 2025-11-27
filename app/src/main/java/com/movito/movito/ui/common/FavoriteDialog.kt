package com.movito.movito.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movito.movito.theme.FunctionalColors
import com.movito.movito.theme.HeartColor
import com.movito.movito.theme.MovitoTheme

/**
 * Enum representing the type of favorite dialog to display.
 *
 * - ADD: Dialog for adding a movie to favorites
 * - REMOVE: Dialog for removing a movie from favorites
 */
enum class FavoriteDialogType {
    ADD, REMOVE
}

/**
 * Configuration data class for FavoriteDialog.
 *
 * This class holds all the necessary information to configure the appearance
 * and behavior of the favorite dialog, including type, content, and callbacks.
 *
 * @param type The type of dialog (add or remove)
 * @param movieTitle The title of the movie being acted upon
 * @param onConfirm Callback when user confirms the action
 * @param onDismiss Callback when user dismisses the dialog
 */
data class FavoriteDialogConfig(
    val type: FavoriteDialogType,
    val movieTitle: String,
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit
) {
    /**
     * The icon to display based on dialog type.
     * - ADD: Filled heart icon
     * - REMOVE: Outline heart icon
     */
    val icon: ImageVector
        get() = when (type) {
            FavoriteDialogType.ADD -> Icons.Filled.Favorite
            FavoriteDialogType.REMOVE -> Icons.Outlined.FavoriteBorder
        }

    /**
     * The color for the icon based on dialog type.
     * - ADD: Heart color (typically red/pink)
     * - REMOVE: Error color for destructive action
     */
    val iconColor
        @Composable
        get() = when (type) {
            FavoriteDialogType.ADD -> HeartColor
            FavoriteDialogType.REMOVE -> FunctionalColors.Error
        }

    /**
     * The title text based on dialog type.
     * - ADD: "Add to Favorites?"
     * - REMOVE: "Remove from Favorites?"
     */
    val title: String
        get() = when (type) {
            FavoriteDialogType.ADD -> "Add to Favorites?"
            FavoriteDialogType.REMOVE -> "Remove from Favorites?"
        }

    /**
     * The message text based on dialog type.
     * - ADD: Asks if user wants to add to favorites
     * - REMOVE: Confirms removal from favorites
     */
    val message: String
        get() = when (type) {
            FavoriteDialogType.ADD -> "Do you want to add this movie to your favorites?"
            FavoriteDialogType.REMOVE -> "Are you sure you want to remove this movie from your favorites?"
        }

    /**
     * The text for the confirm button based on dialog type.
     * - ADD: "Add"
     * - REMOVE: "Remove"
     */
    val confirmButtonText: String
        get() = when (type) {
            FavoriteDialogType.ADD -> "Add"
            FavoriteDialogType.REMOVE -> "Remove"
        }
}

/**
 * A dialog for confirming favorite movie actions (add/remove).
 *
 * Displays a glass-morphism styled dialog with appropriate icon, title,
 * movie title, message, and action buttons based on the configuration.
 *
 * @param config The configuration for dialog appearance and behavior
 */
@Composable
fun FavoriteDialog(
    config: FavoriteDialogConfig
) {
    Dialog(
        onDismissRequest = config.onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FavoriteDialogContent(config)
    }
}

/**
 * The main content of the FavoriteDialog.
 *
 * Arranges the dialog elements in a column with weighted sections:
 * - Icon (40%): Animated heart icon
 * - Title (10%): Action title (Add/Remove)
 * - Movie Title (15%): The movie being acted upon
 * - Message (20%): Explanatory text
 * - Buttons (15%): Cancel and action buttons
 *
 * @param config The configuration for dialog content
 */
@Composable
private fun FavoriteDialogContent(
    config: FavoriteDialogConfig
) {
    GlassContainer(
        modifier = Modifier
            .size(400.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 1f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        backgroundAlpha = 0.6f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            // Icon (40% of the dialog) - Uses heartbeat animation for add action
            HeartBeatIcon(
                imageVector = config.icon,
                contentDescription = null,
                tint = config.iconColor,
                modifier = Modifier
                    .weight(0.40f)
                    .size(100.dp),
                trigger = if (config.type == FavoriteDialogType.ADD) 0 else null
            )

            // Title (the Action remove/add) (10% of the dialog)
            Text(
                modifier = Modifier.weight(0.10f),
                text = config.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            // Movie Title (15% of the dialog)
            Text(
                modifier = Modifier.weight(0.15f),
                text = config.movieTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            // Message (20% of the dialog)
            Text(
                modifier = Modifier.weight(0.20f),
                text = config.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Buttons (15% of the dialog)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Cancel Button (50%) - Uses subtle glass container
                GlassContainer(
                    modifier = Modifier.weight(0.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    backgroundAlpha = 0.4f
                ) {
                    TextButton(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent, RoundedCornerShape(16.dp)),
                        onClick = config.onDismiss,
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                // Action Button (50%) - Uses primary MovitoButton
                Box(modifier = Modifier.weight(0.5f)) {
                    MovitoButton(
                        text = config.confirmButtonText,
                        roundedCornerSize = 12.dp,
                        onClick = {
                            config.onConfirm()
                            config.onDismiss()
                        }
                    )
                }
            }
        }
    }
}

/**
 * An icon with heartbeat animation effect.
 *
 * Scales the icon in a heartbeat pattern when triggered. The animation
 * sequence: normal → big → small → normal to create a pulsating effect.
 *
 * @param imageVector The icon to display
 * @param contentDescription Accessibility description for the icon
 * @param tint The color of the icon
 * @param modifier Modifier for styling and layout
 * @param trigger When non-null, triggers the animation. Use different values
 *                to retrigger. Animation only plays for ADD actions.
 */
@Composable
fun HeartBeatIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
    trigger: Int? = null
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(trigger) {
        // Only animate when trigger is not null
        if (trigger != null) {
            // Heartbeat animation sequence
            scale.snapTo(1f)
            scale.animateTo(1.30f, tween(200, easing = FastOutSlowInEasing))  // Beat up
            scale.animateTo(0.85f, tween(100, easing = LinearOutSlowInEasing)) // Beat down
            scale.animateTo(1.10f, tween(100, easing = FastOutSlowInEasing))  // Second beat up
            scale.animateTo(1.00f, tween(200, easing = LinearEasing))         // Back to normal
        }
    }

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.scale(scale.value)
    )
}

// Preview functions
@Preview("Favorite Dialog - Add", showBackground = true)
@Composable
fun FavoriteDialogPreview_Add() {
    MovitoTheme(darkTheme = false) {
        FavoriteDialog(
            config = FavoriteDialogConfig(
                type = FavoriteDialogType.ADD,
                movieTitle = "The Dark Knight",
                onConfirm = { },
                onDismiss = { }
            )
        )
    }
}

@Preview("Favorite Dialog - Remove", showBackground = true)
@Composable
fun FavoriteDialogPreview_Remove() {
    MovitoTheme(darkTheme = false) {
        FavoriteDialog(
            config = FavoriteDialogConfig(
                type = FavoriteDialogType.REMOVE,
                movieTitle = "Inception",
                onConfirm = { },
                onDismiss = { }
            )
        )
    }
}

@Preview("Favorite Dialog - Add Dark", showBackground = true)
@Composable
fun FavoriteDialogPreview_AddDark() {
    MovitoTheme(darkTheme = true) {
        FavoriteDialog(
            config = FavoriteDialogConfig(
                type = FavoriteDialogType.ADD,
                movieTitle = "Interstellar",
                onConfirm = { },
                onDismiss = { }
            )
        )
    }
}

@Preview("Favorite Dialog - Remove Dark", showBackground = true)
@Composable
fun FavoriteDialogPreview_RemoveDark() {
    MovitoTheme(darkTheme = true) {
        FavoriteDialog(
            config = FavoriteDialogConfig(
                type = FavoriteDialogType.REMOVE,
                movieTitle = "The Shawshank Redemption",
                onConfirm = { },
                onDismiss = { }
            )
        )
    }
}

@Preview("Favorite Dialog - Long Title", showBackground = true)
@Composable
fun FavoriteDialogPreview_LongTitle() {
    MovitoTheme(darkTheme = false) {
        FavoriteDialog(
            config = FavoriteDialogConfig(
                type = FavoriteDialogType.ADD,
                movieTitle = "The Incredibly Long Movie Title That Never Ends And Goes On Forever",
                onConfirm = { },
                onDismiss = { }
            )
        )
    }
}