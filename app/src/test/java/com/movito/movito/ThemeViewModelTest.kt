package com.movito.movito


import android.app.Application
import android.content.Context
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
class ThemeViewModelTest {

    private lateinit var viewModel: ThemeViewModel
    private val mockContext: Context = mockk(relaxed = true)

    // الـ StateFlow اللي هنتحكم فيه يدويًا
    private val fakeIsDarkThemeFlow = MutableStateFlow(false)
    private val fakeIsDarkTheme: StateFlow<Boolean> = fakeIsDarkThemeFlow

    @Before
    fun setup() {
        clearAllMocks()

        // Mock الكائن ThemeManager كـ object
        mockkObject(ThemeManager)

        // الحل الصحيح: رجّع الـ fake flow اللي إحنا بنتحكم فيه
        every { ThemeManager.isDarkTheme } returns fakeIsDarkTheme

        // Mock الدوال اللي بتستخدمها
        every { ThemeManager.loadThemePreference(any()) } just Runs
        every { ThemeManager.toggleTheme(any(), any()) } answers {
            val enableDark = firstArg<Boolean>()
            fakeIsDarkThemeFlow.value = enableDark
        }

        viewModel = ThemeViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isDarkTheme returns ThemeManager flow value`() = runTest {
        fakeIsDarkThemeFlow.value = true
        assertTrue(viewModel.isDarkTheme.first())

        fakeIsDarkThemeFlow.value = false
        assertFalse(viewModel.isDarkTheme.first())
    }

    @Test
    fun `loadThemePreference calls ThemeManager loadThemePreference`() {
        viewModel.loadThemePreference(mockContext)
        verify { ThemeManager.loadThemePreference(mockContext) }
    }

    @Test
    fun `toggleTheme to dark enables dark mode`() = runTest {
        viewModel.toggleTheme(enableDarkTheme = true, context = mockContext)

        verify { ThemeManager.toggleTheme(true, mockContext) }
        assertTrue(fakeIsDarkThemeFlow.value)
        assertTrue(viewModel.isDarkTheme.first())
    }

    @Test
    fun `toggleTheme to light disables dark mode`() = runTest {
        fakeIsDarkThemeFlow.value = true // نبدأ dark

        viewModel.toggleTheme(enableDarkTheme = false, context = mockContext)

        verify { ThemeManager.toggleTheme(false, mockContext) }
        assertFalse(fakeIsDarkThemeFlow.value)
        assertFalse(viewModel.isDarkTheme.first())
    }

    @Test
    fun `multiple toggles update state correctly`() = runTest {
        viewModel.toggleTheme(true, mockContext)
        assertTrue(viewModel.isDarkTheme.first())

        viewModel.toggleTheme(false, mockContext)
        assertFalse(viewModel.isDarkTheme.first())

        viewModel.toggleTheme(true, mockContext)
        assertTrue(viewModel.isDarkTheme.first())
    }

    @Test
    fun `toggleTheme with same value still calls ThemeManager`() {
        fakeIsDarkThemeFlow.value = true

        viewModel.toggleTheme(true, mockContext)
        verify(atLeast = 1) { ThemeManager.toggleTheme(true, mockContext) }
    }
}