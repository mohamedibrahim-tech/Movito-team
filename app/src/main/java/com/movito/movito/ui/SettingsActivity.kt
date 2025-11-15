package com.movito.movito.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movito.movito.BuildConfig
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.ui.common.SettingsCards
import com.movito.movito.viewmodel.AuthViewModel

class SettingsActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemIsDark = isSystemInDarkTheme()
            val authState by authViewModel.authState.collectAsState()

            LaunchedEffect(authState.user) {
                if (authState.user == null && authState.isInitialCheckDone) { // To avoid navigation on initial load
                    val intent = Intent(this@SettingsActivity, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            MovitoTheme(darkTheme = systemIsDark) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        MovitoNavBar(selectedItem = "profile")
                    }
                ) { paddingValues ->
                    SettingsScreen(
                        modifier = Modifier.padding(paddingValues),
                        onThemeToggle = {},
                        currentThemeIsDark = systemIsDark,
                        onSignOut = { authViewModel.signOut() }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit,
    currentThemeIsDark: Boolean,
    onSignOut: () -> Unit
) {
    var notifications by remember { mutableStateOf(false) }
    var downloadsWifiOnly by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val githubUrl = "https://github.com/mohamedibrahim-tech/Movito-team/"


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Profile",
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }


        Spacer(Modifier.height(20.dp))


        SettingsCards {
            Text(
                text = "Account",
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {},
                verticalAlignment = Alignment.CenterVertically

            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Profile info",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = "user@gmail.com",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
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
                    .clickable { onSignOut() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sign Out",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        SettingsCards {
            Text(
                "Appearance",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Theme Mode",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 20.sp
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = currentThemeIsDark,
                    onCheckedChange = onThemeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                )
            }

        }
        Spacer(Modifier.height(20.dp))
        SettingsCards {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Notifications",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = notifications,
                    onCheckedChange = { notifications = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        SettingsCards {
            Text(
                "Downloads",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "WiFi only",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 20.sp
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = downloadsWifiOnly,
                    onCheckedChange = { downloadsWifiOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        SettingsCards {
            Text(
                "About",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Version: ${BuildConfig.VERSION_NAME}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Github Repository",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                    context.startActivity(intent)
                }
            )
        }
        Spacer(Modifier.height(16.dp))
    }

}

@Preview(showSystemUi = true, name = "Dark Mode")
@Composable
fun SettingsPreviewDark() {
    var isDark by remember { mutableStateOf(true) }
    MovitoTheme(darkTheme = isDark) {
        SettingsScreen(
            onThemeToggle = { isDark = it },
            currentThemeIsDark = isDark,
            onSignOut = {}
        )
    }
}

@Preview(showSystemUi = true, name = "Light Mode")
@Composable
fun SettingsPreviewLight() {
    var isDark by remember { mutableStateOf(false) }
    MovitoTheme(darkTheme = isDark) {
        SettingsScreen(
            onThemeToggle = { isDark = it },
            currentThemeIsDark = isDark,
            onSignOut = {}
        )
    }
}
