package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.MoviesByGenreViewModel

class MoviesByGenreActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val genreId = intent.getIntExtra("genreId", -1)//-1 to handle errors from the intent
        val genreName = intent.getStringExtra("genreName") ?: "Movies"

        enableEdgeToEdge()
        setContent {
            MovitoTheme {
                val viewModel: MoviesByGenreViewModel = viewModel(key = genreId.toString()) {
                    MoviesByGenreViewModel(savedStateHandle = androidx.lifecycle.SavedStateHandle(mapOf("genreId" to genreId)))
                }
                MoviesByGenreScreen(
                    viewModel = viewModel,
                    genreName = genreName,
                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}