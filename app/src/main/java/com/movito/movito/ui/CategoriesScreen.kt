package com.movito.movito.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.SouthAmerica
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.R
import com.movito.movito.data.model.Genre
import com.movito.movito.theme.HeartColor
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.viewmodel.CategoriesUiState
import com.movito.movito.viewmodel.CategoriesViewModel

@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    CategoriesScreenContent(uiState = uiState) { genre ->
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
        columns = GridCells.Fixed(3),
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
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = mapGenreNameToIcon(genre.name),
                contentDescription = genre.name,
                tint = HeartColor,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = genre.name,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun mapGenreNameToIcon(genreName: String): ImageVector {
    return when (genreName.lowercase()) {
        "action" -> Icons.Filled.Bolt
        "adventure" -> Icons.Filled.TravelExplore
        "animation" -> Icons.Filled.Animation
        "comedy" -> Icons.Filled.TheaterComedy
        "crime" -> Icons.Filled.Gavel
        "documentary" -> Icons.Filled.DocumentScanner
        "drama" -> Icons.Filled.TheaterComedy
        "family" -> Icons.Filled.FamilyRestroom
        "fantasy" -> Icons.Filled.AutoFixHigh
        "history" -> Icons.Filled.HistoryEdu
        "horror" -> Icons.Filled.Coronavirus
        "music" -> Icons.Filled.MusicNote
        "mystery" -> Icons.Filled.QuestionMark
        "romance" -> Icons.Filled.Favorite
        "science fiction" -> Icons.Filled.Science
        "tv movie" -> Icons.Filled.LiveTv
        "thriller" -> Icons.Filled.Bolt
        "war" -> Icons.Filled.MilitaryTech
        "western" -> Icons.Filled.SouthAmerica
        else -> Icons.Filled.Movie
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
        Box(modifier = Modifier.background(Color.White)) {
            CategoriesScreenContent(uiState = mockState, onGenreClick = {})
        }
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
