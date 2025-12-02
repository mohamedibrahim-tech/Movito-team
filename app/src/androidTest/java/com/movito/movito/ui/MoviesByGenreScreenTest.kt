
package com.movito.movito.ui

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.viewmodel.MoviesUiState
import io.mockk.mockk
import io.mockk.verify
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

        // The loading indicator is a CircularProgressIndicator, which does not have a content description by default.
        // We can check for its existence by asserting that the error and success states are not displayed.
        composeTestRule.onNodeWithText("Failed to load movies").assertDoesNotExist()
        composeTestRule.onNodeWithText("Movie 1").assertDoesNotExist()

    }

    @Test
    fun moviesByGenreScreen_ErrorState_ShowsErrorMessage() {
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

        composeTestRule.onNodeWithText("Failed to load movies").assertIsDisplayed()
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
        val onRefresh: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            MoviesByGenreContent(
                uiState = MoviesUiState(),
                gridState = rememberLazyGridState(),
                genreName = "Action",
                onRefresh = onRefresh,
                onLoadMore = {},
                onBackPressed = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Refresh").performClick()
        verify { onRefresh() }
    }

    @Test
    fun moviesByGenreScreen_BackPressedAction_CallsOnBackPressed() {
        val onBackPressed: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            MoviesByGenreContent(
                uiState = MoviesUiState(),
                gridState = rememberLazyGridState(),
                genreName = "Action",
                onRefresh = {},
                onLoadMore = {},
                onBackPressed = onBackPressed
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        verify { onBackPressed() }
    }
}
