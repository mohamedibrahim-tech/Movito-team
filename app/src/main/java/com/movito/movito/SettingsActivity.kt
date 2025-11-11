package com.movito.movito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.movito.movito.ui.theme.MovitoTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovitoTheme {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    var isDarkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(false) }
    var downloadsWifiOnly by remember { mutableStateOf(true) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F))
            .padding(16.dp)
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
                    color = Color.White
                )
            }
        }


        Spacer(Modifier.height(20.dp))


        SettingsCards {
            Text(
                text = "Account",
                fontSize = 28.sp,
                color = Color.White
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
                        color = Color.White,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = "user@gmail.com",
                        color = Color.Gray,
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
                color = Color.White,
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
                    color = Color.Gray,
                    fontSize = 20.sp
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF64DFDF),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF374151)
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
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = notifications,
                    onCheckedChange = { notifications = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF64DFDF),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF374151)
                    )
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        SettingsCards {
            Text(
                "Downloads",
                color = Color.White,
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
                    color = Color.Gray,
                    fontSize = 20.sp
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = downloadsWifiOnly,
                    onCheckedChange = { downloadsWifiOnly = it }
                )

            }
        }
        Spacer(Modifier.height(20.dp))
        SettingsCards {
            Text(
                "About",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Version: 1.0.0",
                color = Color(0xFF6B7280),
                fontSize = 20.sp
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Github Repository",
                color = Color(0xFF64DFDF),
                fontSize = 20.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {/*code*/ }
            )
        }
    }

}
@Composable
fun SettingsCards(content: @Composable ColumnScope.() -> Unit){
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11182A)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun GreetingPreview() {
    MovitoTheme {
        SettingsScreen()
    }
}