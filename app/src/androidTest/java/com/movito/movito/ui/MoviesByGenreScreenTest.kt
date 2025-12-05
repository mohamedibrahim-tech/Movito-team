
package com.movito.movito.ui

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.viewmodel.MoviesUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoviesByGenreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun moviesByGenreScreen_LoadingState_ShowsLoadingIndicator() {
        composeTestRule.setContent {
            MoviesByGenreContent(
                uiState = MoviesUiState(isLoading = true),
                gridState = rememberLazyGridState(),
                genreName = "Action",
                onRefresh = {},
                onLoadMore = {},
                onBackPressed = {}
            )
        }

        val errorMessage = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.movies_by_genre_failed_to_load)
        composeTestRule.onNodeWithText(errorMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText("Movie 1").assertDoesNotExist()

    }

    @Test
    fun moviesByGenreScreen_ErrorState_ShowsErrorMessage() {
        val errorMessage = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.movies_by_genre_failed_to_load)
        composeTestRule.setContent {
            MoviesByGenreContent(
                uiState = MoviesUiState(error = "Failed to load"),
                gridState = rememberLazyGridState(),
                genreName = "Action",
                onRefresh = {},
                onLoadMore = {},
                onBackPressed = {}
            )
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun moviesByGenreScreen_SuccessState_ShowsMovieList() {
        val movies = listOf(Movie(1, "Movie 1", "2022-01-01", "", 8.0, "", emptyList()))
        composeTestRule.setContent {
            MoviesByGenreContent(
                uiState = MoviesUiState(movies = movies),
                gridState = rememberLazyGridState(),
                genreName = "Action",
                onRefresh = {},
                onLoadMore = {},
                onBackPressed = {}
            )
        }

        composeTestRule.onNodeWithText("Movie 1").assertIsDisplayed()
    }

    @Test
    fun moviesByGenreScreen_RefreshAction_CallsOnRefresh() {
        var refreshClicked = false
        composeTestRule.setContent {
            MoviesByGenreContent(
                uiState = MoviesUiState(),
                gridState = rememberLazyGridState(),
                genreName = "Action",
                onRefresh = { refreshClicked = true },
                onLoadMore = {},
                onBackPressed = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Refresh").performClick()
        assertTrue(refreshClicked)
    }

    @Test
    fun moviesByGenreScreen_BackPressedAction_CallsOnBackPressed() {
        var backPressed = false
        composeTestRule.setContent {
            MoviesByGenreContent(
                uiState = MoviesUiState(),
                gridState = rememberLazyGridState(),
                genreName = "Action",
                onRefresh = {},
                onLoadMore = {},
                onBackPressed = { backPressed = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backPressed)
    }
}
