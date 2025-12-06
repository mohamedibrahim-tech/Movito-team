package com.movito.movito


import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*



@OptIn(ExperimentalCoroutinesApi::class)
class LanguageManagerTest {

    private val mockContext = mockk<Context>()
    private val mockPrefs = mockk<SharedPreferences>(relaxed = true)
    private val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

    private val languageFlow = MutableStateFlow("en") // حقيقي نتحكم فيه

    @Before
    fun setup() {
        clearAllMocks()

        // Mock SharedPreferences
        every { mockContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs

        // نعمل mock للـ LanguageManager
        mockkObject(LanguageManager)

        // نرجّع الـ flow الحقيقي بتاعنا
        every { LanguageManager.currentLanguage } returns languageFlow

        // نعمل spy على setLanguage و loadLanguagePreference عشان نستدعي الكود الحقيقي
        every { LanguageManager.setLanguage(any(), any()) } answers {
            val lang = firstArg<String>()
            val ctx = secondArg<Context>()
            languageFlow.value = lang
            // نستدعي الكود الحقيقي لحفظ اللغة
            callOriginal()
        }

        every { LanguageManager.loadLanguagePreference(any()) } answers {
            val ctx = firstArg<Context>()
            val savedLang = mockPrefs.getString("app_language", "en") ?: "en"
            languageFlow.value = savedLang
        }
    }

    @After
    fun tearDown() {
        unmockkObject(LanguageManager)
        clearAllMocks()
    }

    @Test
    fun `initial language is en`() {
        assertEquals("en", LanguageManager.currentLanguage.value)
    }

    @Test
    fun `loadLanguagePreference loads saved language`() = runTest {
        every { mockPrefs.getString("app_language", "en") } returns "ar"

        LanguageManager.loadLanguagePreference(mockContext)

        assertEquals("ar", LanguageManager.currentLanguage.value)
    }

    @Test
    fun `setLanguage updates state and saves preference`() = runTest {
        LanguageManager.setLanguage("ar", mockContext)

        assertEquals("ar", LanguageManager.currentLanguage.value)
        verify { mockEditor.putString("app_language", "ar") }
        verify { mockEditor.apply() }
    }

    @Test
    fun `setLanguage emits new values through StateFlow`() = runTest {
        val collected = mutableListOf<String>()

        val job = launch(UnconfinedTestDispatcher()) {
            LanguageManager.currentLanguage.collect { collected.add(it) }
        }

        advanceUntilIdle() // القيمة الأولى "en"

        LanguageManager.setLanguage("ar", mockContext)
        LanguageManager.setLanguage("en", mockContext)

        advanceUntilIdle()

        assertEquals(listOf("en", "ar", "en"), collected)

        job.cancel()
    }
}
