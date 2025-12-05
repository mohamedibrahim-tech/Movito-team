
package com.movito.movito.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.movito.movito.viewmodel.AuthViewModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgotPasswordScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun forgotPasswordScreen_InitialState_CorrectElementsDisplayed() {
        composeTestRule.setContent {
            ForgotPasswordScreen(
                authViewModel = viewModel(),
                onPasswordResetSent = {}
            )
        }

        composeTestRule.onNodeWithText("Reset Password").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send Reset Email").assertIsNotEnabled()
    }

    @Test
    fun forgotPasswordScreen_InputValidation_ButtonEnabledWhenFieldNotEmpty() {
        composeTestRule.setContent {
            ForgotPasswordScreen(
                authViewModel = viewModel(),
                onPasswordResetSent = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Send Reset Email").assertIsEnabled()
    }

    @Test
    fun forgotPasswordScreen_SuccessfulPasswordReset_onPasswordResetSentCalled() {
        var passwordResetSent = false
        composeTestRule.setContent {
            // Use a real ViewModel, but intercept the callback
            ForgotPasswordScreen(
                authViewModel = viewModel(),
                onPasswordResetSent = { passwordResetSent = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("mohamedibrahim.aast.1@gmail.com")
        composeTestRule.onNodeWithText("Send Reset Email").performClick()

        // Wait up to 10 seconds for the async operation to complete and the callback to be called.
        // This is more reliable than waitForIdle() for real network calls.
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            passwordResetSent
        }

        // Now, assert that the callback was invoked
        assertTrue("The onPasswordResetSent callback was not invoked.", passwordResetSent)
    }
}
