package com.movito.movito.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.HeartColor
import com.movito.movito.theme.LightBackground
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.theme.StarColor
import com.movito.movito.ui.common.MovieCard
import com.movito.movito.ui.common.MovitoButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    movie: Movie,
    modifier: Modifier = Modifier,
    initiallyFavorite: Boolean = false,
    onFavoriteChanged: (Boolean) -> Unit = {},
    onClickBackButton: () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Details") }, navigationIcon = {
                IconButton(onClick = onClickBackButton) {
                    Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, "back")
                }
            })
        },
    ) { innerPadding ->

        val contentColor = MaterialTheme.colorScheme.onBackground
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
                .fillMaxSize()
        ) {

            // Movie image section (30% of screen)
            MovieCard(
                modifier = Modifier
                    .weight(0.30f)
                    .fillMaxWidth(), movie = movie
            )

            // Info + actions section (15%)
            Row(
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Rating column (25%)
                Column(
                    modifier = Modifier.weight(0.25f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${movie.vote_avg}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = contentColor
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    RatingBar(
                        rating = movie.vote_avg, modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }

                val context = LocalContext.current
                // Share button (25%)
                TextButton(
                    modifier = Modifier.weight(0.25f),
                    onClick = { shareUrl(context, movie.homePage) }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Share", color = contentColor)
                }

                // Play Trailer (35%)
                MovitoButton(
                    text = "Play Trailer",
                    modifier = Modifier.weight(0.35f),
                    roundedCornerSize = 100.dp
                ) {}

                // Favorite button (15%)
                var isFavorite by remember { mutableStateOf(initiallyFavorite) }
                IconButton(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(0.15f)
                        .background(LightBackground, RoundedCornerShape(100.dp)), onClick = {
                        isFavorite = !isFavorite
                        onFavoriteChanged(isFavorite)
                    }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) HeartColor else Color.Black
                    )
                }
            }

            // Details text section (30%)
            Column(
                modifier = Modifier
                    .weight(0.30f)
                    .fillMaxWidth()
            ) {

                Text(
                    "Movie Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor
                )

                Text(
                    "overview: ${movie.overview}",
                    style = MaterialTheme.typography.bodyMedium,
                    softWrap = true
                )

            }

            // Details text section (25%)
            Column(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxWidth()
            ) {
                Text(
                    "More Movies", style = MaterialTheme.typography.titleLarge, color = contentColor
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                ) {

                }

            }

        }
    }
}


fun shareUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    val chooser = Intent.createChooser(intent, "Share via")
    context.startActivity(chooser)
}


@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    maxRating: Float = 10f,
    starsCount: Int = 5,
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
                )

                fraction in 0f..0.99f -> PartialStar(
                    fillFraction = fraction, modifier = Modifier
                        .weight(starWeight)
                        .aspectRatio(1f)
                )

                else -> EmptyStar(
                    modifier = Modifier
                        .weight(starWeight)
                        .aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
fun FullStar(modifier: Modifier) {
    Icon(
        imageVector = Icons.Rounded.Star,
        contentDescription = null,
        modifier = modifier,
        tint = StarColor
    )
}

@Composable
fun EmptyStar(modifier: Modifier) {
    Icon(
        imageVector = Icons.Rounded.StarBorder,
        contentDescription = null,
        modifier = modifier,
        tint = StarColor
    )
}

@Composable
fun PartialStar(fillFraction: Float, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Background star
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
                    clipRect(right = size.width * fillFraction) {
                        this@drawWithContent.drawContent()
                    }
                },
            tint = StarColor
        )
    }
}

@Preview("Dark Details Screen preview", showSystemUi = true)
@Composable
fun DetailsScreenPreviewDark() {
    val mockMovie = Movie(
        1,
        "Cosmic Echoes",
        "2025",
        "2h 15m",
        "https://image.tmdb.org/t/p/w500/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg"
    )
    MovitoTheme (true){
        DetailsScreen(movie = mockMovie) { TODO() }
    }
}

@Preview("light Details Screen preview", showSystemUi = true)
@Composable
fun DetailsScreenPreviewLight() {
    val mockMovie = Movie(
        1,
        "Cosmic Echoes",
        "2025",
        "2h 15m",
        "https://image.tmdb.org/t/p/w500/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg"
    )
    MovitoTheme (false){
        DetailsScreen(movie = mockMovie) { TODO() }
    }
}
