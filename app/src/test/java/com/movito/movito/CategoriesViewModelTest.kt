package com.movito.movito

import com.movito.movito.viewmodel.CategoriesViewModel

import com.movito.movito.LanguageManager
import com.movito.movito.data.model.Genre
import com.movito.movito.data.source.remote.GenreResponse
import com.movito.movito.data.source.remote.RetrofitInstance
import com.movito.movito.data.source.remote.TmdbApi
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
class CategoriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApi: TmdbApi = mockk()  // عادي من غير relaxed

    private lateinit var viewModel: CategoriesViewModel

    private val fakeGenres = listOf(
        Genre(id = 28, name = "Action"),
        Genre(id = 35, name = "Comedy")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // الحل السحري: نحقن الموك جوا RetrofitInstance نفسه
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns mockApi

        // Mock للغة
        mockkObject(LanguageManager)
        every { LanguageManager.currentLanguage.value } returns "en"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(RetrofitInstance)  // مهم جداً نرجّعه زي ما كان
    }

    @Test
    fun `loadGenres success should update genres correctly`() = runTest(testDispatcher) {
        coEvery { mockApi.getGenres(any(), any()) } returns GenreResponse(fakeGenres)

        viewModel = CategoriesViewModel()  // الكونستراكتور العادي خالص!
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(fakeGenres, state.genres)
        assertNull(state.error)
    }

    @Test
    fun `loadGenres network error should show error message`() = runTest(testDispatcher) {
        coEvery { mockApi.getGenres(any(), any()) } throws okio.IOException("No internet")

        viewModel = CategoriesViewModel()
        advanceUntilIdle()

        assertEquals("Failed to load genres: No internet", viewModel.uiState.value.error)
    }

    @Test
    fun `loadGenres unexpected error should show generic message`() = runTest(testDispatcher) {
        coEvery { mockApi.getGenres(any(), any()) } throws RuntimeException("Server down")

        viewModel = CategoriesViewModel()
        advanceUntilIdle()

        assertEquals("An unexpected error occurred: Server down", viewModel.uiState.value.error)
    }

    @Test
    fun `when language is Arabic, error message should be in Arabic`() = runTest(testDispatcher) {
        every { LanguageManager.currentLanguage.value } returns "ar"
        coEvery { mockApi.getGenres(any(), "ar") } throws okio.IOException("لا يوجد اتصال")

        viewModel = CategoriesViewModel()
        advanceUntilIdle()

        assertEquals("تعذر تحميل الفئات: لا يوجد اتصال", viewModel.uiState.value.error)
    }

    @Test
    fun `initial state should be correct`() {
        viewModel = CategoriesViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.genres.isEmpty())
        assertNull(state.error)
    }
}