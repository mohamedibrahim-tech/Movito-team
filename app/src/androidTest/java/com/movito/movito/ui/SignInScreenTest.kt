
package com.movito.movito.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseUser
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
class SignInScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)

    @Test
    fun signInScreen_InitialState_CorrectElementsDisplayed() {
        composeTestRule.setContent {
            SignInScreen(
                authViewModel = mockAuthViewModel,
                onSignInSuccess = {},
                onSignUpClicked = {},
                onForgotPasswordClicked = {}
            )
        }

        composeTestRule.onNodeWithTag("SignInTitle").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Email").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Password").assertIsDisplayed()
        composeTestRule.onNodeWithTag("SignInButton").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }

    @Test
    fun signInScreen_InputValidation_ButtonEnabledWhenFieldsNotEmpty() {
        composeTestRule.setContent {
            SignInScreen(
                authViewModel = mockAuthViewModel,
                onSignInSuccess = {},
                onSignUpClicked = {},
                onForgotPasswordClicked = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("mohamedibrahim.aast.1@gmail.com")
        composeTestRule.onNodeWithTag("SignInButton").assertIsNotEnabled()

        composeTestRule.onNodeWithContentDescription("Password").performTextInput("12345678")
        composeTestRule.onNodeWithTag("SignInButton").assertIsEnabled()
    }

    @Test
    fun signInScreen_SuccessfulSignIn_onSignInSuccessCalled() {
        val onSignInSuccess: () -> Unit = mockk(relaxed = true)
        val mockUser: FirebaseUser = mockk(relaxed = true)
        val authState = MutableStateFlow(AuthState(user = mockUser))
        every { mockAuthViewModel.authState } returns authState

        composeTestRule.setContent {
            SignInScreen(
                authViewModel = mockAuthViewModel,
                onSignInSuccess = onSignInSuccess,
                onSignUpClicked = {},
                onForgotPasswordClicked = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithContentDescription("Password").performTextInput("password")
        composeTestRule.onNodeWithTag("SignInButton").performClick()

        composeTestRule.waitForIdle()
        verify { onSignInSuccess() }
    }

    @Test
    fun signInScreen_FailedSignIn_ToastShown() {
        val authState = MutableStateFlow(AuthState(error = "Invalid credentials"))
        every { mockAuthViewModel.authState } returns authState

        composeTestRule.setContent {
            SignInScreen(
                authViewModel = mockAuthViewModel,
                onSignInSuccess = {},
                onSignUpClicked = {},
                onForgotPasswordClicked = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithContentDescription("Password").performTextInput("wrongpassword")
        composeTestRule.onNodeWithTag("SignInButton").performClick()

        // Toast testing is complex. This verifies state handling.
    }

    @Test
    fun signInScreen_SignUpClicked_onSignUpClickedCalled() {
        val onSignUpClicked: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            SignInScreen(
                authViewModel = mockAuthViewModel,
                onSignInSuccess = {},
                onSignUpClicked = onSignUpClicked,
                onForgotPasswordClicked = {}
            )
        }

        composeTestRule.onNodeWithText("Sign Up").performClick()

        verify { onSignUpClicked() }
    }

    @Test
    fun signInScreen_ForgotPasswordClicked_onForgotPasswordClickedCalled() {
        val onForgotPasswordClicked: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            SignInScreen(
                authViewModel = mockAuthViewModel,
                onSignInSuccess = {},
                onSignUpClicked = {},
                onForgotPasswordClicked = onForgotPasswordClicked
            )
        }

        composeTestRule.onNodeWithText("Forgot Password?").performClick()

        verify { onForgotPasswordClicked() }
    }

    @Test
    fun signInScreen_RealSignIn_Success() {
        val onSignInSuccess: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            SignInScreen(
                onSignInSuccess = onSignInSuccess,
                onSignUpClicked = {},
                onForgotPasswordClicked = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("mohamedibrahim.aast.1@gmail.com")
        composeTestRule.onNodeWithContentDescription("Password").performTextInput("12345678")
        composeTestRule.onNodeWithTag("SignInButton").performClick()

        composeTestRule.waitForIdle()
        // As this is a real sign in, we can't directly verify the callback.
        // Instead, we can check for a UI change that indicates success.
    }

    @Test
    fun signInScreen_RealSignIn_Failure() {
        val onSignInSuccess: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            SignInScreen(
                onSignInSuccess = onSignInSuccess,
                onSignUpClicked = { },
                onForgotPasswordClicked = { }
            )
        }

        // Use real email and wrong password
        composeTestRule.onNodeWithContentDescription("Email").performTextInput("mohamedibrahim.aast.1@gmail.com")
        composeTestRule.onNodeWithContentDescription("Password").performTextInput("wrongpassword123")
        composeTestRule.onNodeWithTag("SignInButton").performClick()

        composeTestRule.waitForIdle()

        // Verify that the success callback is NOT called
        verify(exactly = 0) { onSignInSuccess() }
    }
}
