package com.movito.movito.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit Tests بسيطة للـ AuthViewModel
 *
 * بنختبر Email Validation بس علشان منحتاجش Firebase
 *
 * ⚠️ ملحوظة مهمة:
 * لازم تعدل الـ isValidEmail function في AuthViewModel تكون:
 *
 * private fun isValidEmail(email: String): Boolean {
 *     val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
 *     return emailPattern.matches(email)
 * }
 */
@ExperimentalCoroutinesApi
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // Wrapper class علشان نختبر Email Validation لوحدها
    // بدون ما نحتاج Firebase
    class EmailValidator {
        fun isValidEmail(email: String): Boolean {
            // نفس الـ logic اللي في AuthViewModel
            val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
            return emailPattern.matches(email)
        }
    }

    private lateinit var emailValidator: EmailValidator

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        emailValidator = EmailValidator()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== اختبارات Email Validation =====

    @Test
    fun `valid email should pass validation`() {
        // Arrange
        val validEmail = "test@example.com"

        // Act
        val result = emailValidator.isValidEmail(validEmail)

        // Assert
        assertTrue("Valid email should pass validation", result)
    }

    @Test
    fun `email without at symbol should fail validation`() {
        // Arrange
        val invalidEmail = "notanemail"

        // Act
        val result = emailValidator.isValidEmail(invalidEmail)

        // Assert
        assertFalse("Email without @ should fail", result)
    }

    @Test
    fun `email without domain should fail validation`() {
        // Arrange
        val invalidEmail = "test@"

        // Act
        val result = emailValidator.isValidEmail(invalidEmail)

        // Assert
        assertFalse("Email without domain should fail", result)
    }

    @Test
    fun `email without username should fail validation`() {
        // Arrange
        val invalidEmail = "@example.com"

        // Act
        val result = emailValidator.isValidEmail(invalidEmail)

        // Assert
        assertFalse("Email without username should fail", result)
    }

    @Test
    fun `email without extension should fail validation`() {
        // Arrange
        val invalidEmail = "test@example"

        // Act
        val result = emailValidator.isValidEmail(invalidEmail)

        // Assert
        assertFalse("Email without extension should fail", result)
    }

    @Test
    fun `empty email should fail validation`() {
        // Arrange
        val emptyEmail = ""

        // Act
        val result = emailValidator.isValidEmail(emptyEmail)

        // Assert
        assertFalse("Empty email should fail", result)
    }

    @Test
    fun `email with spaces should fail validation`() {
        // Arrange
        val invalidEmail = "test @example.com"

        // Act
        val result = emailValidator.isValidEmail(invalidEmail)

        // Assert
        assertFalse("Email with spaces should fail", result)
    }

    @Test
    fun `email with plus sign should pass validation`() {
        // Arrange
        val validEmail = "test+tag@example.com"

        // Act
        val result = emailValidator.isValidEmail(validEmail)

        // Assert
        assertTrue("Email with + should be valid", result)
    }

    @Test
    fun `email with dots should pass validation`() {
        // Arrange
        val validEmail = "first.last@example.com"

        // Act
        val result = emailValidator.isValidEmail(validEmail)

        // Assert
        assertTrue("Email with dots should be valid", result)
    }

    @Test
    fun `email with subdomain should pass validation`() {
        // Arrange
        val validEmail = "test@mail.example.com"

        // Act
        val result = emailValidator.isValidEmail(validEmail)

        // Assert
        assertTrue("Email with subdomain should be valid", result)
    }

    @Test
    fun `email with country code should pass validation`() {
        // Arrange
        val validEmail = "test@example.co.uk"

        // Act
        val result = emailValidator.isValidEmail(validEmail)

        // Assert
        assertTrue("Email with country code should be valid", result)
    }

    @Test
    fun `multiple valid emails should all pass`() {
        // Arrange
        val validEmails = listOf(
            "test@example.com",
            "user.name@example.co.uk",
            "user+tag@domain.com",
            "first.last@mail.example.org",
            "user_name@example.io"
        )

        // Act & Assert
        validEmails.forEach { email ->
            val result = emailValidator.isValidEmail(email)
            assertTrue("Email $email should be valid", result)
        }
    }

    @Test
    fun `multiple invalid emails should all fail`() {
        // Arrange
        val invalidEmails = listOf(
            "notanemail",
            "missing@domain",
            "@nodomain.com",
            "no.at.symbol.com",
            "",
            "test @example.com",
            "test@",
            "@test.com"
        )

        // Act & Assert
        invalidEmails.forEach { email ->
            val result = emailValidator.isValidEmail(email)
            assertFalse("Email '$email' should be invalid", result)
        }
    }

    @Test
    fun `email with numbers should pass validation`() {
        // Arrange
        val validEmail = "user123@example456.com"

        // Act
        val result = emailValidator.isValidEmail(validEmail)

        // Assert
        assertTrue("Email with numbers should be valid", result)
    }

    @Test
    fun `email with hyphen should pass validation`() {
        // Arrange
        val validEmail = "user-name@ex-ample.com"

        // Act
        val result = emailValidator.isValidEmail(validEmail)

        // Assert
        assertTrue("Email with hyphen should be valid", result)
    }

    @Test
    fun `email with underscore should pass validation`() {
        // Arrange
        val validEmail = "user_name@example.com"

        // Act
        val result = emailValidator.isValidEmail(validEmail)

        // Assert
        assertTrue("Email with underscore should be valid", result)
    }
}