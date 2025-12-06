
package com.movito.movito.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.viewmodel.SearchUiState
import com.movito.movito.viewmodel.SearchViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel: SearchViewModel = mockk(relaxed = true)
    private val uiState = MutableStateFlow(SearchUiState())

    @Before
    fun setUp() {
        every { mockViewModel.uiState } returns uiState
    }

    @Test
    fun searchScreen_InitialState_ShowsInitialMessage() {
        var initialMessage = ""
        composeTestRule.setContent {
            initialMessage = stringResource(id = R.string.search_initial_message)
            SearchScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText(initialMessage).assertIsDisplayed()
    }

    @Test
    fun searchScreen_LoadingState_ShowsLoadingIndicator() {
        var initialMessage = ""
        composeTestRule.setContent {
            initialMessage = stringResource(id = R.string.search_initial_message)
            SearchScreen(viewModel = mockViewModel)
        }
        uiState.value = SearchUiState(isLoading = true)

        // Assert that the loading indicator is shown by checking that the initial message is gone.
        composeTestRule.onNodeWithText(initialMessage).assertDoesNotExist()
    }

    @Test
    fun searchScreen_ErrorState_ShowsErrorMessage() {
        val errorMessage = "Something went wrong"
        var fullErrorMessage = ""
        composeTestRule.setContent {
            fullErrorMessage = stringResource(id = R.string.search_failed, errorMessage)
            SearchScreen(viewModel = mockViewModel)
        }
        uiState.value = SearchUiState(error = errorMessage)
        composeTestRule.onNodeWithText(fullErrorMessage).assertIsDisplayed()
    }
    @Test
    fun searchScreen_NoResultsState_ShowsNoResultsMessage() {
        val query = "nonexistent"
        var noResultsMessage = ""
        composeTestRule.setContent {
            noResultsMessage = stringResource(id = R.string.search_no_results, query)
            SearchScreen(viewModel = mockViewModel)
        }
        uiState.value = SearchUiState(hasSearched = true, movies = emptyList(), searchQuery = query)

        composeTestRule.onNodeWithText(noResultsMessage).assertIsDisplayed()
    }

    @Test
    fun searchScreen_SuccessState_ShowsMovieList() {
        val movies = listOf(Movie(1, "Movie 1", "2022-01-01", "", 8.0, "", emptyList()))
        uiState.value = SearchUiState(hasSearched = true, movies = movies)

        composeTestRule.setContent {
            SearchScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("Movie 1").assertIsDisplayed()
    }
}
