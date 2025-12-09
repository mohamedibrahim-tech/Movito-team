package com.movito.movito



import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.movito.movito.data.model.Movie
import com.movito.movito.favorites.FavoritesRepository
import com.movito.movito.viewmodel.FavoritesViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test



@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: FavoritesRepository
    private lateinit var viewModel: FavoritesViewModel

    private val fakeMovie = Movie(id = 550, title = "Fight Club", posterPath = "/abc.jpg")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // 1. Mock Firebase Auth
        mockkStatic(FirebaseAuth::class)
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.uid } returns "test_user_123"
        val mockAuth = mockk<FirebaseAuth>()
        every { mockAuth.currentUser } returns mockUser
        every { FirebaseAuth.getInstance() } returns mockAuth

        // 2. Mock Firebase Firestore
        mockkStatic(FirebaseFirestore::class)
        val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockQuery = mockk<Query>(relaxed = true)
        val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

        every { FirebaseFirestore.getInstance() } returns mockFirestore
        every { mockFirestore.collection("favorites") } returns mockCollection
        every { mockCollection.whereEqualTo("userId", "test_user_123") } returns mockQuery
        every { mockQuery.addSnapshotListener(any()) } answers {
            val listener = arg<EventListener<QuerySnapshot>>(0)
            val mockSnapshot = mockk<QuerySnapshot>(relaxed = true)
            every { mockSnapshot.documents } returns emptyList()
            listener.onEvent(mockSnapshot, null)
            mockListenerRegistration
        }

        // 3. ⭐ Mock الـ Repository بشكل صحيح
        mockRepository = mockk(relaxed = true)

        // Non-suspend function
        every { mockRepository.currentUserId() } returns "test_user_123"

        // Suspend functions - استخدم coEvery
        coEvery { mockRepository.addToFavorites(any()) } returns Result.success(Unit)
        coEvery { mockRepository.removeFromFavorites(any()) } returns Result.success(Unit)

        // 4. Mock constructor عشان يستخدم الـ mock بتاعنا
        mockkConstructor(FavoritesRepository::class)

        // Non-suspend function
        every { anyConstructed<FavoritesRepository>().currentUserId() } returns "test_user_123"

        // Suspend functions على الـ constructor
        coEvery { anyConstructed<FavoritesRepository>().addToFavorites(any()) } coAnswers {
            mockRepository.addToFavorites(firstArg())
        }
        coEvery { anyConstructed<FavoritesRepository>().removeFromFavorites(any()) } coAnswers {
            mockRepository.removeFromFavorites(firstArg())
        }

        // 5. Reset singleton
        resetFavoritesViewModelSingleton()
    }

    private fun resetFavoritesViewModelSingleton() {
        try {
            // Try different possible field names
            val possibleFieldNames = listOf("instance", "INSTANCE", "_instance")

            val companionClass = FavoritesViewModel::class.java.declaredClasses
                .firstOrNull { it.simpleName == "Companion" }

            if (companionClass != null) {
                for (fieldName in possibleFieldNames) {
                    try {
                        val field = companionClass.getDeclaredField(fieldName)
                        field.isAccessible = true
                        field.set(null, null)
                        println("✅ Successfully reset singleton field: $fieldName")
                        return
                    } catch (e: NoSuchFieldException) {
                        // Try next field name
                        continue
                    }
                }
            }
            println("⚠️ Could not find singleton field to reset")
        } catch (e: Exception) {
            println("⚠️ Could not reset singleton: ${e.message}")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init should start listening and set loading false`() = runTest(testDispatcher) {
        viewModel = FavoritesViewModel.getInstance()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

//    @Test
//    fun `addToFavorites should add movie optimistically`() = runTest(testDispatcher) {
//        viewModel = FavoritesViewModel.getInstance()
//        advanceUntilIdle()
//
//        viewModel.addToFavorites(fakeMovie)
//        advanceUntilIdle()
//
//        assertTrue(viewModel.uiState.value.favorites.any { it.id == fakeMovie.id })
//       // coVerify { mockRepository.addToFavorites(fakeMovie) }
//        coVerify { anyConstructed<FavoritesRepository>().addToFavorites(fakeMovie) }
//
//    }

    @Test
    fun `addToFavorites should not add duplicate`() = runTest(testDispatcher) {
        viewModel = FavoritesViewModel.getInstance()
        advanceUntilIdle()

        viewModel.addToFavorites(fakeMovie)
        advanceUntilIdle()

        val countBefore = viewModel.uiState.value.favorites.size

        viewModel.addToFavorites(fakeMovie)
        advanceUntilIdle()

        assertEquals(countBefore, viewModel.uiState.value.favorites.size)
    }

    @Test
    fun `removeFromFavorites should remove movie optimistically`() = runTest(testDispatcher) {
        viewModel = FavoritesViewModel.getInstance()
        advanceUntilIdle()

        viewModel.addToFavorites(fakeMovie)
        advanceUntilIdle()

        viewModel.removeFromFavorites(fakeMovie.id)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.favorites.any { it.id == fakeMovie.id })
        coVerify { mockRepository.removeFromFavorites(fakeMovie.id) }
    }

    @Test
    fun `resetForNewUser should clear state and show loading`() = runTest(testDispatcher) {
        viewModel = FavoritesViewModel.getInstance()
        advanceUntilIdle()

        viewModel.addToFavorites(fakeMovie)
        advanceUntilIdle()

        val newUser = mockk<FirebaseUser>()
        every { newUser.uid } returns "new_user_999"
        every { FirebaseAuth.getInstance().currentUser } returns newUser

        viewModel.resetForNewUser()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.favorites.isEmpty())
    }

    @Test
    fun `isFavorite should return correct value`() = runTest(testDispatcher) {
        viewModel = FavoritesViewModel.getInstance()
        advanceUntilIdle()

        assertFalse(viewModel.isFavorite(fakeMovie.id))

        viewModel.addToFavorites(fakeMovie)
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite(fakeMovie.id))

        viewModel.removeFromFavorites(fakeMovie.id)
        advanceUntilIdle()

        assertFalse(viewModel.isFavorite(fakeMovie.id))
    }
}