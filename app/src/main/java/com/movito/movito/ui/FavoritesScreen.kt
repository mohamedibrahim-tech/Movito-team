package com.movito.movito.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovieCard
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.viewmodel.FavoritesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
   // viewModel: FavoritesViewModel = viewModel() // ViewModel للـ Firebase
    viewModel: FavoritesViewModel = remember { FavoritesViewModel.getInstance() }

) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorites",
                        fontSize = 28.sp,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        bottomBar = {
            MovitoNavBar(selectedItem = "favorite")
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.favorites.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.favorites.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "There are no favourites",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your favorites to see them here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = uiState.favorites,
                            key = { it.id },
                            contentType = { "movie" }
                        ) { favoriteMovie ->
                            FavoriteMovieCard(
                                movie = favoriteMovie,
                                onRemoveFavorite = { viewModel.removeFromFavorites(favoriteMovie.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteMovieCard(
    modifier: Modifier = Modifier,
    movie: Movie,
    onRemoveFavorite: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    if (showRemoveDialog) {
        RemoveFromFavoritesDialog(
            movieTitle = movie.title,
            onConfirm = onRemoveFavorite,
            onDismiss = { showRemoveDialog = false }
        )
    }

    MovieCard(
        modifier = modifier.height(280.dp),
        movie = movie,
    ) {
        IconButton(
            onClick = { showRemoveDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Remove from favorites",
                tint = Color.Red,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


// Dark Mode Preview - Empty
@Preview(showSystemUi = true, name = "Favorites - Empty Dark Mode")
@Composable
fun FavoritesEmptyPreviewDark() {
    MovitoTheme(darkTheme = true) {
        FavoritesScreenPreview(
            favorites = emptyList(),
            isLoading = false,
            error = null
        )
    }
}

// Light Mode Preview - Empty
@Preview(showSystemUi = true, name = "Favorites - Empty Light Mode")
@Composable
fun FavoritesEmptyPreviewLight() {
    MovitoTheme(darkTheme = false) {
        FavoritesScreenPreview(
            favorites = emptyList(),
            isLoading = false,
            error = null
        )
    }
}

// Dark Mode Preview - With Movies
@Preview(showSystemUi = true, name = "Favorites - With Movies Dark Mode")
@Composable
fun FavoritesWithMoviesPreviewDark() {
    val mockFavorites = listOf(
        Movie(
            id = 1,
            title = "Cosmic Echoes",
            releaseDate = "2025-03-15",
            posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
            voteAverage = 8.5,
            overview = "An epic space opera.",
        ),
        Movie(
            id = 1,
            title = "Time Warp",
            releaseDate = "2024-07-22",
            posterPath = "/d5NXSklXo0qyIYkgV94XAgMIckC.jpg",
            voteAverage = 7.8,
            overview = "A thrilling time travel adventure.",
        ),

    )

    MovitoTheme(darkTheme = true) {
        FavoritesScreenPreview(
            favorites = mockFavorites,
            isLoading = false,
            error = null
        )
    }
}

// Light Mode Preview - With Movies
@Preview(showSystemUi = true, name = "Favorites - With Movies Light Mode")
@Composable
fun FavoritesWithMoviesPreviewLight() {
    val mockFavorites = listOf(
        Movie(
            id = 1,
            title = "Cosmic Echoes",
            releaseDate = "2025-03-15",
            posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
            voteAverage = 8.5,
            overview = "An epic space opera.",
        ),
        Movie(
            id = 1,
            title = "Time Warp",
            releaseDate = "2024-07-22",
            posterPath = "/d5NXSklXo0qyIYkgV94XAgMIckC.jpg",
            voteAverage = 7.8,
            overview = "A thrilling time travel adventure.",
        ),
    )

    MovitoTheme(darkTheme = false) {
        FavoritesScreenPreview(
            favorites = mockFavorites,
            isLoading = false,
            error = null
        )
    }
}

// Loading Preview
@Preview(showSystemUi = true, name = "Favorites - Loading")
@Composable
fun FavoritesLoadingPreview() {
    MovitoTheme(darkTheme = true) {
        FavoritesScreenPreview(
            favorites = emptyList(),
            isLoading = true,
            error = null
        )
    }
}

// Error Preview
@Preview(showSystemUi = true, name = "Favorites - Error")
@Composable
fun FavoritesErrorPreview() {
    MovitoTheme(darkTheme = true) {
        FavoritesScreenPreview(
            favorites = emptyList(),
            isLoading = false,
            error = "Failed to load favorites"
        )
    }
}

// Favorite Movie Card Preview - Dark
@Preview(name = "Favorite Movie Card Dark")
@Composable
fun FavoriteMovieCardPreviewDark() {
    val mockMovie = Movie(
        id = 1,
        title = "Cosmic Echoes",
        releaseDate = "2025-03-15",
        posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        voteAverage = 8.5,
        overview = "An epic space opera.",
        genreIds = listOf(878)
    )

    MovitoTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            // Mock ViewModel
            FavoriteMovieCardPreviewContent(movie = mockMovie)
        }
    }
}

// Favorite Movie Card Preview - Light
@Preview(name = "Favorite Movie Card Light")
@Composable
fun FavoriteMovieCardPreviewLight() {
    val mockMovie = Movie(
        id = 1,
        title = "Cosmic Echoes",
        releaseDate = "2025-03-15",
        posterPath = "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg",
        voteAverage = 8.5,
        overview = "An epic space opera.",
        genreIds = listOf(878)
    )

    MovitoTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            FavoriteMovieCardPreviewContent(movie = mockMovie)
        }
    }
}

// Helper Composables للـ Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenPreview(
    favorites: List<Movie>,
    isLoading: Boolean,
    error: String?
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorites",
                        fontSize = 28.sp,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        bottomBar = {
            MovitoNavBar(selectedItem = "favorite")
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                favorites.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "There are no favourites",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your favorites to see them here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = favorites,
                            key = { it.id },
                            contentType = { "movie" }
                        ) { favoriteMovie ->
                            val movie = Movie(
                                id = favoriteMovie.id,
                                title = favoriteMovie.title,
                                releaseDate = favoriteMovie.releaseDate,
                                posterPath = favoriteMovie.posterPath,
                                voteAverage = favoriteMovie.voteAverage,
                                overview = favoriteMovie.overview
                            )

                            FavoriteMovieCardPreviewContent(movie = movie)
                        }
                    }
                }
            }
        }
    }
}


//Preview version للـ Card بدون ViewModel

@Composable
private fun FavoriteMovieCardPreviewContent(movie: Movie) {
    MovieCard(
        modifier = Modifier.height(280.dp),
        movie = movie,
    ) {
        IconButton(
            onClick = { /* Preview - no action */ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Remove from favorites",
                tint = Color.Red,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}