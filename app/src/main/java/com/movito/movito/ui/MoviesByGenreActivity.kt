package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.MoviesByGenreViewModel
import com.movito.movito.viewmodel.ThemeViewModel
import androidx.compose.runtime.key

class MoviesByGenreActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val genreId = intent.getIntExtra("genreId", -1)//-1 to handle errors from the intent
        val genreName = intent.getStringExtra("genreName") ?: "Movies"

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    val viewModel: MoviesByGenreViewModel = viewModel(key = genreId.toString()) {
                        MoviesByGenreViewModel(
                            savedStateHandle = androidx.lifecycle.SavedStateHandle(
                                mapOf("genreId" to genreId)
                            )
                        )
                    }
                    MoviesByGenreScreen(
                        viewModel = viewModel,
                        genreName = genreName,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}