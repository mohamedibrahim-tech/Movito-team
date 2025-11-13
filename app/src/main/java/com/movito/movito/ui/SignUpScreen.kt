package com.movito.movito.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme


@Composable
fun MovitoLogo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.movito_logo),
            contentDescription = "Movito Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Movito",
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

        leadingIcon = { Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
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
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    gradient: Brush,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, shape = RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
fun SignUpScreen(
    onSignUpClicked: () -> Unit = {},
    onGoogleSignUpClicked: () -> Unit = {},
    onSignInClicked: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }



    Box(
        modifier = Modifier
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
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email
                )
                Spacer(Modifier.height(20.dp))

                CustomAuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )
                Spacer(Modifier.height(20.dp))

                CustomAuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )
            }

            Spacer(Modifier.height(40.dp))



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
                    .clickable {},
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
                    .clickable {},
                contentAlignment = Alignment.Center

            ) {
                Text(
                    "Sign Up",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

           

            Spacer(Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Don't have an account?",
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
    showBackground = false,
    showSystemUi = true,
    name = "Dark Mode Preview"
)
@Composable
fun FinalSignUpScreenPreviewDark() {
    MovitoTheme(darkTheme = true) {
        SignUpScreen()
    }
}

@Preview(
    showBackground = false,
    showSystemUi = true,
    name = "Light Mode Preview"
)
@Composable
fun FinalSignUpScreenPreviewLight() {
    MovitoTheme(darkTheme = false) {
        SignUpScreen()
    }
}