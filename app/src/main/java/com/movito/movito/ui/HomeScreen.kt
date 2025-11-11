package com.movito.movito.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.HomeViewModel
import com.movito.movito.Movie
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme

/**
 * مهممممممم جداً
 *home screen function
 *TopBar, BottomBar, Grid
 * وبتاخد الـ state بتاعها من الـ HomeViewModel.
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
        // الشريط العلوي (يحتوي على اللوجو وزرار الـ Refresh)
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.movito_logo),
                        contentDescription = "Movito Logo",
                        modifier = Modifier.height(32.dp) // تحديد ارتفاع اللوجو
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
        // Navigation bar
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                var selectedItem by remember { mutableStateOf("home") }
                NavigationBarItem(
                    selected = selectedItem == "home", onClick = { selectedItem = "home" },
                    icon = { Icon(painter = painterResource(id = R.drawable.home), contentDescription = "Home", modifier = Modifier.size(24.dp)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedItem == "search", onClick = { selectedItem = "search" },
                    icon = { Icon(painter = painterResource(id = R.drawable.search), contentDescription = "Search", modifier = Modifier.size(24.dp)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedItem == "favorite", onClick = { selectedItem = "favorite" },
                    icon = { Icon(painter = painterResource(id = R.drawable.like), contentDescription = "Favorite", modifier = Modifier.size(24.dp)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedItem == "profile", onClick = { selectedItem = "profile" },
                    icon = { Icon(painter = painterResource(id = R.drawable.user), contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->

        // محتوى الشاشة (الـ Grid)
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                // في حالة التحميل لأول مرة
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // في حالة وجود خطأ
                uiState.error != null -> {
                    Text(
                        text = "فشل تحميل البيانات: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }

                // في حالة النجاح (عرض شبكة الأفلام)
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items = uiState.movies, key = { it.id }) { movie ->
                            MovieCard(movie = movie)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    movie: Movie
) {
    Card(
        modifier = modifier.height(280.dp), // ده الارتفاع اللي أنت عدلته
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // صورة البوستر
            Image(
                painter = painterResource(id = movie.posterUrl),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // التدرج اللوني الأسود الشفاف
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 400f
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                //  (الاسم والسنة والوقت)
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = movie.year,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = " | ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = movie.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, name = "Dark Mode")
@Composable
fun HomePreview() {
    val mockViewModel = HomeViewModel()
    mockViewModel.loadMovies()

    MovitoTheme(darkTheme = true) {
        HomeScreen(viewModel = mockViewModel)
    }
}

@Preview(showSystemUi = true, name = "Light Mode")
@Composable
fun HomePreviewLight() {
    val mockViewModel = HomeViewModel()
    mockViewModel.loadMovies()

    MovitoTheme(darkTheme = false) {
        HomeScreen(viewModel = mockViewModel)
    }
}

@Preview(name = "Movie Card Preview")
@Composable
fun MovieCardPreview() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025", "2h 15m", R.drawable.poster_test)
    MovitoTheme(darkTheme = true) {
        MovieCard(modifier = Modifier.padding(16.dp), movie = mockMovie)
    }
}
