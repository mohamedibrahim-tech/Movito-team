package com.movito.movito


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import org.junit.Assert.*


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // أي SDK حديث
class MovitoApplicationTest {

    private val mockContext = mockk<Context>(relaxed = true)
    private val mockResources = mockk<android.content.res.Resources>(relaxed = true)
    private val mockConfig = mockk<Configuration>(relaxed = true)

    @Before
    fun setup() {
        clearAllMocks()


        // Mock LanguageManager.loadLanguagePreference
        mockkObject(LanguageManager)
        coEvery { LanguageManager.loadLanguagePreference(any()) } just Runs
        // Mock resources
        every { mockContext.resources } returns mockResources
        every { mockResources.configuration } returns mockConfig
        every { mockResources.displayMetrics } returns mockk()


    }

    @After
    fun tearDown() {
        unmockkAll()

    }



    @Test
    fun `getSavedLanguage returns current language from LanguageManager`() {
        every { LanguageManager.currentLanguage.value } returns "ar"

        val result = MovitoApplication.getSavedLanguage(mockContext)

        assertEquals("ar", result)
    }


//    @Test
//    fun `updateBaseContextLocale does not throw exception`() {
//        val context = mockk<Context>(relaxed = true)
//        MovitoApplication.updateBaseContextLocale(context, "ar")
//    }
//
//    @Test
//    fun `updateBaseContextLocale handles invalid language code`() {
//        val context = mockk<Context>(relaxed = true)
//        MovitoApplication.updateBaseContextLocale(context, "xyz123")
//    }



    @Test
    fun `LanguageChangeObserver adds and removes listeners correctly`() = runTest {
        val listener1 = mockk<() -> Unit>(relaxed = true)
        val listener2 = mockk<() -> Unit>(relaxed = true)

        MovitoApplication.LanguageChangeObserver.addListener(listener1)
        MovitoApplication.LanguageChangeObserver.addListener(listener2)
        MovitoApplication.LanguageChangeObserver.addListener(listener1) // duplicate

        MovitoApplication.LanguageChangeObserver.notifyLanguageChanged()

        verify(exactly = 1) { listener1() }
        verify(exactly = 1) { listener2() }

        MovitoApplication.LanguageChangeObserver.removeListener(listener1)

        MovitoApplication.LanguageChangeObserver.notifyLanguageChanged()

        verify(exactly = 1) { listener1() } // no more calls
        verify(exactly = 2) { listener2() }
    }

    @Test
    fun `LanguageChangeObserver handles empty listeners gracefully`() {
        MovitoApplication.LanguageChangeObserver.notifyLanguageChanged()
        // Should not throw
    }


}

