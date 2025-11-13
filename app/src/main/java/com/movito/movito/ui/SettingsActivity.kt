package com.movito.movito.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
//  إضافة: imports للـ Scroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.common.MovitoNavBar
import com.movito.movito.ui.common.SettingsCards

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            MovitoTheme(darkTheme = isDarkMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    // شيلنا الـ state بتاع selectedItem واستدعينا الـ NavBar الجديد
                    bottomBar = {
                        MovitoNavBar(selectedItem = "profile")
                    }
                ) { paddingValues ->
                    SettingsScreen(
                        modifier = Modifier.padding(paddingValues),
                        onThemeToggle = { isDarkMode = it },
                        currentThemeIsDark = isDarkMode
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
    currentThemeIsDark: Boolean
) {
    var notifications by remember { mutableStateOf(false) }
    var downloadsWifiOnly by remember { mutableStateOf(true) }


    Column(
        modifier = modifier //  ضفنا الـ Scroll
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp) //  خليت الـ Padding أفقي بس
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


        //  بقى بيستدعي الcard المشترك
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
                    .clickable {},
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
                "Version: 1.0.0",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Github Repository",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {/*code*/ }
            )
        }
        //  مسافة تحت عشان الـ Scroll تعديل للشكل
        Spacer(Modifier.height(16.dp))
    }

}

//هنا انا شلت الfunction  بتاعت SettingsCards

//  الـ Preview مبقاش فيه Scaffold
// (عشان نعرض الشاشة بس، والـ BottomBar بقى في الـ Activity)
@Preview(showSystemUi = true, name = "Dark Mode")
@Composable
fun SettingsPreviewDark() {
    var isDark by remember { mutableStateOf(true) }
    MovitoTheme(darkTheme = isDark) {
        //  شيلت الـ Scaffold من هنا
        SettingsScreen(
            onThemeToggle = { isDark = it },
            currentThemeIsDark = isDark
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
            currentThemeIsDark = isDark
        )
    }
}