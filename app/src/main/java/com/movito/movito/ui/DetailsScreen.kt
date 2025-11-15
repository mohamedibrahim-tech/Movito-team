package com.movito.movito.ui

import android.R.id.shareText
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.HeartColor
import com.movito.movito.theme.LightBackground
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.theme.StarColor
import com.movito.movito.ui.common.MovieCard
import com.movito.movito.ui.common.MovitoButton
import com.movito.movito.ui.common.PartialStar
import com.movito.movito.viewmodel.DetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    movie: Movie,
    modifier: Modifier = Modifier,
    initiallyFavorite: Boolean = false,
    onFavoriteChanged: (Boolean) -> Unit = {},
    onClickBackButton: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.trailerUrl) {
        uiState.trailerUrl?.let {
            val intent = Intent(Intent.ACTION_VIEW, it.toUri())
            context.startActivity(intent)
        }
    }

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
                    .fillMaxWidth(),
                movie = movie,
                intentToDetails = false,
                isItInFavorites = initiallyFavorite
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
                        "%.1f".format(movie.voteAverage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = contentColor
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    RatingBar(
                        rating = movie.voteAverage.toFloat(), modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }

                // Share button (25%)
                TextButton(
                    modifier = Modifier.weight(0.25f),
                    onClick = {
                        // Launch a coroutine to call the suspend function
                        CoroutineScope(Dispatchers.IO).launch {
                            val url = viewModel.getTrailerUrl(movieId = movie.id)
                            if (url == null)
                                Toast.makeText(context, "No Trailer Found.", Toast.LENGTH_SHORT)
                                    .show()
                            else url.let {
                                // Switch to main thread to show share dialog
                                withContext(Dispatchers.Main) { shareUrl(context, it) }
                            }
                        }
                    }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = contentColor,
                        modifier = Modifier.weight(0.2f)
                    )
                    Spacer(
                        modifier = Modifier
                            .width(4.dp)
                            .weight(0.1f)
                    )
                    Text(
                        text = "Share",
                        color = contentColor,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                // Play Trailer (35%)
                MovitoButton(
                    text = "Play Trailer",
                    modifier = Modifier.weight(0.35f),
                    isLoading = uiState.isLoading,
                    roundedCornerSize = 100.dp,
                    onClick = { viewModel.findTrailer(movie.id) })

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

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Overview: ${movie.overview}",
                        style = MaterialTheme.typography.bodyMedium,
                        softWrap = true
                    )
                }


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
    val chooser = Intent.createChooser(intent, "Share the trailer link")
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


@SuppressLint("ViewModelConstructorInComposable")
@Preview("Dark Details Screen preview", showSystemUi = true)
@Composable
fun DetailsScreenPreviewDark() {
    val mockMovie = Movie(
        1,
        "Cosmic Echoes",
        "2025-03-15",
        "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        8.5,
        "An epic space opera.",
        listOf(878)
    )

    val fakeVM = DetailsViewModel()

    MovitoTheme(true) {
        DetailsScreen(
            movie = mockMovie,
            viewModel = fakeVM,
            modifier = Modifier,
            initiallyFavorite = false,
            onFavoriteChanged = {},
            onClickBackButton = {},
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview("light Details Screen preview", showSystemUi = true)
@Composable
fun DetailsScreenPreviewLight() {
    val mockMovie = Movie(
        1,
        "Cosmic Echoes",
        "2025-03-15",
        "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        8.5,
        "An epic space opera.",
        listOf(878)
    )

    val fakeVM = DetailsViewModel()

    MovitoTheme(false) {
        DetailsScreen(
            movie = mockMovie,
            viewModel = fakeVM,
            modifier = Modifier,
            initiallyFavorite = false,
            onFavoriteChanged = {},
            onClickBackButton = {},
        )
    }
}
