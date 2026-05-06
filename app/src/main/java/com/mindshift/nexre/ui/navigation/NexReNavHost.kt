package com.mindshift.nexre.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindshift.nexre.ui.detail.DetailScreen
import com.mindshift.nexre.ui.home.HomeScreen
import com.mindshift.nexre.ui.library.LibraryScreen
import com.mindshift.nexre.ui.onboarding.OnboardingScreen
import com.mindshift.nexre.ui.search.SearchScreen
import com.mindshift.nexre.ui.settings.SettingsScreen
import com.mindshift.nexre.ui.tags.TagsScreen

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    NavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem("library", "Library", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    NavItem("tags", "Topics", Icons.Filled.Sell, Icons.Outlined.Sell),
    NavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun NexReNavHost(showOnboarding: Boolean, onOnboardingDone: () -> Unit) {
    val navController = rememberNavController()

    if (showOnboarding) {
        OnboardingScreen(onDone = onOnboardingDone)
        return
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDest = navBackStackEntry?.destination
            val showBottomBar = bottomNavItems.any { item ->
                currentDest?.hierarchy?.any { it.route == item.route } == true
            }
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (selected) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable("home") {
                HomeScreen(
                    onLinkClick = { link -> navController.navigate("detail/${link.id}") },
                    onSearchClick = { navController.navigate("search") },
                )
            }
            composable("library") {
                LibraryScreen(onLinkClick = { link -> navController.navigate("detail/${link.id}") })
            }
            composable("library?tag={tag}", arguments = listOf(navArgument("tag") { nullable = true })) { entry ->
                val tag = entry.arguments?.getString("tag")
                LibraryScreen(
                    onLinkClick = { link -> navController.navigate("detail/${link.id}") },
                    tagFilter = tag,
                )
            }
            composable("tags") {
                TagsScreen(onTagClick = { tag -> navController.navigate("library?tag=$tag") })
            }
            composable("settings") { SettingsScreen() }
            composable("search") {
                SearchScreen(
                    onLinkClick = { link -> navController.navigate("detail/${link.id}") },
                    onClose = { navController.popBackStack() },
                )
            }
            composable(
                "detail/{linkId}",
                arguments = listOf(navArgument("linkId") { type = NavType.StringType }),
            ) {
                DetailScreen(
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() },
                )
            }
        }
    }
}
