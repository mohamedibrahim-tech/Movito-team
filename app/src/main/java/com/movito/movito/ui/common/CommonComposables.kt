package com.movito.movito.ui.common

// (1) --- إضافة: import مكتبة Coil (لازم تضيفها في build.gradle) ---
// (2) --- تعديل: تغيير مسار الـ data class ---
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.movito.movito.R
import com.movito.movito.data.model.Movie
import com.movito.movito.theme.DarkButtonColor1
import com.movito.movito.theme.DarkButtonColor2
import com.movito.movito.theme.LightButtonColor1
import com.movito.movito.theme.LightButtonColor2

@Composable
fun MovieCard(
    modifier: Modifier = Modifier, movie: Movie, content: @Composable BoxScope.() -> Unit = {}
) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // (5) --- تعديل: استخدمنا AsyncImage (من مكتبة Coil) ---
            // ده عشان نحمل الصورة من الـ API (أو الرابط المؤقت)
            AsyncImage(
                model = movie.posterUrl, //  ده بقى بيستقبل String
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                //  صورة مؤقتة لحد ما الـ API يحمل
                placeholder = painterResource(id = R.drawable.poster_test)
            )

            // التدرج اللوني الأسود الشفاف
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black), startY = 400f
                        )
                    ), contentAlignment = Alignment.BottomStart
            ) {
                // أي حاجة انت ضفتها من برة زي زرار القلب
                content()

                // (الاسم والسنة والوقت
                Column(
                    //  تعديل: ضفت align عشان نضمن المكان
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = movie.year,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = " | ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = movie.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 *   كود الـ SettingsCards (منقول من SettingsActivity.kt)
 */
@Composable
fun SettingsCards(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun MovitoButton(
    text: String,
    modifier: Modifier = Modifier,
    roundedCornerSize: Dp = 12.dp,
    isDarkMode: Boolean = false,
    content: BoxScope.() -> Unit = {},
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(roundedCornerSize))
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (isDarkMode) listOf(LightButtonColor1, LightButtonColor2)
                    else listOf(DarkButtonColor1, DarkButtonColor2)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,  // new ripple API
                onClick = onClick
            ), contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium
        )
        content()
    }
}