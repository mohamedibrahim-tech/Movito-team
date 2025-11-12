package com.movito.movito.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview

// استيراد الألوان والـ R (تأكد من وجود هذه الملفات في المشروع)
import com.movito.movito.R
import com.movito.movito.theme.DarkBlueBackground
import com.movito.movito.theme.CardBackground
import com.movito.movito.theme.MovitoTheme
// يمكن ترك استيراد الألوان الخاصة بك، لكننا سنستخدم ألوانًا جديدة للتدرج لضمان النتيجة المطلوبة


// ----------------------------------------------------------------------------------
// ✅ تعريف الألوان الجديدة للتدرج (لضمان اللون البنفسجي/السماوي)
// ----------------------------------------------------------------------------------
val PurpleStart = Color(0xFF9C27B0) // أرجواني عميق
val CyanEnd = Color(0xFF00BCD4)      // سماوي (Cyan) واضح

val UniformButtonGradient: Brush = Brush.horizontalGradient(
    listOf(PurpleStart, CyanEnd) // ✅ التدرج من الأرجواني إلى السماوي
)

// ✅ توحيد لون رابط Sign In مع بداية التدرج (PurpleStart)
val SignInLinkColor = PurpleStart
// ----------------------------------------------------------------------------------


// ----------------------------------------------------------------------------------
// المكونات المساعدة
// ----------------------------------------------------------------------------------

@Composable
fun MovitoLogo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.movito_logo),
            contentDescription = "Movito Logo",
            modifier = Modifier.size(60.dp)
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

/**
 * دالة حقل الإدخال المخصص
 */
@Composable
fun CustomAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    // استخدام CardBackground (0xFF1C1B1F) للوضع الداكن
    val containerColor = if (isSystemInDarkTheme()) CardBackground else MaterialTheme.colorScheme.surface

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
        leadingIcon = { Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,

        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = containerColor,
            focusedContainerColor = containerColor,

            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

/**
 * دالة زر التدرج اللوني (Gradient Button)
 */
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
            .clip(RoundedCornerShape(28.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
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


// ----------------------------------------------------------------------------------
// الدالة الرئيسية لشاشة التسجيل (Sign Up Screen)
// ----------------------------------------------------------------------------------

@Composable
fun SignUpScreen(
    onSignUpClicked: () -> Unit = {},
    onGoogleSignUpClicked: () -> Unit = {},
    onSignInClicked: () -> Unit = {},
    isDarkTheme: Boolean = true
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // تحديد لون رابط Sign In بناءً على اللون الأرجواني الثابت
    val signInLinkColor = SignInLinkColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) DarkBlueBackground else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDarkTheme) CardBackground else MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            MovitoLogo()
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Sign Up",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(32.dp))

            // حقول الإدخال (3 حقول)
            CustomAuthTextField(
                value = email, onValueChange = { email = it }, label = "Email", icon = Icons.Default.Email
            )
            Spacer(Modifier.height(16.dp))
            CustomAuthTextField(
                value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock, isPassword = true
            )
            Spacer(Modifier.height(16.dp))
            CustomAuthTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password", icon = Icons.Default.Lock, isPassword = true
            )

            Spacer(Modifier.height(24.dp))

            // زر جوجل (باستخدام التدرج)
            GradientButton(
                text = "Continue with Google",
                onClick = onGoogleSignUpClicked,
                gradient = UniformButtonGradient
            )

            Spacer(Modifier.height(16.dp))

            // زر Sign Up الرئيسي (باستخدام التدرج)
            GradientButton(
                text = "Sign Up",
                onClick = onSignUpClicked,
                gradient = UniformButtonGradient
            )

            Spacer(Modifier.height(24.dp))

            // خيار Sign In (موحد اللون)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Already have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Sign In",
                    color = signInLinkColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onSignInClicked)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// دوال المعاينة (Preview) للوضع الداكن والفاتح
// ----------------------------------------------------------------------------------

@Preview(showBackground = true, showSystemUi = true, name = "Dark Mode Preview")
@Composable
fun SignUpScreenPreviewDark() {
    MovitoTheme(darkTheme = true) {
        SignUpScreen(isDarkTheme = true)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Light Mode Preview")
@Composable
fun SignUpScreenPreviewLight() {
    MovitoTheme(darkTheme = false) {
        SignUpScreen(isDarkTheme = false)
    }
}