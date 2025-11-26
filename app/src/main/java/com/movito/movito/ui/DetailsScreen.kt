package com.movito.movito.ui

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.movito.movito.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch


@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    movie: Movie,
    modifier: Modifier = Modifier,
    onClickBackButton: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val favoritesViewModel = remember { FavoritesViewModel.getInstance() }
    val favoritesState by favoritesViewModel.uiState.collectAsState()
    val isFavorite = remember(favoritesState.favorites, movie.id) {
        favoritesState.favorites.any { it.id == movie.id }
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Add Dialog
    if (showAddDialog) {
        AddToFavoritesDialog(
            movieTitle = movie.title,
            onConfirm = {
                favoritesViewModel.addToFavorites(movie)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "${movie.title} added to favorites",
                        actionLabel = "Favorites"
                    )
                    if (result.toString() == "ActionPerformed") {
                        val intent = Intent(context, FavoritesActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            },
            onDismiss = { showAddDialog = false }
        )
    }

// Remove Dialog
    if (showRemoveDialog) {
        RemoveFromFavoritesDialog(
            movieTitle = movie.title,
            onConfirm = {
                favoritesViewModel.removeFromFavorites(movie.id)

            },
            onDismiss = { showRemoveDialog = false }
        )
    }

    LaunchedEffect(uiState.trailerUrl) {
        uiState.trailerUrl?.let {
            val intent = Intent(Intent.ACTION_VIEW, it.toUri())
            context.startActivity(intent)
            viewModel.onTrailerLaunched()
        }
    }

    LaunchedEffect(uiState.urlToShare) {
        uiState.urlToShare?.let {
            shareUrl(context, it)
            viewModel.onUrlShared()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.takeIf { it.contains("Trailer", ignoreCase = true) }?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            // Movie image section (25% of screen)
            MovieCard(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxWidth(),
                movie = movie,
                intentToDetails = false,
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
                    onClick = { viewModel.prepareShareUrl(movieId = movie.id) }
                ) {
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
                IconButton(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(0.15f)
                        .background(LightBackground, RoundedCornerShape(100.dp)),
                    onClick = {
                        if (isFavorite) {
                            showRemoveDialog = true
                        } else {
                            showAddDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites"
                        else "Add to favorites",
                        tint = if (isFavorite) HeartColor else Color.Black
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

            viewModel.loadRecommendations(movie.id)

            // Details text section (35%)
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxWidth(),
            ) {
                Text(
                    modifier = Modifier.weight(0.15f).padding(vertical = 4.dp),
                    text = "More Like This",
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor
                )

                if (uiState.recommendedMovies.isNotEmpty())
                    LazyRow(
                        modifier = Modifier.weight(0.85f).fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.recommendedMovies,
                            key = { it.id },
                            contentType = { "movie" }
                        )
                        { movie ->
                            MovieCard(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(144.dp),
                                movie = movie
                            )
                        }
                    }
                else
                    Text(
                        modifier = Modifier
                            .weight(0.15f)
                            .fillMaxSize(),
                        text = if (viewModel.uiState.value.error?.contains("Recommendations")
                                ?: false
                        )
                            "Error while loading recommendation!\nplease check the internet connection."
                        else
                            "Sorry, there are no recommendations for this movie 😕",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor

                    )
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
            onClickBackButton = {},
        )
    }
}