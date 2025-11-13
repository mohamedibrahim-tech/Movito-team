package com.movito.movito.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovieCard
import com.movito.movito.viewmodel.HomeViewModel
import com.movito.movito.data.model.Movie

/**
 * شاشة الهوم
 * (تم تعديل الـ BottomBar)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    // أيقونة الـ Refresh
                    IconButton(onClick = {
                        if (!uiState.isRefreshing && !uiState.isLoading) {
                            viewModel.loadMovies(isRefreshing = true)
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
        // شيلت الـ state بتاع selectedItem واستدعينا الـ NavBar الجديد
        bottomBar = {
            MovitoNavBar(selectedItem = "home")
        }
    ) { innerPadding ->

        // محتوى الشاشة (الـ Grid)
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
                        text = "فشل تحميل البيانات: ${uiState.error}",
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
                        items(items = uiState.movies, key = { it.id }) { movie ->
                            //  استدعاء ال card المشترك
                            MovieCard(modifier = Modifier.height(280.dp), movie = movie)
                        }
                    }
                }
            }
        }
    }
}

//  الـ Previews زي ما هي، بس هتستخدم الكارت المشترك

@Preview(showSystemUi = true, name = "Dark Mode")
@Composable
fun HomePreview() {
    val mockViewModel = HomeViewModel()
    //  تعديل: الـ Preview مبقاش بيحتاج loadMovies()
    // (لأن الـ ViewModel اتغير لـ String URL)
    // mockViewModel.loadMovies() // ده هيعمل Crash لو مشغلناش النت

    MovitoTheme(darkTheme = true) {
        HomeScreen(viewModel = mockViewModel)
    }
}

@Preview(showSystemUi = true, name = "Light Mode")
@Composable
fun HomePreviewLight() {
    val mockViewModel = HomeViewModel()

    MovitoTheme(darkTheme = false) {
        HomeScreen(viewModel = mockViewModel)
    }
}

@Preview(name = "Movie Card Preview")
@Composable
fun MovieCardPreview() {
    //  تعديل: استخدمنا رابط صورة حقيقي
    val mockMovie = Movie(1, "Cosmic Echoes", "2025", "2h 15m", "https://image.tmdb.org/t/p/w500/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg")
    MovitoTheme(darkTheme = true) {
        MovieCard(
            modifier = Modifier
                .padding(16.dp)
                .height(280.dp), movie = mockMovie
        )
    }
}