
package com.movito.movito.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signUpScreen_InitialState_CorrectElementsDisplayed() {
        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = viewModel(),
                onSignUpSuccess = {},
                onSignInClicked = {}
            )
        }

        composeTestRule.onNodeWithTag("SignUpTitle").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Email").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Password").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Confirm Password").assertIsDisplayed()
        composeTestRule.onNodeWithTag("SignUpButton").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_Validation_EmptyEmail_ShowsError() {
        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = viewModel(),
                onSignUpSuccess = {},
                onSignInClicked = {}
            )
        }

        composeTestRule.onNodeWithTag("SignUpButton").performClick()
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_Validation_InvalidEmail_ShowsError() {
        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = viewModel(),
                onSignUpSuccess = {},
                onSignInClicked = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Email").performTextInput("invalid-email")
        composeTestRule.onNodeWithTag("SignUpButton").performClick()
        composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_SuccessfulSignUp_onSignUpSuccessCalled() {
        var signUpSuccess = false
        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = viewModel(),
                onSignUpSuccess = { signUpSuccess = true },
                onSignInClicked = {}
            )
        }

        // Use a unique email to avoid conflicts with existing accounts
        val uniqueEmail = "testuser_${System.currentTimeMillis()}@example.com"
        composeTestRule.onNodeWithContentDescription("Email").performTextInput(uniqueEmail)
        composeTestRule.onNodeWithContentDescription("Password").performTextInput("password123")
        composeTestRule.onNodeWithContentDescription("Confirm Password").performTextInput("password123")
        composeTestRule.onNodeWithTag("SignUpButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            signUpSuccess
        }
        assertTrue(signUpSuccess)
    }

    @Test
    fun signUpScreen_SignInClicked_onSignInClickedCalled() {
        var signInClicked = false
        composeTestRule.setContent {
            SignUpScreen(
                authViewModel = viewModel(),
                onSignUpSuccess = {},
                onSignInClicked = { signInClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Login").performClick()
        assertTrue(signInClicked)
    }
}
