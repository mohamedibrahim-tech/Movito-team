package com.movito.movito.ui.common

import android.content.Intent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.theme.StarColor
import com.movito.movito.theme.movie
import com.movito.movito.ui.DetailsActivity

/**
 * A composable that displays a movie card with poster image, title, rating, and release year.
 * The card is clickable and navigates to the DetailsActivity when tapped.
 *
 * @param modifier Modifier for styling and layout
 * @param movie The movie data to display
 * @param intentToDetails Whether clicking the card should navigate to details screen
 * @param content Optional additional content to display over the movie poster
 */
@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    movie: Movie,
    intentToDetails: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(16.dp),
            clip = true
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            if (intentToDetails) {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra("movie", movie)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                context.startActivity(intent)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/w500${movie.posterPath}")
                    .crossfade(true)
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
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 400f
                        )
                    ),
                contentAlignment = Alignment.BottomStart
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
                        style = MaterialTheme.typography.movie.movieCardTitle,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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
                                style = MaterialTheme.typography.movie.movieCardRating,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = movie.releaseDate.take(4),
                            style = MaterialTheme.typography.movie.movieCardYear,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays a fully filled star icon for ratings.
 *
 * @param modifier Modifier for styling and layout
 */
@Composable
fun FullStar(modifier: Modifier) {
    Icon(
        imageVector = Icons.Rounded.Star,
        contentDescription = null,
        modifier = modifier,
        tint = StarColor
    )
}

/**
 * Displays an empty (outline) star icon for ratings.
 *
 * @param modifier Modifier for styling and layout
 */
@Composable
fun EmptyStar(modifier: Modifier) {
    Icon(
        imageVector = Icons.Rounded.StarBorder,
        contentDescription = null,
        modifier = modifier,
        tint = StarColor
    )
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
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
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
                    if (isRtl) {
                        // RTL: clip from left side, fill from right to left
                        clipRect(
                            left = this.size.width * (1f - fillFraction)
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    } else {
                        // LTR: clip from right side, fill from left to right
                        clipRect(
                            right = this.size.width * fillFraction
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    }
                },
            tint = StarColor
        )
    }
}

/**
 * Displays a customizable rating bar with star icons that can be fully filled,
 * partially filled, or empty based on the rating value.
 *
 * @param rating The current rating value (typically out of 10)
 * @param modifier Modifier for styling and layout
 * @param maxRating The maximum possible rating value (default: 10)
 * @param starsCount The number of stars to display (default: 5)
 * @param starSize The size of each star icon
 */
@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    maxRating: Float = 10f,
    starsCount: Int = 5,
    starSize: Dp = 24.dp
) {
    val fullStarValue = maxRating / starsCount
    val ratingRelativeToStars = rating / fullStarValue

    Row(modifier = modifier.fillMaxWidth()) {
        val starWeight = 1f / starsCount

        repeat(starsCount) { index ->
            val fraction = (ratingRelativeToStars - index).coerceIn(0f, 1f)

            when {
                fraction == 1f -> FullStar(
                    modifier = Modifier
                        .weight(starWeight)
                        .aspectRatio(1f)
                        .size(starSize)
                )

                fraction in 0f..0.99f -> PartialStar(
                    fillFraction = fraction,
                    modifier = Modifier
                        .weight(starWeight)
                        .aspectRatio(1f)
                        .size(starSize)
                )

                else -> EmptyStar(
                    modifier = Modifier
                        .weight(starWeight)
                        .aspectRatio(1f)
                        .size(starSize)
                )
            }
        }
    }
}

/**
 * A container card used for settings screens with consistent styling.
 *
 * @param content The content to display inside the settings card
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

/**
 * A custom-styled button with gradient background, loading state, and rounded corners.
 * Supports both light and dark themes with appropriate gradient colors.
 *
 * @param text The button text to display
 * @param modifier Modifier for styling and layout
 * @param roundedCornerSize The corner radius for the button shape
 * @param isLoading Whether the button is in loading state (shows spinner)
 * @param enabled Whether the button is enabled and interactive
 * @param onClick Callback when the button is clicked
 */
@Composable
fun MovitoButton(
    text: String,
    modifier: Modifier = Modifier,
    roundedCornerSize: Dp = 12.dp,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(roundedCornerSize),
                clip = false
            )
            .clip(RoundedCornerShape(roundedCornerSize))
            .background(
                brush = if (enabled)
                    Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                else
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
            )
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,

            )
        }
    }

}

/**
 * A container with glass morphism effect (transparent background with blur and border).
 * Creates a frosted glass appearance with gradient background and subtle border.
 *
 * @param modifier Modifier for styling and layout
 * @param shape The shape of the glass container
 * @param elevation The shadow elevation for depth
 * @param backgroundAlpha The transparency level of the background
 * @param colors The gradient colors for the glass effect
 * @param content The content to display inside the glass container
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 12.dp,
    backgroundAlpha: Float = 0.4f,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.surface.copy(alpha = backgroundAlpha),
        MaterialTheme.colorScheme.surface.copy(alpha = backgroundAlpha * 0.5f)
    ),
    content: @Composable BoxScope.() -> Unit
) {
    // Apply aspect ratio only for CircleShape
    val containerModifier = if (shape == CircleShape) {
        modifier.aspectRatio(1f)
    } else {
        modifier
    }

    Box(
        modifier = containerModifier.shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.15f),
            spotColor = Color.Black.copy(alpha = 0.25f)
        )
    ) {
        // Glass background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = colors
                    ),
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = shape
                )
        )

        content()
    }
}

@Preview("MovitoButton - All States", showBackground = true, widthDp = 200)
@Composable
fun MovitoButtonPreview_AllStates() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MovitoButton(
            text = "Normal State",
            onClick = { }
        )

        MovitoButton(
            text = "Loading State",
            isLoading = true,
            onClick = { }
        )

        MovitoButton(
            text = "Rounded",
            roundedCornerSize = 100.dp,
            onClick = { }
        )
    }
}


@Preview("MovitoButton - All States Both Themes", showBackground = true, widthDp = 200)
@Composable
fun MovitoButtonPreview_AllStatesBothThemes() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Light theme
        Text("Light Theme:", style = MaterialTheme.typography.bodyMedium)
        MovitoTheme(darkTheme = false) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MovitoButton(text = "Enabled", onClick = { })
                MovitoButton(text = "Disabled", enabled = false, onClick = { })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dark theme
        Text("Dark Theme:", style = MaterialTheme.typography.bodyMedium)
        MovitoTheme(darkTheme = true) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MovitoButton(text = "Enabled", onClick = { })
                MovitoButton(text = "Disabled", enabled = false, onClick = { })
            }
        }
    }
}

@Preview("MovieCard - Normal", widthDp = 180, heightDp = 300)
@Composable
fun MovieCardPreview_Normal() {
    val mockMovie = Movie(
        id = 1,
        title = "Interstellar",
        releaseDate = "2014-11-07",
        posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
        voteAverage = 8.3,
        overview = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
        genreIds = listOf(12, 18, 878)
    )

    MovitoTheme(darkTheme = false) {
        MovieCard(
            modifier = Modifier.size(180.dp, 300.dp),
            movie = mockMovie
        )
    }
}

@Preview("MovieCard - Long Title", widthDp = 180, heightDp = 300)
@Composable
fun MovieCardPreview_LongTitle() {
    val mockMovie = Movie(
        id = 2,
        title = "The Incredibly Long Movie Title That Never Ends",
        releaseDate = "2023-05-15",
        posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        voteAverage = 7.8,
        overview = "A movie with a very long title to test text wrapping and ellipsis.",
        genreIds = listOf(35, 10749)
    )

    MovitoTheme(darkTheme = false) {
        MovieCard(
            modifier = Modifier.size(180.dp, 300.dp),
            movie = mockMovie
        )
    }
}

@Preview("MovieCard - Low Rating", widthDp = 180, heightDp = 300)
@Composable
fun MovieCardPreview_LowRating() {
    val mockMovie = Movie(
        id = 4,
        title = "Low Budget Movie",
        releaseDate = "2022-01-01",
        posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        voteAverage = 3.2,
        overview = "A movie with low ratings to test the rating display.",
        genreIds = listOf(27, 53)
    )

    MovitoTheme(darkTheme = false) {
        MovieCard(
            modifier = Modifier.size(180.dp, 300.dp),
            movie = mockMovie
        )
    }
}

@Preview("MovieCard - Multiple Cards", widthDp = 400, heightDp = 350)
@Composable
fun MovieCardPreview_Multiple() {
    val mockMovies = listOf(
        Movie(
            id = 1,
            title = "Interstellar",
            releaseDate = "2014-11-07",
            posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
            voteAverage = 8.3,
            overview = "Space exploration movie.",
            genreIds = listOf(12, 18, 878)
        ),
        Movie(
            id = 2,
            title = "The Dark Knight",
            releaseDate = "2008-07-18",
            posterPath = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            voteAverage = 9.0,
            overview = "Batman movie.",
            genreIds = listOf(28, 80, 18)
        )
    )

    MovitoTheme(darkTheme = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            mockMovies.forEach { movie ->
                MovieCard(
                    modifier = Modifier.size(180.dp, 300.dp),
                    movie = movie
                )
            }
        }
    }
}

@Preview("PartialStar - Various Fill Levels", showBackground = true)
@Composable
fun PartialStarPreview_VariousLevels() {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Different fill levels
        listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f).forEach { fill ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(fill * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                PartialStar(
                    fillFraction = fill,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview("RatingBar - Various Ratings", showBackground = true)
@Composable
fun RatingBarPreview_VariousRatings() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Different ratings
        listOf(1.0f, 3.5f, 5.0f, 7.2f, 9.8f, 10.0f).forEach { rating ->
            Column {
                Text("Rating: $rating/10", style = MaterialTheme.typography.bodyMedium)
                RatingBar(
                    rating = rating,
                    modifier = Modifier.width(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview("GlassContainer - Rounded", showBackground = true, widthDp = 200, heightDp = 100,
    backgroundColor = 0xFF021E30)
@Composable
fun GlassContainerPreview_Rounded() {
    MovitoTheme(darkTheme = false) {
        GlassContainer(
            modifier = Modifier.size(200.dp, 100.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Glass Container",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview("GlassContainer - Circular", showBackground = true, widthDp = 100,
    backgroundColor = 0xFF021E30
)
@Composable
fun GlassContainerPreview_Circular() {
    MovitoTheme(darkTheme = false) {
        GlassContainer(
            modifier = Modifier.size(100.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview("GlassContainer - Different Shapes - dark background", showBackground = true, widthDp = 300, heightDp = 200,
    backgroundColor = 0xFF021E30
)
@Composable
fun GlassContainerPreview_DifferentShapes() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rounded rectangle
        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Rounded Rectangle",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Circular
        GlassContainer(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.CenterHorizontally),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Star",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}