package com.movito.movito.ui.common

// (1) --- إضافة: imports جديدة عشان الـ Intent ---
import android.app.Activity
import android.content.Context
import android.content.Intent
// ------------------------------------------
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.CategoriesActivity
import com.movito.movito.ui.SearchActivity
import com.movito.movito.ui.FavoritesActivity
import com.movito.movito.ui.SettingsActivity

/**
 * (4) --- تعديل: تم إزالة دالة MovieCard من هنا ---
 * (اتنقلت لملف CommonComposables.kt)
 *
 * ده الـ BottomAppBar (شريط التنقل) المشترك بين كل الشاشات.
 * @param selectedItem هو اسم الشاشة اللي إحنا واقفين عليها ("home", "search", "favorite", "profile")
 */
@Composable
fun MovitoNavBar(selectedItem: String) {
    //  بنجيب الـ Context عشان نعرف نفتح Activity جديدة
    val context = LocalContext.current

    BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
        NavigationBarItem(
            selected = selectedItem == "home",
            // (8) --- تعديل: الـ onClick بقى بيستخدم Intent ---
            onClick = {
                if (selectedItem != "home") {
                    navigateToActivity(context, CategoriesActivity::class.java)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = navBarColors()
        )

        NavigationBarItem(
            selected = selectedItem == "search",
            onClick = {
                if (selectedItem != "search") {
                    // (10) --- هنا هنحط الـ Intent بتاع الـ SearchActivity ---
                    navigateToActivity(context, SearchActivity::class.java)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = navBarColors()
        )

        NavigationBarItem(
            selected = selectedItem == "favorite",
            onClick = {
                if (selectedItem != "favorite") {
                    // (12) --- هنا هنحط الـ Intent بتاع الـ FavoritesActivity ---
                    navigateToActivity(context, FavoritesActivity::class.java)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.like),
                    contentDescription = "Favorite",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = navBarColors()
        )

        //  ده زرار الـ Profile/Settings
        NavigationBarItem(
            selected = selectedItem == "profile",
            onClick = {
                if (selectedItem != "profile") {
                    //   هنا هنحط الـ Intent بتاع الـ SettingsActivity
                    navigateToActivity(context, SettingsActivity::class.java)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = navBarColors()
        )
    }
}

/*
   إضافة دالة مساعدة لتوحيد ألوان الـ NavBar
 */
@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = Color.Transparent
)

/*
    إضافة دالة التنقل بتستخدم Intent عادي عشان تفتح Activity جديدة
 */
private fun navigateToActivity(context: Context, activityClass: Class<*>) {
    val intent = Intent(context, activityClass)
    // بيمنع التطبيق إنه يفتح 10 شاشات فوق بعض لو فضلت تدوس على الأيقونات
    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    context.startActivity(intent)

/*     أنيميشن التنقل
     بيلغي الوميض (flicker) اللي بيحصل بين الـ Activities*/
    if (context is Activity) {
        context.overridePendingTransition(0, 0)
    }
}


@Preview(name = "Movito Nav Bar Preview")
@Composable
fun MovitoNavBarPreview() {
    MovitoTheme(darkTheme = true) {
        MovitoNavBar("home")
    }
}

//  الـ Preview بتاع الـ MovieCard
