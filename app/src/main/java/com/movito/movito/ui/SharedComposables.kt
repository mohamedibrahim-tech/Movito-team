package com.movito.movito.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movito.movito.Movie
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme

@Composable
fun MovitoNavBar(selectedItem: String, onItemSelected: (String) -> Unit) =
    BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
        NavigationBarItem(
            selected = selectedItem == "home",
            onClick = { onItemSelected("home") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == "search",
            onClick = { onItemSelected("search") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == "favorite",
            onClick = { onItemSelected("favorite") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.like),
                    contentDescription = "Favorite",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == "profile",
            onClick = { onItemSelected("profile") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
    }

@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    movie: Movie,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Card(
        modifier = modifier,
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
                // أي حاجة انت ضفتها من برة باستخدام اللامدا
                content()
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

@Preview(name = "Movito Nav Bar Preview")
@Composable
fun MovitoNavBarPreview() {
    MovitoTheme(darkTheme = true) {
       MovitoNavBar("home") { }
    }
}

@Preview(name = "280 dp height Movie Card Preview")
@Composable
fun GeneralMovieCardPreview() {
    val mockMovie = Movie(1, "Cosmic Echoes", "2025", "2h 15m", R.drawable.poster_test)
    MovitoTheme(darkTheme = true) {
        MovieCard(modifier = Modifier
            .padding(16.dp)
            .height(280.dp), movie = mockMovie)
    }
}