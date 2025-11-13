package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movito.movito.data.model.Movie
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val movies: List<Movie> = emptyList(), // ده بيستخدم Movie data class
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val mockMovies = listOf(
        Movie(1, "Dune: Part One", "2021", "2h 35m", "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg"),
        Movie(2, "Dune: Part Two", "2024", "2h 46m", "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05ESUPt.jpg"),
        Movie(3, "Cosmic Echoes", "2023", "2h 05m", "https://image.tmdb.org/t/p/w500/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg"),
        Movie(4, "Mountain Peak", "2025", "2h 20m", "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg"),
        Movie(5, "Desert Runner", "2024", "1h 45m", "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05ESUPt.jpg"),
        Movie(6, "Space Pirates", "2023", "2h 00m", "https://image.tmdb.org/t/p/w500/qA9b2xSJ8nCK2z3yIuVnAwmWsum.jpg"),
        Movie(7, "Time Bender", "2025", "2h 30m", "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg"),
        Movie(8, "Alpha Centauri", "2024", "2h 10m", "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05ESUPt.jpg")
    )


    init {
        loadMovies(isLoading = true)
    }

    fun loadMovies(isLoading: Boolean = false, isRefreshing: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    error = null
                )
            }

            delay(2000)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    movies = mockMovies
                )
            }
        }
    }
}