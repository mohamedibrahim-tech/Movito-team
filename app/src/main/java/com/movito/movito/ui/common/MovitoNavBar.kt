package com.movito.movito.ui.common

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
import com.movito.movito.MovitoApplication
import com.movito.movito.R
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.CategoriesActivity
import com.movito.movito.ui.FavoritesActivity
import com.movito.movito.ui.MoviesByGenreScreen
import com.movito.movito.ui.SearchActivity
import com.movito.movito.ui.SettingsActivity

/**
 * Main navigation bar component for the Movito application.
 *
 * This bottom app bar provides navigation between four main screens:
 * 1. **Home** ([CategoriesActivity]) - Browse movies by genre
 * 2. **Search** ([SearchActivity]) - Search for movies
 * 3. **Favorites** ([FavoritesActivity]) - View saved favorite movies
 * 4. **Profile/Settings** ([SettingsActivity]) - User settings and preferences
 *
 * Features:
 * - Consistent navigation across all main activities
 * - Visual feedback for selected screen
 * - RTL/LTR layout support with proper animations
 * - Smart activity management to prevent duplicate instances
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @param selectedItem The currently selected navigation item identifier.
 *                     Must be one of: `"home"`, `"search"`, `"favorite"`, `"profile"`
 *
 * @see NavigationBarItem
 * @see CategoriesActivity
 * @see SearchActivity
 * @see FavoritesActivity
 * @see SettingsActivity
 *
 * @since first appear in the [MoviesByGenreScreen] (which back then called HomeScreen) (8 Nov 2025), then moved to this file (13 Nov 2025)
 */
@Composable
fun MovitoNavBar(selectedItem: String) {
    // Get the current context for navigation intents
    val context = LocalContext.current

    BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
        /**
         * Reusable navigation item composable for each bottom bar entry.
         *
         * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
         *
         * @param route Unique identifier for this navigation destination
         * @param selectedIcon Icon to display when this item is selected
         * @param unselectedIcon Icon to display when this item is not selected
         * @param activity The Activity class to navigate to when this item is clicked
         * @since 26 Nov 2025
         */
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

        // Define the four navigation items
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

/**
 * Provides consistent color configuration for navigation bar items.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @return [NavigationBarItemDefaults.colors] with theme-appropriate colors
 *
 * @since 13 Nov 2025
 */
@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer
)

/**
 * Maps activity classes to their order in the navigation flow.
 * Used to determine appropriate transition animations.
 *
 * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
 *
 * @since 26 Nov 2025
 */
private val activityOrder = mapOf(
    CategoriesActivity::class.java to 1,
    SearchActivity::class.java to 2,
    FavoritesActivity::class.java to 3,
    SettingsActivity::class.java to 4
)

/**
 * Navigates to the specified activity with proper intent flags and animations.
 *
 * This function:
 * 1. Prevents re-opening the same activity
 * 2. Clears the activity stack appropriately
 * 3. Determines forward/backward navigation direction
 * 4. Applies RTL/LTR-aware slide animations
 * 5. Handles [Activity] lifecycle correctly
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @param context The current context (should be an [Activity])
 * @param activityClass The target activity class to navigate to
 *
 * @throws ClassCastException if context is not an [Activity]
 * @since 13 Nov 2025
 */
private fun navigateToActivity(context: Context, activityClass: Class<*>) {
    val currentActivity = context as? Activity ?: return

    // Don't reopen the same activity
    if (currentActivity::class.java == activityClass) return

    val intent = Intent(context, activityClass).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    currentActivity.startActivity(intent)

    // Determine forward/back animation based on navigation order
    val currentOrder = activityOrder[currentActivity::class.java] ?: 0
    val targetOrder = activityOrder[activityClass] ?: 0
    var forward = targetOrder > currentOrder

    // Simplified animation that works for both RTL and LTR
    val isArabic = MovitoApplication.getSavedLanguage(context) == "ar"

    if (isArabic) {
        forward = !forward
        // For RTL languages
        currentActivity.overridePendingTransition(
            if(forward) R.anim.slide_in_left else R.anim.slide_in_right,
            if(forward) R.anim.slide_out_right else  R.anim.slide_out_left
        )
    } else {
        // For LTR languages
        currentActivity.overridePendingTransition(
            if (forward) R.anim.slide_in_right else R.anim.slide_in_left,
            if (forward) R.anim.slide_out_left else R.anim.slide_out_right
        )
    }
}

/**
 * Preview function showing all navigation bar states in light theme.
 */
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

/**
 * Preview function showing all navigation bar states in dark theme.
 */
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