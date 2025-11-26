package com.movito.movito.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovieCard
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.ui.navigation.Screen
import com.movito.movito.viewmodel.MoviesUiState
import com.movito.movito.viewmodel.MoviesByGenreViewModel
import kotlinx.coroutines.launch

@Composable
fun MoviesByGenreScreen(
    navController: NavController,
    genreName: String,
    viewModel: MoviesByGenreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    MoviesByGenreContent(
        uiState = uiState,
        gridState = gridState,
        genreName = genreName,
        onRefresh = {
            viewModel.loadMovies(isRefreshing = true)
            coroutineScope.launch {
                gridState.animateScrollToItem(0)
            }
        },
        onLoadMore = { viewModel.loadMoreMovies() },
        onMovieClick = { movieId ->
            navController.navigate(Screen.Details.createRoute(movieId))
        },
        onBackPressed = { navController.popBackStack() },
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesByGenreContent(
    uiState: MoviesUiState,
    gridState: LazyGridState,
    genreName: String,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onBackPressed: () -> Unit,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(genreName) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
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
            MovitoNavBar(navController = navController, selectedItem = "") // No item is selected
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.movies.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null && uiState.movies.isEmpty() -> {
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
                        state = gridState,
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
                            MovieCard(
                                modifier = Modifier.height(280.dp),
                                movie = movie,
                                onClick = { onMovieClick(movie.id) }
                            )
                        }

                        if (uiState.isLoadingMore) {
                            item(span = { GridItemSpan(2) }) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
                    }

                    val buffer = 4 // Start loading when 4 items are left
                    val shouldLoadMore by remember {
                        derivedStateOf { // to detect when the list is scrolled to the end
                            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 //current index of the last visible item
                            val totalItemCount = gridState.layoutInfo.totalItemsCount
                            lastVisibleItemIndex >= totalItemCount - 1 - buffer
                        }
                    }

                    LaunchedEffect(shouldLoadMore) {
                        if (shouldLoadMore && !uiState.isLoadingMore && !uiState.isRefreshing) {
                            onLoadMore()
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
fun MoviesByGenrePreviewSuccessDark() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025-03-15", "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg", 8.5, "An epic space opera.", listOf(878))
    val mockState = MoviesUiState(movies = List(10) { mockMovie })
    MovitoTheme(darkTheme = true) {
        MoviesByGenreContent(
            uiState = mockState,
            gridState = rememberLazyGridState(),
            genreName = "Action",
            onRefresh = {},
            onLoadMore = {},
            onMovieClick = {},
            onBackPressed = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showSystemUi = true, name = "Light Mode - Success")
@Composable
fun MoviesByGenrePreviewSuccessLight() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025-03-15", "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg", 8.5, "An epic space opera.", listOf(878))
    val mockState = MoviesUiState(movies = List(10) { mockMovie })
    MovitoTheme(darkTheme = false) {
        MoviesByGenreContent(
            uiState = mockState,
            gridState = rememberLazyGridState(),
            genreName = "Action",
            onRefresh = {},
            onLoadMore = {},
            onMovieClick = {},
            onBackPressed = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showSystemUi = true, name = "Dark Mode - Loading")
@Composable
fun MoviesByGenrePreviewLoading() {
    val mockState = MoviesUiState(isLoading = true)
    MovitoTheme(darkTheme = true) {
        MoviesByGenreContent(
            uiState = mockState,
            gridState = rememberLazyGridState(),
            genreName = "Action",
            onRefresh = {},
            onLoadMore = {},
            onMovieClick = {},
            onBackPressed = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showSystemUi = true, name = "Dark Mode - Error")
@Composable
fun MoviesByGenrePreviewError() {
    val mockState = MoviesUiState(error = "Failed to load movies")
    MovitoTheme(darkTheme = true) {
        MoviesByGenreContent(
            uiState = mockState,
            gridState = rememberLazyGridState(),
            genreName = "Action",
            onRefresh = {},
            onLoadMore = {},
            onMovieClick = {},
            onBackPressed = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showSystemUi = true, name = "Dark Mode - Loading More")
@Composable
fun MoviesByGenrePreviewLoadingMore() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025-03-15", "/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg", 8.5, "An epic space opera.", listOf(878))
    val mockState = MoviesUiState(movies = List(10) { mockMovie }, isLoadingMore = true)
    MovitoTheme(darkTheme = true) {
        MoviesByGenreContent(
            uiState = mockState,
            gridState = rememberLazyGridState(),
            genreName = "Action",
            onRefresh = {},
            onLoadMore = {},
            onMovieClick = {},
            onBackPressed = {},
            navController = rememberNavController()
        )
    }
}
