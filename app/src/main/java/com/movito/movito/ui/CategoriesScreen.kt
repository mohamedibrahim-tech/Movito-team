package com.movito.movito.ui

import android.app.Activity
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = viewModel(), snackbarHost: @Composable () -> Unit = {},) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    CategoriesScreenContent(uiState = uiState, snackbarHost = snackbarHost) { genre ->
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", genre.id)
            putExtra("genreName", genre.name)
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        context.startActivity(intent)
        (context as? Activity)?.overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreenContent(
    modifier: Modifier = Modifier,
    uiState: CategoriesUiState,
    //used to remind user to grant the notification permeation
    snackbarHost: @Composable () -> Unit = {},
    onGenreClick: (Genre) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = {
                Image(
                    painter = painterResource(id = R.drawable.movito_logo),
                    contentDescription = stringResource(id = R.string.categories_movito_logo_description),
                    modifier = Modifier.height(28.dp)
                )
            })
        },
        bottomBar = {
            MovitoNavBar(selectedItem = "home")
        },
        snackbarHost = snackbarHost
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
                        text = uiState.error ?: stringResource(id = R.string.categories_unknown_error),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = mapGenreNameToDrawable(genre.name)),
                contentDescription = genre.name, // For accessibility
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
            )
            Text(
                text = genre.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            )
        }
    }
}

@DrawableRes
private fun mapGenreNameToDrawable(genreName: String): Int {
    return when (genreName.lowercase()) {
        "action"         , "حركة"         -> R.drawable.action
        "adventure"      , "مغامرة"       -> R.drawable.adventure
        "animation"      , "رسوم متحركة"  -> R.drawable.animation
        "comedy"         , "كوميديا"      -> R.drawable.comedy
        "crime"          , "جريمة"        -> R.drawable.crime
        "documentary"    , "وثائقي"       -> R.drawable.documentary
        "drama"          , "دراما"        -> R.drawable.drama
        "family"         , "عائلي"        -> R.drawable.family
        "fantasy"        , "فانتازيا"     -> R.drawable.fantasy
        "history"        , "تاريخ"        -> R.drawable.history
        "horror"         , "رعب"          -> R.drawable.horror
        "music"          , "موسيقى"       -> R.drawable.music
        "mystery"        , "غموض"         -> R.drawable.mystery
        "romance"        , "رومنسية"      -> R.drawable.romance
        "science fiction", "خيال علمي"    -> R.drawable.science_fiction
        "tv movie"       , "فيلم تلفازي"  -> R.drawable.tv_movie
        "thriller"       , "إثارة"        -> R.drawable.thriller
        "war"            , "حرب"          -> R.drawable.war
        "western"        , "غربي"         -> R.drawable.western
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
    val mockState = CategoriesUiState(error = stringResource(id = R.string.categories_failed_to_load_genres))
    MovitoTheme(darkTheme = true) {
        CategoriesScreenContent(uiState = mockState, onGenreClick = {})
    }
}
