package com.movito.movito


import androidx.lifecycle.SavedStateHandle
import com.movito.movito.LanguageManager
import com.movito.movito.data.model.Movie
import com.movito.movito.data.source.remote.MovieResponse
import com.movito.movito.data.source.remote.RetrofitInstance
import com.movito.movito.data.source.remote.TmdbApi
import com.movito.movito.viewmodel.SearchViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*


@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApi = mockk<TmdbApi>(relaxed = true)

    private lateinit var viewModel: SearchViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    private val fakeMovies = listOf(
        Movie(id = 550, title = "Fight Club"),
        Movie(id = 27205, title = "Inception")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock RetrofitInstance
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns mockApi

        // Mock LanguageManager
        mockkObject(LanguageManager)
        every { LanguageManager.currentLanguage.value } returns "en"

        // Default successful response
        coEvery { mockApi.searchMovies(any(), any(), any()) } returns MovieResponse(results = fakeMovies)

        // Create SavedStateHandle + ViewModel
        savedStateHandle = SavedStateHandle()
        viewModel = SearchViewModel(savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `updateSearchQuery updates state and saves to SavedStateHandle`() = runTest(testDispatcher) {
        viewModel.updateSearchQuery("matrix")

        advanceUntilIdle()

        assertEquals("matrix", viewModel.uiState.value.searchQuery)
        assertEquals("matrix", savedStateHandle.get<String>("search_query"))
    }

    @Test
    fun `searchMovies with empty query clears results and does not call API`() = runTest(testDispatcher) {
        viewModel.updateSearchQuery("   ")
        viewModel.searchMovies()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasSearched)
        assertTrue(viewModel.uiState.value.movies.isEmpty())
        assertNull(viewModel.uiState.value.error)
        coVerify(exactly = 0) { mockApi.searchMovies(any(), any(), any()) }
    }

    @Test
    fun `searchMovies with valid query calls API and updates movies`() = runTest(testDispatcher) {
        viewModel.updateSearchQuery("inception")
        viewModel.searchMovies()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasSearched)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(fakeMovies, viewModel.uiState.value.movies)
        assertNull(viewModel.uiState.value.error)

        coVerify { mockApi.searchMovies(apiKey = any(), query = "inception", language = "en") }
    }

    @Test
    fun `searchMovies on API success sets loading false`() = runTest(testDispatcher) {
        viewModel.updateSearchQuery("avatar")
        viewModel.searchMovies()

        // نستني شوية عشان الكوروتين تبدأ وتغيّر isLoading = true
        advanceTimeBy(50)  // مهم جدًا
// Before API returns
      //  assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `searchMovies on API error shows error message in English`() = runTest(testDispatcher) {
        coEvery { mockApi.searchMovies(any(), any(), any()) } throws Exception("Network failed")

        viewModel.updateSearchQuery("titanic")
        viewModel.searchMovies()
        advanceUntilIdle()

        val error = viewModel.uiState.value.error
        assertTrue(error?.contains("Failed to fetch movies") == true)
        assertTrue(error?.contains("Network failed") == true)
        assertTrue(viewModel.uiState.value.movies.isEmpty())
    }

//
//@Test
//fun `searchMovies on API error shows Arabic message when language is ar`() = runTest(testDispatcher) {
//    //  نغيّر اللغة للعربي
//    every { LanguageManager.currentLanguage.value } returns "ar"
//
//    // . نعمل override كامل للـ mock (مهم جدًا نستخدم any() مش eq("ar"))
//    coEvery { mockApi.searchMovies(any(), any(), any()) } throws Exception("فشل الاتصال")
//
//    //  نعمل ViewModel جديد عشان ياخد اللغة الجديدة
//    viewModel = SearchViewModel(savedStateHandle)
//
//    viewModel.updateSearchQuery("القراصنة")
//    viewModel.searchMovies()
//    advanceUntilIdle()
//
//    val error = viewModel.uiState.value.error
//    println("Error message: '$error'") // اطبعي دي في الـ Logcat عشان نشوف إيه اللي بيحصل
//
//    assertNotNull(viewModel.uiState.value.isLoading)
//    assertTrue(viewModel.uiState.value.movies.isEmpty())
//    assertNotNull(error)
//    assertTrue(error!!.startsWith("تعذر تحميل الأفلام"))
//    assertTrue(error.contains("فشل الاتصال"))
//}

    @Test
    fun `errorShown clears error message`() = runTest(testDispatcher) {
        coEvery { mockApi.searchMovies(any(), any(), any()) } throws Exception("Test error")

        viewModel.updateSearchQuery("test")
        viewModel.searchMovies()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.errorShown()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

//    @Test
//    fun `searchQueryState emits current query correctly`() = runTest(testDispatcher) {
//        val collected = mutableListOf<String>()
//
//        val job = launch {
//            viewModel.searchQueryState.collect { collected.add(it) }
//        }
//
//        advanceUntilIdle() // نستني القيمة الأولى تنزل
//        viewModel.updateSearchQuery("first")
//        viewModel.updateSearchQuery("second")
//        advanceUntilIdle()
//
////        assertTrue(collected.contains(""))
////        assertTrue(collected.contains("first"))
////        assertTrue(collected.contains("second"))
//        assertEquals(listOf("", "first", "second"), collected)
//        job.cancel()
//    }


    @Test
    fun `saved query is restored after process death`() = runTest(testDispatcher) {
        // Simulate process death: save query first
        savedStateHandle["search_query"] = "saved query"

        // Create new ViewModel instance (like after process death)
        viewModel = SearchViewModel(savedStateHandle)
        advanceUntilIdle()

        assertEquals("saved query", viewModel.uiState.value.searchQuery)
    }
}