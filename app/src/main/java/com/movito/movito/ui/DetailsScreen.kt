package com.movito.movito.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.movito.movito.R
import com.movito.movito.data.model.Genre
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.HeartColor
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.FavoriteDialog
import com.movito.movito.ui.common.FavoriteDialogConfig
import com.movito.movito.ui.common.FavoriteDialogType
import com.movito.movito.ui.common.GlassContainer
import com.movito.movito.ui.common.HeartBeatIcon
import com.movito.movito.ui.common.MovieCard
import com.movito.movito.ui.common.MovitoButton
import com.movito.movito.ui.common.RatingBar
import com.movito.movito.viewmodel.DetailsViewModel
import com.movito.movito.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch


/**
 * Main details screen showing comprehensive movie information.
 *
 * Features:
 * - Blurred background with movie poster
 * - Poster, rating, and action buttons section
 * - Scrollable movie overview
 * - Genre tags display
 * - Movie recommendations carousel
 * - Favorite management with dialogs
 *
 * @param viewModel ViewModel handling movie details and recommendations
 * @param movie The movie to display details for
 * @param modifier Modifier for styling and layout
 * @param onClickBackButton Callback when back button is pressed
 */
@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    movie: Movie,
    modifier: Modifier = Modifier,
    onClickBackButton: () -> Unit,
)
{
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val favoritesViewModel = remember { FavoritesViewModel.getInstance() }
    val favoritesState by favoritesViewModel.uiState.collectAsState()
    val isFavorite = remember(favoritesState.favorites, movie.id) {
        favoritesState.favorites.any { it.id == movie.id }
    }
    // Dialog state control
    var showAddDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    var heartAnimationTrigger by remember { mutableStateOf<Int?>(if (isFavorite) 0 else null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle dialogs
    if (showAddDialog) {
        FavoriteDialog(
            config = FavoriteDialogConfig(
                type = FavoriteDialogType.ADD,
                movieTitle = movie.title,
                onConfirm = {
                    favoritesViewModel.addToFavorites(movie)
                    heartAnimationTrigger = (heartAnimationTrigger ?: 0) + 1
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = context.getString(R.string.details_added_to_favorites, movie.title),
                            actionLabel = context.getString(R.string.details_favorites_button)
                        )
                        if (result.toString() == "ActionPerformed") {
                            val intent = Intent(context, FavoritesActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                },
                onDismiss = { showAddDialog = false }
            )
        )
    }

    if (showRemoveDialog) {
        FavoriteDialog(
            config = FavoriteDialogConfig(
                type = FavoriteDialogType.REMOVE,
                movieTitle = movie.title,
                onConfirm = { favoritesViewModel.removeFromFavorites(movie.id) },
                onDismiss = { showRemoveDialog = false }
            )
        )
    }

    // Handle side effects
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
            TopAppBar(
                title = { Text(stringResource(id = R.string.details_title)) },                navigationIcon = {
                    IconButton(onClick = onClickBackButton) {
                        Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, stringResource(id = R.string.details_back_button_description))                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
    ) { innerPadding ->
        BackgroundWithBlur(movie.posterPath) {
            // Main content column with proper weight distribution
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // SECTION 1: Poster + Actions (35% of screen)
                Box(
                    modifier = Modifier.weight(0.35f)
                ) {
                    MoviePosterAndActionsSection(
                        movie = movie,
                        isLoading = uiState.isLoading,
                        isFavorite = isFavorite,
                        heartAnimationTrigger = heartAnimationTrigger,
                        onPlayTrailer = { viewModel.findTrailer(movie.id) },
                        onShare = { viewModel.prepareShareUrl(movie.id) },
                        onFavoriteClick = {
                            if (isFavorite) showRemoveDialog = true else showAddDialog = true
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // SECTION 2: Movie Overview (25% of screen)
                Box(
                    modifier = Modifier.weight(0.25f)
                ) {
                    MovieOverviewSection(
                        overview = movie.overview,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // SECTION 3: Genre Tags (5% of screen)
                Box(
                    modifier = Modifier.weight(0.05f)
                ) {
                    GenresSection(
                        genres = uiState.genres,
                        movieGenreIds = movie.genreIds,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // SECTION 4: Recommendations (35% of screen)
                Box(
                    modifier = Modifier.weight(0.35f)
                ) {
                    // Load recommendations when section is composed
                    LaunchedEffect(Unit) {
                        viewModel.loadRecommendations(movieId = movie.id)
                    }
                    RecommendationsSection(
                        recommendedMovies = uiState.recommendedMovies,
                        error = uiState.error,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

/**
 * Background with blurred movie poster and gradient overlay.
 */
@Composable
private fun BackgroundWithBlur(
    posterPath: String?,
    content: @Composable () -> Unit
)
{
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://image.tmdb.org/t/p/w500${posterPath}")
                .crossfade(true)
                .crossfade(400)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to MaterialTheme.colorScheme.background,
                            0.30f to Color.Transparent,
                            0.70f to MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            content()
        }
    }
}

/**
 * Displays the main movie poster alongside rating info and action buttons.
 * Uses weighted layout to maintain proportional sizing across screen sizes.
 */
@Composable
fun MoviePosterAndActionsSection(
    movie: Movie,
    isLoading: Boolean,
    isFavorite: Boolean,
    heartAnimationTrigger: Int?,
    onPlayTrailer: () -> Unit,
    onShare: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
)
{
    val contentColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Movie poster (50% width)
        MovieCard(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5f),
            movie = movie,
            intentToDetails = false,
        )

        // Actions column (50% width)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Top spacer (15% height)
            Spacer(modifier = Modifier.weight(0.15f))

            // Rating section (30% height)
            Column(
                modifier = Modifier.weight(0.30f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "%.2f".format(movie.voteAverage),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = contentColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                RatingBar(
                    rating = movie.voteAverage.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Play Trailer button (20% height)
            MovitoButton(
                text = stringResource(id = R.string.details_play_trailer_button),                modifier = Modifier.weight(0.20f),
                isLoading = isLoading,
                roundedCornerSize = 100.dp,
                onClick = onPlayTrailer
            )

            // Share + Favorite buttons (20% height)
            Row(
                modifier = Modifier.weight(0.20f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share button (80% width)
                GlassContainer(
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    TextButton(
                        modifier = Modifier.fillMaxSize(),
                        onClick = onShare
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share",
                                color = contentColor,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Favorite button (20% width)
                GlassContainer(
                    modifier = Modifier.weight(0.2f),
                    shape = CircleShape
                ) {
                    IconButton(
                        modifier = Modifier.fillMaxSize(),
                        onClick = onFavoriteClick
                    ) {
                        HeartBeatIcon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites"
                            else "Add to favorites",
                            tint = if (isFavorite) HeartColor else contentColor,
                            trigger = heartAnimationTrigger
                        )
                    }
                }
            }

            // Bottom spacer (15% height)
            Spacer(modifier = Modifier.weight(0.15f))
        }
    }
}

/**
 * Shows the movie overview/description in a scrollable text area.
 * Handles long text content with proper scrolling and text justification.
 */
@Composable
fun MovieOverviewSection(
    overview: String,
    modifier: Modifier = Modifier
)
{
    Column(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = "Movie Details",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = overview,
                style = MaterialTheme.typography.bodyMedium,
                softWrap = true,
                textAlign = TextAlign.Justify,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Displays genre tags for the movie in a horizontal scrollable row.
 * Matches genre IDs with names from the provided genres list.
 */
@Composable
fun GenresSection(
    genres: List<Genre>,
    movieGenreIds: List<Int>?,
    modifier: Modifier = Modifier
)
{
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.25f),
            text = "Genres: ",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.75f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movieGenreIds ?: emptyList(), key = { it }) { genreId ->
                GenreChip(genreName = genres.firstOrNull { it.id == genreId }?.name ?: "Unknown")
            }
        }
    }
}

/**
 * Individual genre tag with glassmorphism styling.
 * Used within the GenresSection to display each genre.
 */
@Composable
private fun GenreChip(genreName: String) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp),
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(all = 8.dp)
    ) {
        Text(
            text = genreName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
    }
}

/**
 * Shows recommended movies in a horizontal carousel.
 * Handles empty states and error messages gracefully.
 */
@Composable
fun RecommendationsSection(
    recommendedMovies: List<Movie>,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier
                .weight(0.2f)
                .padding(vertical = 8.dp),
            text = "More Like This",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (recommendedMovies.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recommendedMovies, key = { it.id }) { movie ->
                    MovieCard(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(144.dp),
                        movie = movie
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (error?.contains("Recommendations") == true) {
                        "Error loading recommendations!\nCheck your connection."
                    } else {
                        "No recommendations available"
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

/**
 * Shares a URL using Android's share intent.
 */
fun shareUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    val chooser = Intent.createChooser(intent, "Share the trailer link")
    context.startActivity(chooser)
}

/**
 * Preview-only version of DetailsScreen for design and testing.
 * Uses the same UI components but with mock data and no ViewModel dependencies.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreenPreview(
    movie: Movie,
    genres: List<Genre> = emptyList(),
    recommendedMovies: List<Movie> = emptyList(),
    isLoading: Boolean = false,
    error: String? = null,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPlayTrailer: () -> Unit = {},
    onShare: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
)
{
    var heartAnimationTrigger by remember { mutableStateOf<Int?>(if (isFavorite) 0 else null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, "back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.poster_test), // Add this to your drawables
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 16.dp)
            )
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.01f to MaterialTheme.colorScheme.background,
                                0.29f to Color.Transparent,
                                0.70f to MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(24.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MoviePosterAndActionsSection(
                        movie = movie,
                        isLoading = isLoading,
                        isFavorite = isFavorite,
                        heartAnimationTrigger = heartAnimationTrigger,
                        onPlayTrailer = onPlayTrailer,
                        onShare = onShare,
                        onFavoriteClick = onFavoriteClick,
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxWidth()
                    )

                    MovieOverviewSection(
                        overview = movie.overview,
                        modifier = Modifier
                            .weight(0.25f)
                            .fillMaxWidth()
                    )

                    GenresSection(
                        genres = genres,
                        movieGenreIds = movie.genreIds,
                        modifier = Modifier
                            .weight(0.05f)
                            .fillMaxWidth()
                    )

                    RecommendationsSection(
                        recommendedMovies = recommendedMovies,
                        error = error,
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Preview: Normal movie in light theme, not in favorites
 * Tests: Basic layout, light theme, empty recommendations
 */
@Preview("Light Theme - Normal", showSystemUi = true)
@Composable
fun DetailsScreenPreview_LightNormal() {
    val mockMovie = Movie(
        id = 1,
        title = "Interstellar",
        releaseDate = "2014-11-07",
        posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
        voteAverage = 6.35,
        overview = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
        genreIds = listOf(12, 18, 878)
    )

    val mockGenres = listOf(
        Genre(12, "Adventure"),
        Genre(18, "Drama"),
        Genre(878, "Science Fiction")
    )

    MovitoTheme(darkTheme = false) {
        DetailsScreenPreview(
            movie = mockMovie,
            genres = mockGenres,
            isFavorite = false
        )
    }
}

/**
 * Preview: Movie in dark theme, marked as favorite
 * Tests: Dark theme, favorite state, heart icon animation
 */
@Preview("Dark Theme - Favorite", showSystemUi = true)
@Composable
fun DetailsScreenPreview_DarkFavorite() {
    val mockMovie = Movie(
        id = 2,
        title = "The Dark Knight",
        releaseDate = "2008-07-18",
        posterPath = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
        voteAverage = 9.0,
        overview = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham.",
        genreIds = listOf(28, 80, 18, 53)
    )

    val mockGenres = listOf(
        Genre(28, "Action"),
        Genre(80, "Crime"),
        Genre(18, "Drama"),
        Genre(53, "Thriller")
    )

    MovitoTheme(darkTheme = true) {
        DetailsScreenPreview(
            movie = mockMovie,
            genres = mockGenres,
            isFavorite = true
        )
    }
}

/**
 * Preview: With loaded recommendations
 * Tests: Recommendation carousel, multiple items, layout with content
 */
@Preview("With Recommendations", showSystemUi = true)
@Composable
fun DetailsScreenPreview_WithRecommendations() {
    val mockMovie = Movie(
        id = 3,
        title = "Inception",
        releaseDate = "2010-07-16",
        posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        voteAverage = 8.8,
        overview = "A thief who steals corporate secrets through dream-sharing technology.",
        genreIds = listOf(28, 878, 53)
    )

    val mockGenres = listOf(
        Genre(28, "Action"),
        Genre(878, "Science Fiction"),
        Genre(53, "Thriller")
    )

    val recommendedMovies = listOf(
        Movie(
            4,
            "The Matrix",
            "1999-03-31",
            "/poster1.jpg",
            8.7,
            "Virtual reality action",
            listOf(28, 878)
        ),
        Movie(
            5,
            "Tenet",
            "2020-08-26",
            "/poster2.jpg",
            7.4,
            "Time inversion thriller",
            listOf(28, 878, 53)
        ),
        Movie(
            6,
            "Interstellar",
            "2014-11-07",
            "/poster3.jpg",
            8.6,
            "Space exploration",
            listOf(12, 18, 878)
        )
    )

    MovitoTheme(darkTheme = false) {
        DetailsScreenPreview(
            movie = mockMovie,
            genres = mockGenres,
            recommendedMovies = recommendedMovies,
            isFavorite = false
        )
    }
}

/**
 * Preview: Trailer button in loading state
 * Tests: Loading indicator, disabled button state
 */
@Preview("Loading State", showSystemUi = true)
@Composable
fun DetailsScreenPreview_Loading() {
    val mockMovie = Movie(
        id = 7,
        title = "Avatar",
        releaseDate = "2009-12-18",
        posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        voteAverage = 7.8,
        overview = "A paraplegic marine dispatched to the moon Pandora.",
        genreIds = listOf(28, 12, 878)
    )

    MovitoTheme(darkTheme = true) {
        DetailsScreenPreview(
            movie = mockMovie,
            isLoading = true,
            isFavorite = false
        )
    }
}

/**
 * Preview: Error state for recommendations
 * Tests: Error message display, empty state handling
 */
@Preview("Error State", showSystemUi = true)
@Composable
fun DetailsScreenPreview_Error() {
    val mockMovie = Movie(
        id = 8,
        title = "Dune",
        releaseDate = "2021-10-22",
        posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        voteAverage = 8.0,
        overview = "Feature adaptation of Frank Herbert's science fiction novel.",
        genreIds = listOf(878, 12)
    )

    MovitoTheme(darkTheme = false) {
        DetailsScreenPreview(
            movie = mockMovie,
            error = "Failed to load recommendations: No internet connection",
            isFavorite = true
        )
    }
}