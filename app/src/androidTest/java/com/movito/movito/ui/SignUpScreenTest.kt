
package com.movito.movito.ui

import androidx.compose.ui.test.assertIsDisplayed
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
class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)

    @Test
    fun signUpScreen_InitialState_CorrectElementsDisplayed() {
        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = mockAuthViewModel,
                onSignUpSuccess = {},
                onSignInClicked = {}
            )
        }

        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Email").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Password").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Confirm Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_Validation_EmptyEmail_ShowsError() {
        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = mockAuthViewModel,
                onSignUpSuccess = {},
                onSignInClicked = {}
            )
        }

        composeTestRule.onNodeWithText("Sign Up").performClick()
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_Validation_InvalidEmail_ShowsError() {
        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = mockAuthViewModel,
                onSignUpSuccess = {},
                onSignInClicked = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("invalid-email")
        composeTestRule.onNodeWithText("Sign Up").performClick()
        composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_SuccessfulSignUp_onSignUpSuccessCalled() {
        val onSignUpSuccess: () -> Unit = mockk(relaxed = true)
        val authState = MutableStateFlow(AuthState(message = "Verification email sent."))
        every { mockAuthViewModel.authState } returns authState

        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = mockAuthViewModel,
                onSignUpSuccess = onSignUpSuccess,
                onSignInClicked = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithContentDescription("Password").performTextInput("password")
        composeTestRule.onNodeWithContentDescription("Confirm Password").performTextInput("password")
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitForIdle()
        verify { onSignUpSuccess() }
    }

    @Test
    fun signUpScreen_SignInClicked_onSignInClickedCalled() {
        val onSignInClicked: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = mockAuthViewModel,
                onSignUpSuccess = {},
                onSignInClicked = onSignInClicked
            )
        }

        composeTestRule.onNodeWithText("Login").performClick()

        verify { onSignInClicked() }
    }
}
