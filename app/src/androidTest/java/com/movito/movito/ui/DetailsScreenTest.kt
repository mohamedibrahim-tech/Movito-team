
package com.movito.movito.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.movito.movito.data.model.Movie
import com.movito.movito.viewmodel.DetailsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// A fake implementation of the DetailsViewModel for testing purposes.
// This avoids issues with mocking on Android instrumented tests.
open class FakeDetailsViewModel : DetailsViewModel() {
    var findTrailerCalledWith: Int? = null
    var prepareShareUrlCalledWith: Int? = null

    override fun findTrailer(movieId: Int) {
        findTrailerCalledWith = movieId
    }

    override fun prepareShareUrl(movieId: Int) {
        prepareShareUrlCalledWith = movieId
    }
}

@RunWith(AndroidJUnit4::class)
class DetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeDetailsViewModel

    private val mockMovie = Movie(
        id = 1,
        title = "Test Movie",
        releaseDate = "2023-01-01",
        posterPath = "",
        voteAverage = 8.0,
        overview = "This is a test movie overview.",
        genreIds = emptyList()
    )

    @Before
    fun setUp() {
        fakeViewModel = FakeDetailsViewModel()
    }

    @Test
    fun detailsScreen_InitialState_CorrectElementsDisplayed() {
        composeTestRule.setContent {
            DetailsScreen(
                viewModel = fakeViewModel,
                movie = mockMovie,
                onClickBackButton = {}
            )
        }

        composeTestRule.onNodeWithText("Test Movie").assertIsDisplayed()
        composeTestRule.onNodeWithText("This is a test movie overview.").assertIsDisplayed()
    }

    @Test
    fun detailsScreen_PlayTrailerButton_CallsViewModel() {
        composeTestRule.setContent {
            DetailsScreen(
                viewModel = fakeViewModel,
                movie = mockMovie,
                onClickBackButton = { }
            )
        }

        composeTestRule.onNodeWithText(text = "Play Trailer", useUnmergedTree = true).performClick()
        assertEquals(mockMovie.id, fakeViewModel.findTrailerCalledWith)
    }

    @Test
    fun detailsScreen_ShareButton_CallsViewModel() {
        composeTestRule.setContent {
            DetailsScreen(
                viewModel = fakeViewModel,
                movie = mockMovie,
                onClickBackButton = { }
            )
        }

        composeTestRule.onNodeWithText("Share").performClick()
        assertEquals(mockMovie.id, fakeViewModel.prepareShareUrlCalledWith)
    }

    @Test
    fun detailsScreen_AddToFavorites_ShowsAddDialog() {
        composeTestRule.setContent {
            DetailsScreen(
                viewModel = fakeViewModel,
                movie = mockMovie,
                onClickBackButton = { }
            )
        }

        // Find the favorite button by its content description when it's not a favorite yet
        composeTestRule.onNodeWithContentDescription("Add to favorites").performClick()

        // Verify that the add dialog is shown
        composeTestRule.onNodeWithText("Add to Favorites?").assertIsDisplayed()
    }

}
