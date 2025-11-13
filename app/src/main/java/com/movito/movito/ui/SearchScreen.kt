package com.movito.movito.ui

//  شيلت 'Image' وضفنا 'AsyncImage'
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage //  إضافة import مكتبة Coil  اللي عملناها في الجرادل
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.data.model.Movie
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.viewmodel.SearchViewModel

/**
 * شاشة البحث
 * (تم تعديل الـ BottomBar)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier, viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var active by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SearchBar(
                //  (كل كود الـ SearchBar زي ما هو)
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (active) 0.dp else 16.dp),
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = {
                    active = false // Close the 'active' state when pressing Enter
                    viewModel.searchMovies(it) // Execute the search
                },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search for movies, series...") },
                leadingIcon = {
                    if (active) {
                        // Back button when in 'active' state
                        IconButton(onClick = { active = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        // Default search icon
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        // Clear search field button
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            ) {
                // Content when in 'active' state (you can put search history here)
                // In this example, we place a simple message
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Type the movie or series name to search...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        // شيلنا الـ state بتاع selectedItem واستدعينا الـ NavBar الجديد
        bottomBar = {
            MovitoNavBar(selectedItem = "search")
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

                // Error state
                uiState.error != null -> {
                    Text(
                        text = "Search failed: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                // Message if the result list is empty after a search
                uiState.hasSearched && uiState.movies.isEmpty() -> {
                    Text(
                        text = "No results found for \"${uiState.searchQuery}\"",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                // Welcome message before starting the search
                !uiState.hasSearched && uiState.searchQuery.isEmpty() -> {
                    Text(
                        text = "Welcome! Type a movie or series name to search.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }

                // Display search results
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = uiState.movies, key = { it.id }) { movie ->
                            MovieListItem(movie = movie)
                        }
                    }
                }
            }
        }
    }
}

/**
 * تصميم عنصر واحد في قايمة البحث (ListView).
 */
@Composable
fun MovieListItem(
    modifier: Modifier = Modifier, movie: Movie
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { /* Handle item click */ }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        // Thumbnail poster image
        Card(
            modifier = Modifier.size(width = 80.dp, height = 120.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            //  استخدمنا AsyncImage (Coil)
            // (عشان نجهز للـ API الحقيقي)
            AsyncImage(
                model = movie.posterUrl, // (بقى String)
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(id = R.drawable.poster_test)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        // Movie information
        Column(
            modifier = Modifier.weight(1f) // To make the column take the remaining space
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = movie.year,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " | ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = movie.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Add a short description (optional)
            Text(
                text = "A science fiction and action film revolving around the exploration of distant galaxies.",
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
    val mockViewModel = SearchViewModel()
    MovitoTheme(darkTheme = true) {
        SearchScreen(viewModel = mockViewModel)
    }
}

@Preview(showSystemUi = true, name = "Light Mode")
@Composable
fun SearchPreviewLight() {
    val mockViewModel = SearchViewModel()
    MovitoTheme(darkTheme = false) {
        SearchScreen(viewModel = mockViewModel)
    }
}