package com.movito.movito.ui.navigation

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object Categories : Screen("categories")
    object Details : Screen("details/{movieId}") {
        fun createRoute(movieId: Int) = "details/$movieId"
    }
    object MoviesByGenre : Screen("movies_by_genre/{genreId}/{genreName}") {
        fun createRoute(genreId: Int, genreName: String) = "movies_by_genre/$genreId/$genreName"
    }
    object Settings : Screen("settings")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
}
