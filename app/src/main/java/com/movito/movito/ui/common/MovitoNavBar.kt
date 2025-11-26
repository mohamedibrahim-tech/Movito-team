package com.movito.movito.ui.common

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.movito.movito.theme.MovitoTheme
import com.movito.movito.ui.navigation.Screen

@Composable
fun MovitoNavBar(navController: NavController, selectedItem: String) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
        @Composable
        fun NavItem(
            label: String,
            screen: Screen,
            selectedIcon: ImageVector,
            unselectedIcon: ImageVector
        ) {
            NavigationBarItem(
                selected = selectedItem == label,
                onClick = {
                    if (selectedItem != label) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selectedItem == label) selectedIcon else unselectedIcon,
                        contentDescription = label,
                        modifier = Modifier.size((if (selectedItem == label) 32 else 24).dp)
                    )
                },
                colors = navBarColors()
            )
        }

        NavItem(
            label = "home",
            screen = Screen.Categories,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        )
        NavItem(
            label = "search",
            screen = Screen.Search,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search
        )
        NavItem(
            label = "favorite",
            screen = Screen.Favorites,
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder
        )
        NavItem(
            label = "profile",
            screen = Screen.Settings,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    }
}

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer
)

@Preview("NavBar - Light Theme - All Tabs", showBackground = true, widthDp = 360)
@Composable
fun MovitoNavBarPreview_LightThemeAllTabs() {
    val navController = rememberNavController()
    Column {
        listOf("home", "search", "favorite", "profile").forEach { tab ->
            Text(
                text = "Selected: $tab",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            MovitoNavBar(navController = navController, selectedItem = tab)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview("NavBar - Dark Theme - All Tabs", showBackground = true, widthDp = 360)
@Composable
fun MovitoNavBarPreview_DarkThemeAllTabs() {
    val navController = rememberNavController()
    MovitoTheme(darkTheme = true) {
        Column {
            listOf("home", "search", "favorite", "profile").forEach { tab ->
                Text(
                    text = "Selected: $tab",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(8.dp)
                )
                MovitoNavBar(navController = navController, selectedItem = tab)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
