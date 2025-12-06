package com.movito.movito


import com.movito.movito.data.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit Tests للـ FavoritesRepository
 *
 *
 * الـ Tests دي بتختبر:
 * - Document ID formatting
 * - Error handling logic
 * - Data validation
 */
@ExperimentalCoroutinesApi
class FavoritesRepositoryTest {

    private lateinit var testMovie: Movie
    private val testUserId = "test_user_123"

    @Before
    fun setup() {
        testMovie = Movie(
            id = 1,
            title = "Test Movie",
            releaseDate = "2024-01-01",
            posterPath = "/test.jpg",
            voteAverage = 8.5,
            overview = "Test Overview",
            genreIds = listOf(1, 2, 3),
            homepage = "https://test.com"
        )
    }

    // ===== Helper Class لاختبار الـ Logic =====

    /**
     * Helper class تحتوي على نفس الـ logic بدون Firebase
     * علشان نختبر الـ business logic فقط
     */
    class FavoritesLogic {

        /**
         * توليد Document ID بنفس الطريقة اللي في الـ Repository
         */
        fun generateDocumentId(userId: String, movieId: Int): String {
            return "${userId}_${movieId}"
        }

        /**
         * التحقق من صحة userId
         */
        fun isValidUserId(userId: String?): Boolean {
            return !userId.isNullOrEmpty()
        }

        /**
         * التحقق من صحة movieId
         */
        fun isValidMovieId(movieId: Int): Boolean {
            return movieId > 0
        }

        /**
         * التحقق من صحة Movie object
         */
        fun isValidMovie(movie: Movie?): Boolean {
            return movie != null && movie.id > 0
        }

        /**
         * معالجة حالة User مش مسجل دخول
         */
        fun handleNotLoggedIn(): Result<Unit> {
            return Result.failure(Exception("Not logged in"))
        }

        /**
         * معالجة Success case
         */
        fun handleSuccess(): Result<Unit> {
            return Result.success(Unit)
        }

        /**
         * معالجة Exception
         */
        fun handleException(e: Exception): Result<Unit> {
            return Result.failure(e)
        }
    }

    private lateinit var logic: FavoritesLogic

    @Before
    fun setupLogic() {
        logic = FavoritesLogic()
    }

    // ===== اختبارات Document ID Generation =====

    @Test
    fun `generateDocumentId should format correctly`() {
        // Arrange
        val userId = "user123"
        val movieId = 456

        // Act
        val result = logic.generateDocumentId(userId, movieId)

        // Assert
        assertEquals("Document ID should be userId_movieId", "user123_456", result)
    }

    @Test
    fun `generateDocumentId with different values should format correctly`() {
        // Arrange & Act & Assert
        assertEquals("test_1", logic.generateDocumentId("test", 1))
        assertEquals("abc_999", logic.generateDocumentId("abc", 999))
        assertEquals("user_12345_100", logic.generateDocumentId("user_12345", 100))
    }

    @Test
    fun `generateDocumentId should handle special characters in userId`() {
        // Arrange
        val userId = "user@123.com"
        val movieId = 1

        // Act
        val result = logic.generateDocumentId(userId, movieId)

        // Assert
        assertEquals("user@123.com_1", result)
    }

    // ===== اختبارات Validation =====

    @Test
    fun `isValidUserId should return true for valid userId`() {
        // Arrange
        val validUserId = "user123"

        // Act
        val result = logic.isValidUserId(validUserId)

        // Assert
        assertTrue("Valid userId should return true", result)
    }

    @Test
    fun `isValidUserId should return false for null userId`() {
        // Act
        val result = logic.isValidUserId(null)

        // Assert
        assertFalse("Null userId should return false", result)
    }

    @Test
    fun `isValidUserId should return false for empty userId`() {
        // Act
        val result = logic.isValidUserId("")

        // Assert
        assertFalse("Empty userId should return false", result)
    }

    @Test
    fun `isValidMovieId should return true for positive id`() {
        // Arrange
        val validId = 1

        // Act
        val result = logic.isValidMovieId(validId)

        // Assert
        assertTrue("Positive ID should be valid", result)
    }

    @Test
    fun `isValidMovieId should return false for zero or negative id`() {
        // Act & Assert
        assertFalse("Zero should be invalid", logic.isValidMovieId(0))
        assertFalse("Negative should be invalid", logic.isValidMovieId(-1))
        assertFalse("Negative should be invalid", logic.isValidMovieId(-100))
    }

    @Test
    fun `isValidMovie should return true for valid movie`() {
        // Act
        val result = logic.isValidMovie(testMovie)

        // Assert
        assertTrue("Valid movie should return true", result)
    }

    @Test
    fun `isValidMovie should return false for null movie`() {
        // Act
        val result = logic.isValidMovie(null)

        // Assert
        assertFalse("Null movie should return false", result)
    }

    @Test
    fun `isValidMovie should return false for movie with invalid id`() {
        // Arrange
        val invalidMovie = testMovie.copy(id = 0)

        // Act
        val result = logic.isValidMovie(invalidMovie)

        // Assert
        assertFalse("Movie with ID 0 should be invalid", result)
    }

    // ===== اختبارات Error Handling =====

    @Test
    fun `handleNotLoggedIn should return failure with correct message`() {
        // Act
        val result = logic.handleNotLoggedIn()

        // Assert
        assertTrue("Should be failure", result.isFailure)
        assertEquals("Not logged in", result.exceptionOrNull()?.message)
    }

    @Test
    fun `handleSuccess should return success`() {
        // Act
        val result = logic.handleSuccess()

        // Assert
        assertTrue("Should be success", result.isSuccess)
    }

    @Test
    fun `handleException should return failure with exception`() {
        // Arrange
        val testException = Exception("Test error")

        // Act
        val result = logic.handleException(testException)

        // Assert
        assertTrue("Should be failure", result.isFailure)
        assertEquals("Test error", result.exceptionOrNull()?.message)
    }

    // ===== اختبارات Movie Data Model =====

    @Test
    fun `Movie should contain all required fields`() {
        // Assert - تحقق من أن الـ Movie فيها كل البيانات المطلوبة
        assertNotNull("Movie should not be null", testMovie)
        assertTrue("Movie ID should be positive", testMovie.id > 0)
        assertNotNull("Title should not be null", testMovie.title)
        assertFalse("Title should not be empty", testMovie.title.isEmpty())
    }

    @Test
    fun `Movie copy should create new instance with updated values`() {
        // Act
        val copiedMovie = testMovie.copy(id = 999, title = "New Title")

        // Assert
        assertEquals("Copied movie should have new ID", 999, copiedMovie.id)
        assertEquals("Copied movie should have new title", "New Title", copiedMovie.title)
        assertEquals("Original movie ID should be unchanged", 1, testMovie.id)
        assertEquals("Original movie title should be unchanged", "Test Movie", testMovie.title)
    }

    // ===== اختبارات Integration Logic =====

    @Test
    fun `document ID should be unique for different users`() {
        // Arrange
        val user1 = "user1"
        val user2 = "user2"
        val movieId = 1

        // Act
        val docId1 = logic.generateDocumentId(user1, movieId)
        val docId2 = logic.generateDocumentId(user2, movieId)

        // Assert
        assertNotEquals("Document IDs should be different", docId1, docId2)
    }

    @Test
    fun `document ID should be unique for different movies`() {
        // Arrange
        val userId = "user1"
        val movie1 = 1
        val movie2 = 2

        // Act
        val docId1 = logic.generateDocumentId(userId, movie1)
        val docId2 = logic.generateDocumentId(userId, movie2)

        // Assert
        assertNotEquals("Document IDs should be different", docId1, docId2)
    }

    @Test
    fun `same user and movie should generate same document ID`() {
        // Arrange
        val userId = "user1"
        val movieId = 1

        // Act
        val docId1 = logic.generateDocumentId(userId, movieId)
        val docId2 = logic.generateDocumentId(userId, movieId)

        // Assert
        assertEquals("Same parameters should generate same ID", docId1, docId2)
    }

    // ===== اختبارات Edge Cases =====

    @Test
    fun `document ID should handle very long userId`() {
        // Arrange
        val longUserId = "a".repeat(100)
        val movieId = 1

        // Act
        val result = logic.generateDocumentId(longUserId, movieId)

        // Assert
        assertNotNull("Should handle long userId", result)
        assertTrue("Should contain userId", result.contains(longUserId))
        assertTrue("Should contain movieId", result.endsWith("_1"))
    }

    @Test
    fun `document ID should handle very large movieId`() {
        // Arrange
        val userId = "user1"
        val largeMovieId = Int.MAX_VALUE

        // Act
        val result = logic.generateDocumentId(userId, largeMovieId)

        // Assert
        assertNotNull("Should handle large movieId", result)
        assertEquals("user1_${Int.MAX_VALUE}", result)
    }

    @Test
    fun `validation should handle boundary values`() {
        // Act & Assert
        assertTrue("Max int should be valid", logic.isValidMovieId(Int.MAX_VALUE))
        assertFalse("Min int should be invalid", logic.isValidMovieId(Int.MIN_VALUE))
        assertTrue("1 should be valid", logic.isValidMovieId(1))
    }
}
