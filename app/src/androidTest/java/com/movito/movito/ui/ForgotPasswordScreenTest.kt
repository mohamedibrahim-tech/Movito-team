
package com.movito.movito.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.movito.movito.viewmodel.AuthViewModel
import com.movito.movito.viewmodel.AuthState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgotPasswordScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)

    @Test
    fun forgotPasswordScreen_InitialState_CorrectElementsDisplayed() {
        composeTestRule.setContent {
            ForgotPasswordScreen(
                authViewModel = mockAuthViewModel,
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
                authViewModel = mockAuthViewModel,
                onPasswordResetSent = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Send Reset Email").assertIsEnabled()
    }

    @Test
    fun forgotPasswordScreen_SuccessfulPasswordReset_onPasswordResetSentCalled() {
        val onPasswordResetSent: () -> Unit = mockk(relaxed = true)
        val authState = MutableStateFlow(AuthState(message = "Password reset email sent."))
        every { mockAuthViewModel.authState } returns authState

        composeTestRule.setContent {
            ForgotPasswordScreen(
                authViewModel = mockAuthViewModel,
                onPasswordResetSent = onPasswordResetSent
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Send Reset Email").performClick()

        composeTestRule.waitForIdle()
        verify { onPasswordResetSent() }
    }
}
