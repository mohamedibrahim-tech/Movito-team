package com.movito.movito.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.viewmodel.SearchViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier, viewModel: SearchViewModel = viewModel()
) {
    // 1. COLLECT THE STATE
    // The uiState contains the latest searchQuery.
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var active by remember { mutableStateOf(false) }
    val context = LocalContext.current


    // Show a snackbar when an error occurs
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            // viewModel.errorShown() // Notify ViewModel that the error has been shown
        }
    }



    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SearchBar(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (active) 0.dp else 16.dp),

                query = uiState.searchQuery,

                // 3. EVENT INPUT: Call the ViewModel function every time the text changes.
                onQueryChange = { newText ->
                    // This is the critical line!
                    viewModel.updateSearchQuery(newText)
                },
                onSearch = {
                    active = false
                    viewModel.searchMovies()
                },

                active = active,
                onActiveChange = { active = it },
                placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
                leadingIcon = {
                    if (active) {
                        IconButton(onClick = { active = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.search_back_description))
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.search_clear_description))
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            ) {
                // This is the content shown when the search bar is active (for suggestions).
                // We will display the same movie list here as suggestions.
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = uiState.movies, key = { it.id }) { movie ->
                        MovieListItem(
                            movie = movie,
                            onClick = {
                                active = false // Close the active search view
                                viewModel.updateSearchQuery(movie.title) // Update the text field
                                viewModel.searchMovies() // Trigger the search


                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            MovitoNavBar(selectedItem = "search")
        },
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
                        text = stringResource(id = R.string.search_failed, uiState.error!!),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                // If a search was performed and no movies were found
                uiState.hasSearched && uiState.movies.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.search_no_results, uiState.searchQuery),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                // Initial state before any search is performed
                !uiState.hasSearched -> {
                    Text(
                        text = stringResource(id = R.string.search_initial_message),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }

                // Default case: display the list of movies from the search result
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = uiState.movies, key = { it.id }) { movie ->
                            MovieListItem(
                                movie = movie,
                                onClick = {

                                    active = false // Close the active search view
                                    viewModel.updateSearchQuery(movie.title) // Update the text field
                                    viewModel.searchMovies() // Trigger the search
                                    navigateToActivity(
                                        context = context,
                                        movie
                                    )

                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun navigateToActivity(context: Context, movie: Movie) {
    val intent = Intent(context, DetailsActivity::class.java)
    intent.putExtra("movie", movie)
    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    context.startActivity(intent)

    /*     أنيميشن التنقل
         بيلغي الوميض (flicker) اللي بيحصل بين الـ Activities*/
    if (context is Activity) {
        context.overridePendingTransition(0, 0)
    }
}


@Composable
fun MovieListItem(
    modifier: Modifier = Modifier,
    movie: Movie,
    onClick: () -> Unit // Hoist the click event

) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically) {
        Card(
            modifier = Modifier.size(width = 80.dp, height = 120.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(id = R.drawable.poster_test)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Add a check to prevent crashing if releaseDate is too short
            if (movie.releaseDate.length >= 4) {
                Row {
                    Text(
                        text = movie.releaseDate.take(4),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview(showSystemUi = true, name = "Dark Mode")
@Composable
fun SearchPreview() {
    MovitoTheme(darkTheme = true) {
        SearchScreen()
    }
}

@Preview(showSystemUi = true, name = "Light Mode")
@Composable
fun SearchPreviewLight() {
    MovitoTheme(darkTheme = false) {
        SearchScreen()
    }
}
