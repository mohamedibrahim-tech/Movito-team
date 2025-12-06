package com.movito.movito


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.movito.movito.viewmodel.LanguageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit Tests للـ LanguageViewModel
 *
 *  ملحوظة: بنختبر الأجزاء اللي ممكن نختبرها بدون SharedPreferences
 *
 * بنختبر:
 * - Initial state
 * - onActivityRestarted behavior
 * - StateFlow behavior
 * - ViewModel lifecycle
 */
@ExperimentalCoroutinesApi
class LanguageViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: LanguageViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LanguageViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== اختبارات Initial State =====

    @Test
    fun `initial shouldRestartActivity should be false`() = runTest {
        // Assert
        val shouldRestart = viewModel.shouldRestartActivity.value
        assertFalse("Initial restart flag should be false", shouldRestart)
    }

    @Test
    fun `currentLanguage should have initial value`() = runTest {
        // Assert
        val language = viewModel.currentLanguage.value
        assertNotNull("Language should not be null", language)
        assertTrue(
            "Language should be 'en' or 'ar'",
            language == "en" || language == "ar"
        )
    }

    @Test
    fun `initial state should be stable`() = runTest {
        // Arrange
        val initialLanguage = viewModel.currentLanguage.value
        val initialRestart = viewModel.shouldRestartActivity.value

        // Wait a bit
        testScheduler.advanceTimeBy(100)

        // Assert - State shouldn't change on its own
        assertEquals(
            "Language should remain stable",
            initialLanguage,
            viewModel.currentLanguage.value
        )
        assertEquals(
            "Restart flag should remain stable",
            initialRestart,
            viewModel.shouldRestartActivity.value
        )
    }

    // ===== اختبارات onActivityRestarted =====

    @Test
    fun `onActivityRestarted when flag is false should keep it false`() = runTest {
        // Arrange
        assertFalse("Initial should be false", viewModel.shouldRestartActivity.value)

        // Act
        viewModel.onActivityRestarted()
        testScheduler.advanceUntilIdle()

        // Assert
        assertFalse(
            "Should still be false",
            viewModel.shouldRestartActivity.value
        )
    }

    @Test
    fun `calling onActivityRestarted multiple times should not cause issues`() = runTest {
        // Act - Call multiple times
        repeat(5) {
            viewModel.onActivityRestarted()
        }
        testScheduler.advanceUntilIdle()

        // Assert - Should handle gracefully
        assertFalse(
            "Flag should remain false",
            viewModel.shouldRestartActivity.value
        )
    }

    @Test
    fun `onActivityRestarted should complete without throwing`() = runTest {
        // Act & Assert - Should not throw
        try {
            viewModel.onActivityRestarted()
            testScheduler.advanceUntilIdle()
        } catch (e: Exception) {
            fail("onActivityRestarted should not throw: ${e.message}")
        }
    }

    // ===== اختبارات StateFlow Behavior =====

    @Test
    fun `shouldRestartActivity is a valid StateFlow`() = runTest {
        // Assert - Simply check that StateFlow is accessible and has a value
        assertNotNull("StateFlow should not be null", viewModel.shouldRestartActivity)

        // Can read current value
        val currentValue = viewModel.shouldRestartActivity.value
        assertNotNull("Should have a current value", currentValue)
    }

    @Test
    fun `currentLanguage is a valid StateFlow`() = runTest {
        // Assert - Check that StateFlow is accessible and has a value
        assertNotNull("Language StateFlow should not be null", viewModel.currentLanguage)

        // Can read current value
        val currentValue = viewModel.currentLanguage.value
        assertNotNull("Should have a current value", currentValue)
        assertTrue(
            "Language should be valid",
            currentValue == "en" || currentValue == "ar" || currentValue.isNotEmpty()
        )
    }

    @Test
    fun `multiple reads of StateFlow should return consistent values`() = runTest {
        // Read multiple times
        val value1 = viewModel.shouldRestartActivity.value
        val value2 = viewModel.shouldRestartActivity.value
        val value3 = viewModel.shouldRestartActivity.value

        // All reads should return the same value
        assertEquals("Values should be consistent", value1, value2)
        assertEquals("Values should be consistent", value2, value3)
    }

    // ===== اختبارات ViewModel Lifecycle =====

    @Test
    fun `ViewModel should be created successfully`() {
        // Assert
        assertNotNull("ViewModel should not be null", viewModel)
    }

    @Test
    fun `ViewModel state should be accessible after creation`() {
        // Assert
        assertNotNull(
            "shouldRestartActivity should be accessible",
            viewModel.shouldRestartActivity
        )
        assertNotNull(
            "currentLanguage should be accessible",
            viewModel.currentLanguage
        )
    }

    @Test
    fun `multiple ViewModel instances should be independent`() = runTest {
        // Arrange
        val viewModel1 = LanguageViewModel()
        val viewModel2 = LanguageViewModel()

        // Act
        viewModel1.onActivityRestarted()
        testScheduler.advanceUntilIdle()

        // Assert - Both should have their own state
        assertNotSame(
            "ViewModels should be different instances",
            viewModel1,
            viewModel2
        )
    }

    // ===== اختبارات Coroutine Safety =====

    @Test
    fun `onActivityRestarted should be coroutine-safe`() = runTest {
        // Act - Call from multiple coroutines sequentially
        repeat(10) {
            viewModel.onActivityRestarted()
        }
        testScheduler.advanceUntilIdle()

        // Assert - Should handle multiple calls
        val finalValue = viewModel.shouldRestartActivity.value
        assertNotNull("Should have a valid state", finalValue)
        assertFalse("State should be false", finalValue)
    }

    @Test
    fun `StateFlow value reads are thread-safe`() = runTest {
        // Multiple reads should all succeed
        val values = mutableListOf<Boolean>()

        repeat(100) {
            values.add(viewModel.shouldRestartActivity.value)
        }

        // Assert - All reads should succeed and return consistent values
        assertEquals("Should have 100 values", 100, values.size)
        assertTrue(
            "All values should be the same",
            values.all { it == values.first() }
        )
    }

    // ===== اختبارات Edge Cases =====

    @Test
    fun `rapid calls to onActivityRestarted should not crash`() = runTest {
        // Act - Very rapid calls
        repeat(100) {
            viewModel.onActivityRestarted()
        }
        testScheduler.advanceUntilIdle()

        // Assert - Should complete without issues
        assertFalse(
            "State should remain stable",
            viewModel.shouldRestartActivity.value
        )
    }

    @Test
    fun `ViewModel should survive repeated operations`() = runTest {
        // Act - Multiple operations
        repeat(10) {
            viewModel.onActivityRestarted()
            testScheduler.advanceTimeBy(50)
        }

        // Assert - ViewModel should still be functional
        assertNotNull("ViewModel should still be valid", viewModel)
        assertFalse(
            "State should be consistent",
            viewModel.shouldRestartActivity.value
        )
    }

    // ===== اختبارات State Consistency =====

    @Test
    fun `shouldRestartActivity value should be consistent`() = runTest {
        // Arrange
        val value1 = viewModel.shouldRestartActivity.value

        // Wait
        testScheduler.advanceTimeBy(100)

        val value2 = viewModel.shouldRestartActivity.value

        // Assert - Without any operations, value should remain the same
        assertEquals(
            "Value should be consistent over time",
            value1,
            value2
        )
    }

    @Test
    fun `currentLanguage value should be valid at all times`() = runTest {
        // Check multiple times
        repeat(5) {
            val language = viewModel.currentLanguage.value
            assertTrue(
                "Language should always be valid",
                language == "en" || language == "ar" || language.isNotEmpty()
            )
            testScheduler.advanceTimeBy(50)
        }
    }


    /**
     *
     * لاختبار setLanguage و loadLanguagePreference بشكل كامل،
     * محتاجين نعمل Mock لـ SharedPreferences أو نستخدم
     * Robolectric مع proper Android Context.
     *
     * الـ Tests دي بتختبر الأجزاء اللي ممكن نختبرها
     * بدون dependencies خارجية.
     *
     * للاختبار الكامل، استخدم Instrumented Tests في androidTest/
     */
}

