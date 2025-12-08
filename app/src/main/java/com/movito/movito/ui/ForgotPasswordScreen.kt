package com.movito.movito.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.R
import com.movito.movito.ui.common.CustomAuthTextField
import com.movito.movito.ui.common.MovitoButton
import com.movito.movito.viewmodel.AuthViewModel

/**
 * Screen for requesting a password reset via email.
 *
 * This screen provides a simple form where users can enter their email address
 * to receive a password reset link. It integrates with [AuthViewModel] to handle
 * the password reset request and provide feedback to the user.
 *
 * Features:
 * - Email input field with validation
 * - Loading state during request
 * - Success/error feedback via Toast messages
 * - Auto-navigation back on successful reset email sent
 *
 * * **Author**: Movito Development Team Member [Yossef Sayed](https://github.com/yossefsayedhassan)
 *
 * @param modifier [Modifier] for styling and layout
 * @param authViewModel The [AuthViewModel] instance handling authentication operations
 * @param onPasswordResetSent Callback triggered when password reset email is successfully sent
 *
 * @since 15 Nov 2025
 *
 * @see AuthViewModel.sendPasswordResetEmail
 *
 */
@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    onPasswordResetSent: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    /*
     * Observes authentication state changes and shows appropriate Toast messages.
     * Also triggers navigation back when password reset email is successfully sent.
     */
    LaunchedEffect(authState) {
        if (authState.message != null) {
            Toast.makeText(context, authState.message, Toast.LENGTH_SHORT)
                .apply {
                    setText(authState.message)
                }.show()
            onPasswordResetSent()
            authViewModel.resetState()
        }
        if (authState.error != null) {
            Toast.makeText(context, authState.error, Toast.LENGTH_SHORT)
                .apply {
                    setText(authState.error)
                }.show()
            authViewModel.resetState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.forgotpassword_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(32.dp))

        CustomAuthTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(id = R.string.forgotpassword_email_label),
            icon = Icons.Default.Email
        )
        Spacer(Modifier.height(40.dp))

        MovitoButton(
            text = stringResource(id = R.string.forgotpassword_send_button),
            modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            isLoading = authState.isLoading,
            enabled = email.isNotBlank(),
            onClick = { authViewModel.sendPasswordResetEmail(email.trim()) },
        )
    }
}