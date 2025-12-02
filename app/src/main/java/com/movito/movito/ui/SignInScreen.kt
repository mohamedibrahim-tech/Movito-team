package com.movito.movito.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun MovitoLogo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.movito_logo),
            contentDescription = stringResource(id = R.string.signin_movito_logo_description),
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CustomAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,

        placeholder = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant) },

        leadingIcon = {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(

            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),


            cursorColor = MaterialTheme.colorScheme.primary,

            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}


@Composable
fun SignInScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignInSuccess: () -> Unit,
    onSignUpClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState.user != null) {
            onSignInSuccess()
            authViewModel.resetState()
        }
        if (authState.error != null) {
            Toast.makeText(context, authState.error, Toast.LENGTH_SHORT).show()
            authViewModel.resetState()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                MovitoLogo()
                Spacer(Modifier.height(48.dp))
                Text(
                    text = stringResource(id = R.string.signin_title),
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
                        onValueChange = { email = it },
                        label = stringResource(id = R.string.signin_email_label),
                        icon = Icons.Default.Email
                    )
                    Spacer(Modifier.height(20.dp))

                    CustomAuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(id = R.string.signin_password_label),
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )
                }

                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onForgotPasswordClicked,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.signin_forgot_password),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }
                Spacer(Modifier.height(24.dp))

                if (authState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    val isButtonEnabled = email.isNotBlank() && password.isNotBlank()

                    MovitoButton(
                        text = stringResource(id = R.string.signin_button),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        roundedCornerSize = 12.dp,
                        enabled = isButtonEnabled,
                    ) { authViewModel.signInWithEmailPassword(email.trim(), password) }

                    Spacer(Modifier.height(16.dp))

                }

                Spacer(Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = stringResource(id = R.string.signin_no_account),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    TextButton(
                        onClick = onSignUpClicked,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.signin_sign_up),
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 14.sp
                        )
                    }

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
fun FinalSignInScreenPreviewDark() {
    MovitoTheme(darkTheme = true) {
        SignInScreen(onSignInSuccess = {}, onSignUpClicked = {}, onForgotPasswordClicked = {})
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Light Mode Preview"
)
@Composable
fun FinalSignInScreenPreviewLight() {
    MovitoTheme(darkTheme = false) {
        SignInScreen(onSignInSuccess = {}, onSignUpClicked = {}, onForgotPasswordClicked = {})
    }
}
