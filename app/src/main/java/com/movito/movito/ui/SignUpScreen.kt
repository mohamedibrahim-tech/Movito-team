package com.movito.movito.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: () -> Unit = {},
    onSignInClicked: () -> Unit = {},
    onGoogleSignUpClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current


    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.idToken?.let { token ->
                        authViewModel.signInWithGoogle(token)
                    }
                } catch (e: ApiException) {
                    authViewModel.resetState()
                }
            }
        }
    )


    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)


    LaunchedEffect(Unit) {
        authViewModel.navigationFlow.collect {
            onSignUpSuccess()
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
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            MovitoLogo()
            Spacer(Modifier.height(48.dp))

            Text(
                text = "Sign Up",
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
                    label = "Email",
                    icon = Icons.Default.Email
                )
                Spacer(Modifier.height(20.dp))

                CustomAuthTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        validationError = null
                    },
                    label = "Password",
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
                    label = "Confirm Password",
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF9D5BFF),
                                    Color(0xFF64DFDF)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "Continue with Google",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF9D5BFF),
                                    Color(0xFF64DFDF)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { validateAndSignUp() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sign Up",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }


            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }


            authState.error?.let {
                Text(
                    text = it,
                    color = Color(0xFF673AB7),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            authState.message?.let {
                Text(
                    text = it,
                    color = Color(0xFF673AB7),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Already have an account?",
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
                        text = "Login",
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
            onGoogleSignUpClicked = {}
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
            onGoogleSignUpClicked = {}
        )
    }
}
