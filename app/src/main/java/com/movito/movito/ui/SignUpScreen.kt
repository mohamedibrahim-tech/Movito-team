
package com.movito.movito.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoButton
import com.movito.movito.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: () -> Unit = {},
    onSignInClicked: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState.message != null) {
            Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
            onSignUpSuccess()
            authViewModel.resetState()
        }
        if (authState.error != null) {
            Toast.makeText(context, authState.error, Toast.LENGTH_SHORT).show()
            authViewModel.resetState()
        }
    }


    fun validateAndSignUp() {
        validationError = null

        when {
            email.isBlank() -> {
                validationError = "Email is required"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                validationError = "Invalid email format"
                return
            }
            password.isBlank() -> {
                validationError = "Password is required"
                return
            }
            password.length < 6 -> {
                validationError = "Password must be at least 6 characters"
                return
            }
            confirmPassword.isBlank() -> {
                validationError = "Please confirm your password"
                return
            }
            password != confirmPassword -> {
                validationError = "Passwords do not match"
                return
            }
        }

        authViewModel.signUpWithEmailPassword(email, password)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            MovitoLogo()
            Spacer(Modifier.height(48.dp))

            Text(
                text = stringResource(id = R.string.signup_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                CustomAuthTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        validationError = null
                    },
                    label = stringResource(id = R.string.signup_email_label),
                    icon = Icons.Default.Email
                )
                Spacer(Modifier.height(20.dp))

                CustomAuthTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        validationError = null
                    },
                    label = stringResource(id = R.string.signup_password_label),
                    icon = Icons.Default.Lock,
                    isPassword = true
                )
                Spacer(Modifier.height(20.dp))

                CustomAuthTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        validationError = null
                    },
                    label = stringResource(id = R.string.signup_confirm_password_label),
                    icon = Icons.Default.Lock,
                    isPassword = true
                )
            }

            Spacer(Modifier.height(40.dp))


            if (authState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            } else {

                Spacer(Modifier.height(16.dp))

                MovitoButton(
                    text = stringResource(id = R.string.signup_button),
                    onClick = { validateAndSignUp() },
                    modifier = Modifier.testTag("SignUpButton")
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            authState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.signup_already_have_account),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = onSignInClicked,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.signup_login),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Dark Mode Preview"
)
@Composable
fun FinalSignUpScreenPreviewDark() {
    MovitoTheme(darkTheme = true) {
        SignUpScreen(
            onSignUpSuccess = {},
            onSignInClicked = {},
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Light Mode Preview"
)
@Composable
fun FinalSignUpScreenPreviewLight() {
    MovitoTheme(darkTheme = false) {
        SignUpScreen(
            onSignUpSuccess = {},
            onSignInClicked = {},
        )
    }
}
