package com.movito.movito.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.movito.movito.ui.CategoriesScreen
import com.movito.movito.ui.DetailsScreen
import com.movito.movito.ui.FavoritesScreen
import com.movito.movito.ui.ForgotPasswordScreen
import com.movito.movito.ui.MoviesByGenreScreen
import com.movito.movito.ui.SearchScreen
import com.movito.movito.ui.SettingsScreen
import com.movito.movito.ui.SignInScreen
import com.movito.movito.ui.SignUpScreen
import com.movito.movito.viewmodel.DetailsViewModel
import com.movito.movito.viewmodel.MoviesByGenreViewModel

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Categories.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onSignUpClicked = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClicked = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Categories.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onSignInClicked = { navController.navigate(Screen.SignIn.route) }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onPasswordResetSent = { navController.popBackStack() }
            )
        }
        composable(Screen.Categories.route) {
            CategoriesScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController = navController)
        }
        composable(
            route = Screen.MoviesByGenre.route,
            arguments = listOf(
                navArgument("genreId") { type = NavType.IntType },
                navArgument("genreName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getInt("genreId") ?: -1
            val genreName = backStackEntry.arguments?.getString("genreName") ?: "Movies"
            val viewModel: MoviesByGenreViewModel = viewModel(
                key = genreId.toString(),
                factory = MoviesByGenreViewModel.Factory(genreId)
            )
            MoviesByGenreScreen(
                navController = navController,
                genreName = genreName,
                viewModel = viewModel
            )
        }
        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: -1
            val viewModel: DetailsViewModel = viewModel(
                key = movieId.toString(),
                factory = DetailsViewModel.Factory(movieId)
            )
            DetailsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
