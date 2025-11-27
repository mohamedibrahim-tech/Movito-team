package com.movito.movito.ui.common

// (1) --- إضافة: imports جديدة عشان الـ Intent ---
// ------------------------------------------
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.CategoriesActivity
import com.movito.movito.ui.FavoritesActivity
import com.movito.movito.ui.SearchActivity
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
        @Composable
        fun NavItem(
            route: String,
            selectedIcon: ImageVector,
            unselectedIcon: ImageVector,
            activity: Class<*>
        ) {
            NavigationBarItem(
                selected = selectedItem == route,
                onClick = { if (selectedItem != route) navigateToActivity(context, activity) },
                icon = {
                    Icon(
                        imageVector = if (selectedItem == route)
                            selectedIcon else unselectedIcon,
                        contentDescription = route,
                        modifier = Modifier.size((if (selectedItem == route) 32 else 24).dp)
                    )
                },
                colors = navBarColors()
            )
        }

        NavItem("home", Icons.Filled.Home, Icons.Outlined.Home, CategoriesActivity::class.java)
        NavItem("search", Icons.Filled.Search, Icons.Outlined.Search, SearchActivity::class.java)
        NavItem(
            "favorite",
            Icons.Filled.Favorite,
            Icons.Outlined.FavoriteBorder,
            FavoritesActivity::class.java
        )
        NavItem(
            "profile",
            Icons.Filled.Settings,
            Icons.Outlined.Settings,
            SettingsActivity::class.java
        )
    }
}

/*
   إضافة دالة مساعدة لتوحيد ألوان الـ NavBar
 */
@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer
)

/*
    إضافة دالة التنقل بتستخدم Intent عادي عشان تفتح Activity جديدة
 */
private val activityOrder = mapOf(
    CategoriesActivity::class.java to 1,
    SearchActivity::class.java to 2,
    FavoritesActivity::class.java to 3,
    SettingsActivity::class.java to 4
)

private fun navigateToActivity(context: Context, activityClass: Class<*>) {
    val currentActivity = context as? Activity ?: return

    // Don't reopen the same activity
    if (currentActivity::class.java == activityClass) return

    val intent = Intent(context, activityClass).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    currentActivity.startActivity(intent)

    // Determine forward/back animation
    val currentOrder = activityOrder[currentActivity::class.java] ?: 0
    val targetOrder = activityOrder[activityClass] ?: 0
    val forward = targetOrder > currentOrder

    currentActivity.overridePendingTransition(
        if (forward) R.anim.slide_in_right else R.anim.slide_in_left,
        if (forward) R.anim.slide_out_left else R.anim.slide_out_right
    )
}


@Preview("NavBar - Light Theme - All Tabs", showBackground = true, widthDp = 360)
@Composable
fun MovitoNavBarPreview_LightThemeAllTabs() {
    Column {
        listOf("home", "search", "favorite", "profile").forEach { tab ->
            Text(
                text = "Selected: $tab",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            MovitoNavBar(selectedItem = tab)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview("NavBar - Dark Theme - All Tabs", showBackground = true, widthDp = 360)
@Composable
fun MovitoNavBarPreview_DarkThemeAllTabs() {
    MovitoTheme(darkTheme = true) {
        Column {
            listOf("home", "search", "favorite", "profile").forEach { tab ->
                Text(
                    text = "Selected: $tab",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(8.dp)
                )
                MovitoNavBar(selectedItem = tab)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
