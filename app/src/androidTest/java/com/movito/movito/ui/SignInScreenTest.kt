
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signInScreen_InitialState_CorrectElementsDisplayed() {
        composeTestRule.setContent {
            SignInScreen(
                authViewModel = viewModel(),
                onSignInSuccess = {},
                onSignUpClicked = {},
                onForgotPasswordClicked = {},
                onLanguageChange = {}
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
                authViewModel = viewModel(),
                onSignInSuccess = {},
                onSignUpClicked = {},
                onForgotPasswordClicked = {},
                onLanguageChange = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("mohamedibrahim.aast.1@gmail.com")
        composeTestRule.onNodeWithTag("SignInButton").assertIsNotEnabled()

        composeTestRule.onNodeWithContentDescription("Password").performTextInput("12345678")
        composeTestRule.onNodeWithTag("SignInButton").assertIsEnabled()
    }

    @Test
    fun signInScreen_SignUpClicked_onSignUpClickedCalled() {
        var signUpClicked = false
        composeTestRule.setContent {
            SignInScreen(
                authViewModel = viewModel(),
                onSignInSuccess = {},
                onSignUpClicked = { signUpClicked = true },
                onForgotPasswordClicked = {},
                onLanguageChange = {}
            )
        }

        composeTestRule.onNodeWithText("Sign Up").performClick()
        assertTrue(signUpClicked)
    }

    @Test
    fun signInScreen_ForgotPasswordClicked_onForgotPasswordClickedCalled() {
        var forgotPasswordClicked = false
        composeTestRule.setContent {
            SignInScreen(
                authViewModel = viewModel(),
                onSignInSuccess = {},
                onSignUpClicked = {},
                onForgotPasswordClicked = { forgotPasswordClicked = true },
                onLanguageChange = {}
            )
        }

        composeTestRule.onNodeWithText("Forgot Password?").performClick()
        assertTrue(forgotPasswordClicked)
    }

    @Test
    fun signInScreen_RealSignIn_Success() {
        var signInSuccess = false
        composeTestRule.setContent {
            SignInScreen(
                onSignInSuccess = { signInSuccess = true },
                onSignUpClicked = {},
                onForgotPasswordClicked = {},
                onLanguageChange = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("mohamedibrahim.aast.1@gmail.com")
        composeTestRule.onNodeWithContentDescription("Password").performTextInput("12345678")
        composeTestRule.onNodeWithTag("SignInButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            signInSuccess
        }
        assertTrue(signInSuccess)
    }

    @Test
    fun signInScreen_RealSignIn_Failure() {
        var signInSuccess = false
        composeTestRule.setContent {
            SignInScreen(
                onSignInSuccess = { signInSuccess = true },
                onSignUpClicked = { },
                onForgotPasswordClicked = { },
                onLanguageChange = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("mohamedibrahim.aast.1@gmail.com")
        composeTestRule.onNodeWithContentDescription("Password").performTextInput("wrongpassword123")
        composeTestRule.onNodeWithTag("SignInButton").performClick()

        composeTestRule.waitForIdle()

        assertFalse(signInSuccess)
    }
}
