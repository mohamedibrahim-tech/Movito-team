package com.movito.movito.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme

class DetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemIsDark = isSystemInDarkTheme()
            val movie: Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("movie", Movie::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra("movie")
            }
            MovitoTheme(darkTheme = systemIsDark) {
                //TODO: implement the on Favorite Changed
                DetailsScreen(
                    movie = movie ?: Movie(),
                    initiallyFavorite = intent.getBooleanExtra("isItInFav", false),
                    onFavoriteChanged = { TODO() }) {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MovitoTheme {

    }
}