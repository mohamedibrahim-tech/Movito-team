package com.movito.movito.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.DetailsViewModel
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.favorites.FavoritesViewModel
import kotlinx.coroutines.launch

class DetailsActivity : ComponentActivity() {

    private val viewModel: DetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemIsDark = isSystemInDarkTheme()
            val movie: Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("movie", Movie::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra("movie")
            }
// Favorites ViewModel
            val favoritesViewModel: FavoritesViewModel = viewModel()

            // State للـ Favorite
            var isFavorite by remember { mutableStateOf(false) }
            var showAddDialog by remember { mutableStateOf(false) }
            var showRemoveDialog by remember { mutableStateOf(false) }

            val scope = rememberCoroutineScope()

            LaunchedEffect(movie?.id) {
                movie?.let {
                    isFavorite = favoritesViewModel.isFavorite(it.id)
                }
            }

            if (showAddDialog && movie != null) {
                AddToFavoritesDialog(
                    movieTitle = movie.title,
                    onConfirm = {
                        scope.launch {
                            favoritesViewModel.addToFavorites(movie)
                            isFavorite = true
                        }
                    },
                    onDismiss = { showAddDialog = false }
                )
            }

            if (showRemoveDialog && movie != null) {
                RemoveFromFavoritesDialog(
                    movieTitle = movie.title,
                    onConfirm = {
                        scope.launch {
                            favoritesViewModel.removeFromFavorites(movie.id)
                            isFavorite = false
                        }
                    },
                    onDismiss = { showRemoveDialog = false }
                )
            }
            MovitoTheme(darkTheme = systemIsDark) {
                //TODO: implement the on Favorite Changed
                DetailsScreen(
                    viewModel = viewModel,
                    movie = movie ?: Movie(),
                    initiallyFavorite = isFavorite,
                    onFavoriteChanged = { newState ->
                        if (newState) {
                            showAddDialog = true
                        } else {
                            showRemoveDialog = true
                        }
                    }
                ) {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MovitoTheme {

    }
}