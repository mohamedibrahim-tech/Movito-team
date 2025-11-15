package com.movito.movito.ui

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.R
import com.movito.movito.data.model.Genre
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.viewmodel.CategoriesUiState
import com.movito.movito.viewmodel.CategoriesViewModel
import kotlin.math.floor

@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    CategoriesScreenContent(uiState = uiState) { genre ->
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", genre.id)
            putExtra("genreName", genre.name)
        }
        context.startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreenContent(
    modifier: Modifier = Modifier,
    uiState: CategoriesUiState,
    onGenreClick: (Genre) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = {
                Image(
                    painter = painterResource(id = R.drawable.movito_logo),
                    contentDescription = "Movito Logo",
                    modifier = Modifier.height(28.dp)
                )
            })
        },
        bottomBar = {
            MovitoNavBar(selectedItem = "home")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "An unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    CategoriesGrid(genres = uiState.genres, onGenreClick = onGenreClick)
                }
            }
        }
    }
}

@Composable
fun CategoriesGrid(genres: List<Genre>, onGenreClick: (Genre) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(genres) { genre ->
            GenreCard(genre = genre, onClick = { onGenreClick(genre) })
        }
    }
}

@Composable
fun GenreCard(genre: Genre, onClick: () -> Unit) {
    val printer = painterResource(id = mapGenreNameToDrawable(genre.name))
    Card(
        modifier = Modifier
            .width(floor(printer.intrinsicSize.width / 4.0f).dp)
            .height(floor(printer.intrinsicSize.height / 4.0f).dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Image(
            painter = printer,
            contentDescription = genre.name, // For accessibility
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@DrawableRes
private fun mapGenreNameToDrawable(genreName: String): Int {
    return when (genreName.lowercase()) {
        "action" -> R.drawable.action
        "adventure" -> R.drawable.adventure
        "animation" -> R.drawable.animation
        "comedy" -> R.drawable.comedy
        "crime" -> R.drawable.crime
        "documentary" -> R.drawable.documentary
        "drama" -> R.drawable.drama
        "family" -> R.drawable.family
        "fantasy" -> R.drawable.fantasy
        "history" -> R.drawable.history
        "horror" -> R.drawable.horror
        "music" -> R.drawable.music
        "mystery" -> R.drawable.mystery
        "romance" -> R.drawable.romance
        "science fiction" -> R.drawable.science_fiction
        "tv movie" -> R.drawable.tv_movie
        "thriller" -> R.drawable.thriller
        "war" -> R.drawable.war
        "western" -> R.drawable.western
        else -> R.drawable.movito_logo // Default image as a fallback
    }
}


// --- Previews ---

@Preview(showSystemUi = true, name = "Dark Mode - Success")
@Composable
fun CategoriesScreenSuccessPreviewDark() {
    val mockGenres = listOf(
        Genre(28, "Action"),
        Genre(12, "Adventure"),
        Genre(16, "Animation"),
        Genre(35, "Comedy")
    )
    val mockState = CategoriesUiState(genres = mockGenres)
    MovitoTheme(darkTheme = true) {
        CategoriesScreenContent(uiState = mockState, onGenreClick = {})
    }
}

@Preview(showSystemUi = true, name = "Light Mode - Success")
@Composable
fun CategoriesScreenSuccessPreviewLight() {
    val mockGenres = listOf(
        Genre(28, "Action"),
        Genre(12, "Adventure"),
        Genre(16, "Animation"),
        Genre(35, "Comedy")
    )
    val mockState = CategoriesUiState(genres = mockGenres)
    MovitoTheme(darkTheme = false) {
        CategoriesScreenContent(uiState = mockState, onGenreClick = {})
    }
}

@Preview(showSystemUi = true, name = "Dark Mode - Loading")
@Composable
fun CategoriesScreenLoadingPreview() {
    val mockState = CategoriesUiState(isLoading = true)
    MovitoTheme(darkTheme = true) {
        CategoriesScreenContent(uiState = mockState, onGenreClick = {})
    }
}

@Preview(showSystemUi = true, name = "Dark Mode - Error")
@Composable
fun CategoriesScreenErrorPreview() {
    val mockState = CategoriesUiState(error = "Failed to load genres")
    MovitoTheme(darkTheme = true) {
        CategoriesScreenContent(uiState = mockState, onGenreClick = {})
    }
}
