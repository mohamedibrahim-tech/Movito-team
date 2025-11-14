package com.movito.movito.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovieCard
import com.movito.movito.viewmodel.HomeUiState
import com.movito.movito.viewmodel.HomeViewModel

// Stateful Composable: Handles logic and state
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        modifier = modifier,
        uiState = uiState,
        onRefresh = { viewModel.loadMovies(isRefreshing = true) }
    )
}

// Stateless Composable: Only displays UI, perfect for previews
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onRefresh: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.movito_logo),
                        contentDescription = "Movito Logo",
                        modifier = Modifier.height(32.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                actions = {
                    IconButton(onClick = {
                        if (!uiState.isRefreshing && !uiState.isLoading) {
                            onRefresh()
                        }
                    }) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            MovitoNavBar(selectedItem = "home")
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
                    Text(
                        text = "Failed to load movies",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
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
                            items = uiState.movies,
                            key = { it.id },
                            contentType = { "movie" }
                        ) { movie ->
                            MovieCard(modifier = Modifier.height(280.dp), movie = movie)
                        }
                    }
                }
            }
        }
    }
}

// --- Previews ---

@Preview(showSystemUi = true, name = "Dark Mode - Success")
@Composable
fun HomePreviewSuccessDark() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025-03-15", "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg", 8.5, "An epic space opera.", listOf(878))
    val mockState = HomeUiState(movies = List(10) { mockMovie })
    MovitoTheme(darkTheme = true) {
        HomeScreenContent(uiState = mockState, onRefresh = {})
    }
}

@Preview(showSystemUi = true, name = "Light Mode - Success")
@Composable
fun HomePreviewSuccessLight() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025-03-15", "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg", 8.5, "An epic space opera.", listOf(878))
    val mockState = HomeUiState(movies = List(10) { mockMovie })
    MovitoTheme(darkTheme = false) {
        HomeScreenContent(uiState = mockState, onRefresh = {})
    }
}

@Preview(showSystemUi = true, name = "Dark Mode - Loading")
@Composable
fun HomePreviewLoading() {
    val mockState = HomeUiState(isLoading = true)
    MovitoTheme(darkTheme = true) {
        HomeScreenContent(uiState = mockState, onRefresh = {})
    }
}

@Preview(showSystemUi = true, name = "Dark Mode - Error")
@Composable
fun HomePreviewError() {
    val mockState = HomeUiState(error = "Failed to load movies")
    MovitoTheme(darkTheme = true) {
        HomeScreenContent(uiState = mockState, onRefresh = {})
    }
}

@Preview(name = "Movie Card Preview")
@Composable
fun MovieCardPreview() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025-03-15", "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg", 8.5, "An epic space opera.", listOf(878))
    MovitoTheme(darkTheme = true) {
        MovieCard(
            modifier = Modifier
                .padding(16.dp)
                .height(280.dp), movie = mockMovie
        )
    }
}
