package com.movito.movito


import com.movito.movito.LanguageManager
import com.movito.movito.data.model.Genre
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.GenreResponse
import com.movito.movito.data.source.remote.MovieResponse

import com.movito.movito.data.source.remote.RetrofitInstance
import com.movito.movito.data.source.remote.TmdbApi
import com.movito.movito.data.source.remote.Video
import com.movito.movito.data.source.remote.VideoResponse
import com.movito.movito.viewmodel.DetailsViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*



@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApi: TmdbApi = mockk()

    private lateinit var viewModel: DetailsViewModel

    private val fakeGenres = listOf(Genre(id = 28, name = "Action"))
    private val fakeMovies = listOf(Movie(id = 1, title = "Test Movie"))
    private val fakeTrailerKey = "abc123"
    private val fakeTrailerUrl = "https://www.youtube.com/watch?v=$fakeTrailerKey"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock RetrofitInstance
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns mockApi

        // Mock LanguageManager (افتراضي إنجليزي)
        mockkObject(LanguageManager)
        every { LanguageManager.currentLanguage.value } returns "en"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(RetrofitInstance)
    }

    // ===== Tests for loadGenres =====

    @Test
    fun `loadGenres success should update genres`() = runTest(testDispatcher) {
        coEvery { mockApi.getGenres(any(), "en") } returns GenreResponse(fakeGenres)

        viewModel = DetailsViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(fakeGenres, state.genres)
        assertNull(state.genreError)
    }

    @Test
    fun `loadGenres network error should show error message`() = runTest(testDispatcher) {
        coEvery { mockApi.getGenres(any(), "en") } throws okio.IOException("No internet")

        viewModel = DetailsViewModel()
        advanceUntilIdle()

        assertEquals("Failed to load genres: No internet", viewModel.uiState.value.genreError)
    }

    @Test
    fun `loadGenres unexpected error should show generic message`() = runTest(testDispatcher) {
        coEvery { mockApi.getGenres(any(), "en") } throws RuntimeException("Server error")

        viewModel = DetailsViewModel()
        advanceUntilIdle()

        assertEquals("An unexpected error occurred: Server error", viewModel.uiState.value.genreError)
    }

    // ===== Tests for loadRecommendations =====

    @Test
    fun `loadRecommendations success should update recommended movies`() = runTest(testDispatcher) {
        val movieId = 123
        val response = MovieResponse(results = fakeMovies)
        coEvery { mockApi.getMovieRecommendations(movieId, any(), "en") } returns response

        viewModel = DetailsViewModel()
        viewModel.loadRecommendations(movieId)
        advanceUntilIdle()

        assertEquals(fakeMovies, viewModel.uiState.value.recommendedMovies)
    }

    @Test
    fun `loadRecommendations network error should show error`() = runTest(testDispatcher) {
        val movieId = 123
        coEvery { mockApi.getMovieRecommendations(movieId, any(), "en") } throws okio.IOException("No connection")

        viewModel = DetailsViewModel()
        viewModel.loadRecommendations(movieId)
        advanceUntilIdle()

        assertEquals("Failed to load Recommendations: No connection", viewModel.uiState.value.recommendationsError)
    }

    // ===== Tests for findTrailer =====

    @Test
    fun `findTrailer success should update trailerUrl`() = runTest(testDispatcher) {
        val movieId = 123
        val videos = listOf(
            Video(
                key = fakeTrailerKey,
                site = "YouTube",
                type = "Trailer",
                official = true
            )
        )
        coEvery { mockApi.getMovieVideos(movieId, any(), "en") } returns VideoResponse(videos)

        viewModel = DetailsViewModel()
        viewModel.findTrailer(movieId)
        advanceUntilIdle()

        assertEquals(fakeTrailerUrl, viewModel.uiState.value.trailerUrl)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.trailerError)
    }

    @Test
    fun `findTrailer no trailer found should show no trailer message`() = runTest(testDispatcher) {
        val movieId = 123
        coEvery { mockApi.getMovieVideos(movieId, any(), "en") } returns VideoResponse(emptyList())

        viewModel = DetailsViewModel()
        viewModel.findTrailer(movieId)
        advanceUntilIdle()

        assertEquals("No Trailer Found.", viewModel.uiState.value.trailerError)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.trailerUrl)
    }

    @Test
    fun `findTrailer network error should show trailer error message`() = runTest(testDispatcher) {
        val movieId = 123
        coEvery { mockApi.getMovieVideos(movieId, any(), "en") } throws okio.IOException("No internet")

        viewModel = DetailsViewModel()
        viewModel.findTrailer(movieId)
        advanceUntilIdle()

        assertEquals("Failed to load trailer: No internet", viewModel.uiState.value.trailerError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ===== Tests for prepareShareUrl =====

    @Test
    fun `prepareShareUrl success should update urlToShare`() = runTest(testDispatcher) {
        val movieId = 123
        val videos = listOf(Video(key = fakeTrailerKey, site = "YouTube", type = "Trailer", official = true))

        coEvery { mockApi.getGenres(any(), any()) } returns GenreResponse(emptyList())
        coEvery { mockApi.getMovieVideos(movieId, any(), "en") } returns VideoResponse(videos)

        viewModel = DetailsViewModel()
        viewModel.prepareShareUrl(movieId)
        advanceUntilIdle()

        assertEquals(fakeTrailerUrl, viewModel.uiState.value.urlToShare)
        assertNull(viewModel.uiState.value.trailerError)
    }

    @Test
    fun `prepareShareUrl no trailer should show error`() = runTest(testDispatcher) {
        val movieId = 123
        coEvery { mockApi.getMovieVideos(movieId, any(), "en") } returns VideoResponse(emptyList())

        viewModel = DetailsViewModel()
        viewModel.prepareShareUrl(movieId)
        advanceUntilIdle()

        assertEquals("No Trailer Found.", viewModel.uiState.value.trailerError)
        assertNull(viewModel.uiState.value.urlToShare)
    }

    // ===== Tests for Arabic language =====

    @Test
    fun `findTrailer error with Arabic language should show Arabic message`() = runTest(testDispatcher) {
        every { LanguageManager.currentLanguage.value } returns "ar"
        val movieId = 123
        coEvery { mockApi.getMovieVideos(movieId, any(), "ar") } throws okio.IOException("لا اتصال")

        viewModel = DetailsViewModel()
        viewModel.findTrailer(movieId)
        advanceUntilIdle()

        assertEquals("تعذر تحميل المقطع الدعائي: لا اتصال", viewModel.uiState.value.trailerError)
    }

    // ===== Tests for state cleanup methods =====

    @Test
    fun `onTrailerLaunched should clear trailerUrl`() = runTest(testDispatcher) {
        coEvery { mockApi.getMovieVideos(123, any(), "en") } returns VideoResponse(
            listOf(Video(key = fakeTrailerKey, site = "YouTube", type = "Trailer", official = true))
        )

        viewModel = DetailsViewModel()
        viewModel.findTrailer(123)
        advanceUntilIdle()

        assertEquals(fakeTrailerUrl, viewModel.uiState.value.trailerUrl)

        viewModel.onTrailerLaunched()
        assertNull(viewModel.uiState.value.trailerUrl)
    }

    @Test
    fun `onUrlShared should clear urlToShare`() = runTest(testDispatcher) {
        coEvery { mockApi.getMovieVideos(123, any(), "en") } returns VideoResponse(
            listOf(Video(key = fakeTrailerKey, site = "YouTube", type = "Trailer",official = true))
        )

        viewModel = DetailsViewModel()
        viewModel.prepareShareUrl(123)
        advanceUntilIdle()

        assertEquals(fakeTrailerUrl, viewModel.uiState.value.urlToShare)

        viewModel.onUrlShared()
        assertNull(viewModel.uiState.value.urlToShare)
    }

    @Test
    fun `onToastShown should clear error`() = runTest(testDispatcher) {
        coEvery { mockApi.getMovieVideos(any(), "en") } throws okio.IOException("No internet")

        viewModel = DetailsViewModel()
        advanceUntilIdle()

        //assertEquals("Failed to load genres: No internet", viewModel.uiState.value.trailerError)

        viewModel.onTrailerToastShown()
        assertNull(viewModel.uiState.value.trailerError)
    }
}