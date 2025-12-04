package com.movito.movito


import android.app.Application
import com.movito.movito.data.ThemeDataStore
import com.movito.movito.viewmodel.ThemeViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkConstructor(ThemeDataStore::class)
        every { anyConstructed<ThemeDataStore>().isDarkThemeSync() } returns false
        coEvery { anyConstructed<ThemeDataStore>().isDarkTheme } returns flowOf(false)
        coEvery { anyConstructed<ThemeDataStore>().saveThemePreference(any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state is light`() = runTest(testDispatcher) {
        val viewModel = ThemeViewModel(mockk(relaxed = true))
        advanceUntilIdle()
        assertFalse(viewModel.isDarkTheme.value)
    }

    @Test
    fun `toggle saves correctly`() = runTest(testDispatcher) {
        val viewModel = ThemeViewModel(mockk(relaxed = true))
        viewModel.toggleTheme(true)
        advanceUntilIdle()
        coVerify { anyConstructed<ThemeDataStore>().saveThemePreference(true) }
    }
}