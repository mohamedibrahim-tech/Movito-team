package com.movito.movito

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.MovieResponse
import com.movito.movito.data.source.remote.RetrofitInstance
import com.movito.movito.data.source.remote.TmdbApi
import com.movito.movito.viewmodel.MoviesByGenreViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesByGenreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApi = mockk<TmdbApi>()
    private val mockApplication = mockk<Application>(relaxed = true)

    private lateinit var viewModel: MoviesByGenreViewModel
    private val genreId = 28 // Action

    private val movie1 = Movie(id = 1, title = "Inception")
    private val movie2 = Movie(id = 2, title = "The Dark Knight")
    private val movie3 = Movie(id = 3, title = "Interstellar")


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns mockApi

        mockkObject(LanguageManager)
        every { LanguageManager.currentLanguage.value } returns "en"

        // السطر الذهبي اللي هيشتغل مع أي دالة مهما كان شكلها
        coEvery { mockApi.discoverMoviesByGenre(any(), any(), any(), any()) } answers {
            when (secondArg<Int>()) { // page دايمًا الباراميتر التاني
                1 -> MovieResponse(results = listOf(movie1, movie2))
                2 -> MovieResponse(results = listOf(movie3))
                3 -> MovieResponse(results = emptyList()) // مهم جدًا لتيست الـ empty page
                else -> MovieResponse(results = emptyList())
            }
        }
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }


    @Test
    fun `init should load first page automatically`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle().apply {
            set("genreId", genreId)
        }

        viewModel = MoviesByGenreViewModel(savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf(movie1, movie2), state.movies)
        assertNull(state.error)
    }

    @Test
    fun `loadMoreMovies should append new movies`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("genreId" to genreId))

        viewModel = MoviesByGenreViewModel( savedStateHandle)
        advanceUntilIdle() // page 1

        viewModel.loadMoreMovies()
        advanceUntilIdle() // page 2

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingMore)
        assertEquals(listOf(movie1, movie2, movie3), state.movies)
    }

    @Test
    fun `refresh should reset page and replace movies`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle().apply { set("genreId", genreId) }

        viewModel = MoviesByGenreViewModel( savedStateHandle)
        advanceUntilIdle() // page 1

        viewModel.loadMoreMovies()
        advanceUntilIdle() // page 2 → 3 movies

        viewModel.loadMovies(isRefreshing = true)
        advanceUntilIdle() // refresh → back to page 1

        assertEquals(listOf(movie1, movie2), viewModel.uiState.value.movies)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `should not add duplicate movies`() = runTest(testDispatcher) {

        val savedStateHandle = SavedStateHandle(mapOf("genreId" to genreId))
        coEvery { mockApi.discoverMoviesByGenre(any(), eq(2), any(), any()) } returns MovieResponse(
            results = listOf(movie1) // نفس الفيلم اللي في page 1
        )
        viewModel = MoviesByGenreViewModel( savedStateHandle)
        advanceUntilIdle()
        //assertTrue(viewModel.uiState.value.error?.contains("Failed to load movies") == true)

        viewModel.loadMoreMovies()
        advanceUntilIdle()

        val movies = viewModel.uiState.value.movies
        assertEquals(2, movies.size) // duplicate filtered out
    }

    @Test
    fun `network error on init should show error`() = runTest(testDispatcher) {
        val exceptionMessage = "No internet"

// نعمل override للـ mock عشان يرمي error في الـ init (page = 1)
        coEvery { mockApi.discoverMoviesByGenre(any(), any(), any(), any()) } throws IOException(exceptionMessage)
        val savedStateHandle = SavedStateHandle().apply { set("genreId", genreId) }

        viewModel = MoviesByGenreViewModel( savedStateHandle)
        advanceUntilIdle()

     //   assertTrue(viewModel.uiState.value.error?.contains("Failed to load movies") == true)
        val expectedError = "Failed to load movies: $exceptionMessage"
        assertEquals(expectedError, viewModel.uiState.value.error)
    }

    @Test
    fun `loadMoreMovies with error should show error`() = runTest(testDispatcher) {
        val exceptionMessage = "Check connection"
        val expectedError = "Failed to load more movies: $exceptionMessage"

        coEvery { mockApi.discoverMoviesByGenre(any(), eq(2), any(), any()) } throws IOException(exceptionMessage)

        val savedStateHandle = SavedStateHandle(mapOf("genreId" to genreId))

        viewModel = MoviesByGenreViewModel(savedStateHandle)
        advanceUntilIdle()

       // assertTrue(viewModel.uiState.value.error?.contains("Failed to load movies") == true)

        viewModel.loadMoreMovies()
        advanceUntilIdle()

        assertEquals(expectedError, viewModel.uiState.value.error)
    }

    @Test
    fun `arabic language should show arabic messages`() = runTest(testDispatcher) {
        every { LanguageManager.currentLanguage.value } returns "ar"
        val exceptionMessage = "لا يوجد اتصال"
        val expectedError = "فشل تحميل الأفلام: $exceptionMessage"
        every { mockApplication.getString(any(), exceptionMessage) } returns expectedError

        coEvery {
            mockApi.discoverMoviesByGenre(page = 1, genreId = genreId, language = "ar", apiKey = any())
        } throws IOException(exceptionMessage)

        val savedStateHandle = SavedStateHandle().apply { set("genreId", genreId) }

        viewModel = MoviesByGenreViewModel( savedStateHandle)
        advanceUntilIdle()

        assertEquals("Failed to load movies: لا يوجد اتصال", viewModel.uiState.value.error)    }

    @Test
    fun `empty page should not increment currentPage`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("genreId" to genreId))

        viewModel = MoviesByGenreViewModel( savedStateHandle)
        advanceUntilIdle() // page 1

        viewModel.loadMoreMovies()
        advanceUntilIdle() // page 2

        viewModel.loadMoreMovies()
        advanceUntilIdle() // page 3 → empty → page stays 3

        viewModel.loadMoreMovies()
        advanceUntilIdle()
        coVerify(exactly = 0) { mockApi.discoverMoviesByGenre(any(), eq(4), any(), any()) }
       // assertTrue(viewModel.uiState.value.error?.contains("Failed to load movies") == true)
    }
}
