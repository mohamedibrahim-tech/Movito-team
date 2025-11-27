package com.movito.movito.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.DetailsViewModel
import com.movito.movito.viewmodel.ThemeViewModel


class DetailsActivity : ComponentActivity() {

    private val viewModel: DetailsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val movie: Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("movie", Movie::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra("movie")
            }
            key(isDarkTheme) {
                MovitoTheme(darkTheme = isDarkTheme) {
                    DetailsScreen(
                        viewModel = viewModel,
                        movie = movie ?: Movie(),
                        onClickBackButton = { onBackPressedDispatcher.onBackPressed() }
                    )
                }
            }

        }
    }
}
