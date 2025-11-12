package com.movito.movito.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movito.movito.Movie
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    favoriteMovies: List<Movie> = emptyList(),
    onRemoveFavorite: (Int) -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        bottomBar = {
            var selectedItem by remember { mutableStateOf("favorite") }
            MovitoNavBar(selectedItem) { selectedItem = it}
        }
    ) { innerPadding ->


        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            if (favoriteMovies.isEmpty()) {

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
            } else {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = favoriteMovies,
                        key = { it.id }
                    ) { movie ->
                        FavoriteMovieCard(
                            movie = movie,
                            onRemoveFavorite = { onRemoveFavorite(movie.id) }
                        )
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
) = MovieCard(modifier = modifier.height(280.dp), movie = movie) {
    IconButton(
        onClick = onRemoveFavorite,
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


@Preview(showSystemUi = true, name = "Favorites - Empty State")
@Composable
fun FavoritesEmptyPreview() {
    MovitoTheme(darkTheme = true) {
        FavoritesScreen(
            favoriteMovies = emptyList()
        )
    }
}

@Preview(showSystemUi = true, name = "Favorites - With Movies")
@Composable
fun FavoritesWithMoviesPreview() {
    val mockMovies = listOf(
        Movie(1, "Cosmic Echoes", "2025", "2h 15m", R.drawable.poster_test),
        Movie(2, "Time Warp", "2024", "1h 50m", R.drawable.poster_test),
        Movie(3, "Space Journey", "2025", "2h 30m", R.drawable.poster_test),
        Movie(4, "Dark Universe", "2024", "2h 5m", R.drawable.poster_test)
    )

    MovitoTheme(darkTheme = true) {
        FavoritesScreen(
            favoriteMovies = mockMovies,
            onRemoveFavorite = { /* remove codeً */ }
        )
    }
}

@Preview(name = "Favorite Movie Card Preview")
@Composable
fun FavoriteMovieCardPreview() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025", "2h 15m", R.drawable.poster_test)
    MovitoTheme(darkTheme = true) {
        FavoriteMovieCard(
            modifier = Modifier.padding(16.dp),
            movie = mockMovie,
            onRemoveFavorite = { /* remove code */ }
        )
    }
}