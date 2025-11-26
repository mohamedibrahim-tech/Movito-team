package com.movito.movito.ui.common

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.DarkButtonColor1
import com.movito.movito.theme.DarkButtonColor2
import com.movito.movito.theme.LightButtonColor1
import com.movito.movito.theme.LightButtonColor2
import com.movito.movito.theme.StarColor

@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    movie: Movie,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit = {}
) {
    Card(
        modifier = modifier, 
        shape = RoundedCornerShape(16.dp), 
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/w500${movie.posterPath}").crossfade(true)
                    .crossfade(400)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(id = R.drawable.poster_test)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black), startY = 400f
                        )
                    ), contentAlignment = Alignment.BottomStart
            ) {

                content()

                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PartialStar(
                                fillFraction = movie.voteAverage.toFloat() * 0.1f,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "%.1f".format(movie.voteAverage),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = movie.releaseDate.take(4), // لعرض السنة فقط
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draws a partially-filled star icon based on a given fill fraction.
 *
 * This is used for ratings where a star can be fully filled, half filled,
 * or any fractional amount. Internally, it renders two stars:
 *
 * 1. **StarBorder** (background, always visible)
 * 2. **Star** (foreground, clipped horizontally based on `fillFraction`)
 *
 * The `fillFraction` controls how much of the foreground star is visible:
 * - `0f` → empty star
 * - `1f` → fully filled star
 * - Any value in between → partially filled star
 *
 * @param fillFraction A value between 0f and 1f indicating how much of the star to fill.
 * @param modifier Modifier applied to the star container.
 */
@Composable
fun PartialStar(fillFraction: Float, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Rounded.StarBorder,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = StarColor
        )

        // Foreground filled star (clipped)
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(
                        right = this.size.width * fillFraction
                    ) {
                        this@drawWithContent.drawContent()
                    }
                },
            tint = StarColor
        )
    }
}

/**
 *   كود الـ SettingsCards (منقول من SettingsActivity.kt)
 */
@Composable
fun SettingsCards(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun MovitoButton(
    text: String,
    modifier: Modifier = Modifier,
    roundedCornerSize: Dp = 12.dp,
    isDarkMode: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(roundedCornerSize))
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (isDarkMode) listOf(LightButtonColor1, LightButtonColor2)
                    else listOf(DarkButtonColor1, DarkButtonColor2)
                )
            )
            .clickable(
                enabled = !isLoading,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ), contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}