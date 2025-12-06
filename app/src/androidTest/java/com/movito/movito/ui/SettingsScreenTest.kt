
package com.movito.movito.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_InitialState_CorrectElementsDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(
                onThemeToggle = {},
                onSignOut = {},
                userEmail = "test@example.com",
                onChangePassword = {},
                isDarkTheme = false
            )
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_SignOutButton_CallsOnSignOut() {
        var signOutCalled = false
        composeTestRule.setContent {
            SettingsScreen(
                onThemeToggle = {},
                onSignOut = { signOutCalled = true },
                userEmail = "test@example.com",
                onChangePassword = {},
                isDarkTheme = false
            )
        }

        composeTestRule.onNodeWithText("Sign Out").performClick()
        assertTrue(signOutCalled)
    }

    @Test
    fun settingsScreen_ChangePasswordButton_CallsOnChangePassword() {
        var changedPasswordForEmail: String? = null
        val testEmail = "test@example.com"
        composeTestRule.setContent {
            SettingsScreen(
                onThemeToggle = {},
                onSignOut = {},
                userEmail = testEmail,
                onChangePassword = { email -> changedPasswordForEmail = email },
                isDarkTheme = false
            )
        }

        composeTestRule.onNodeWithText("Change Password").performClick()
        assertEquals(testEmail, changedPasswordForEmail)
    }
}
