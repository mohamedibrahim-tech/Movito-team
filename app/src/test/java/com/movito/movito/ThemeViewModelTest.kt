package com.movito.movito


import android.app.Application
import androidx.compose.runtime.currentCompositionLocalContext
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
        mockkConstructor(ThemeManager::class)
        coEvery { anyConstructed<ThemeManager>().isDarkTheme } returns flowOf(false) as StateFlow<Boolean>
        coEvery { anyConstructed<ThemeManager>().toggleTheme(any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state is light`() = runTest(testDispatcher) {
        val viewModel = ThemeViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.isDarkTheme.value)
    }

    @Test
    fun `toggle saves correctly`() = runTest(testDispatcher) {
        coVerify {
        val viewModel = ThemeViewModel()
        viewModel.toggleTheme(true,any())
        advanceUntilIdle()
         anyConstructed<ThemeManager>().toggleTheme(true, any()) }
    }
}