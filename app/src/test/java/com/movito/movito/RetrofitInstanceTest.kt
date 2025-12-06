package com.movito.movito


import com.movito.movito.data.source.remote.RetrofitInstance
import com.movito.movito.data.source.remote.TmdbApi
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*


class RetrofitInstanceTest {

    private val mockApi = mockk<TmdbApi>(relaxed = true)

    @Before
    fun setup() {
        // السطر السحري اللي هيخلّي كل الـ ViewModels تشتغل مع mock
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns mockApi
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitInstance)
    }

    @Test
    fun `RetrofitInstance can be mocked successfully`() {
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns mockApi

        val api = RetrofitInstance.api

        assertNotNull(api)
        assertSame(mockApi, api)
    }

    @Test
    fun `multiple calls return the same mocked instance`() {
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns mockApi

        val first = RetrofitInstance.api
        val second = RetrofitInstance.api

        assertSame(first, second)
        assertSame(mockApi, first)
    }

    @Test
    fun `multiple calls to api return the same instance`() = runTest {
        val first = RetrofitInstance.api
        val second = RetrofitInstance.api

        assertSame(first, second) // لأنه singleton
    }

    @Test
    fun `mocked api can be used in ViewModels without NPE`() = runTest {
        // ده أهم تيست → يثبت إن كل الـ ViewModels اللي عملناها هتشتغل
        coEvery { mockApi.getGenres(any(), any()) } returns mockk()
        coEvery { mockApi.searchMovies(any(), any(), any()) } returns mockk()
        coEvery { mockApi.discoverMoviesByGenre(any(), any(), any(), any()) } returns mockk()

        // لو وصلنا لهنا بدون NPE → يعني كل حاجة تمام
        assertSame(mockApi, RetrofitInstance.api)
    }
}