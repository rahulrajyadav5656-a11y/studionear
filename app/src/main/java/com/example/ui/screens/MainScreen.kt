package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

sealed class MainTab(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Home : MainTab("home", "Home", { Icon(Icons.Default.Home, contentDescription = "Home") })
    object Search : MainTab("search", "Search", { Icon(Icons.Outlined.Search, contentDescription = "Search") })
    object Bookings : MainTab("bookings", "Bookings", { Icon(Icons.Default.Movie, contentDescription = "Bookings") })
    object Favorites : MainTab("favorites", "Favorites", { Icon(Icons.Default.Favorite, contentDescription = "Favorites") })
    object Profile : MainTab("profile", "Profile", { Icon(Icons.Default.Person, contentDescription = "Profile") })
}

@Composable
fun MainScreen(
    onStudioClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onReviewBooking: (String, String, String) -> Unit = { _, _, _ -> },
    onProfileMenuClick: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val tabs = listOf(MainTab.Home, MainTab.Search, MainTab.Bookings, MainTab.Favorites, MainTab.Profile)
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ThemeSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, ThemeOutline)
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(MainTab.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = tab.icon,
                        label = { 
                            Text(
                                text = tab.title, 
                                fontWeight = if (currentRoute == tab.route) FontWeight.Bold else FontWeight.Normal 
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ThemePrimary,
                            selectedTextColor = ThemePrimary,
                            indicatorColor = ThemePrimaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.Home.route) {
                // We pass empty padding because MainScreen handles it
                HomeScreen(
                    onStudioClick = onStudioClick,
                    onNotificationClick = onNotificationClick,
                    onCategoryClick = onCategoryClick
                )
            }
            composable(MainTab.Search.route) {
                SearchScreen(
                    onStudioClick = onStudioClick
                )
            }
            composable(MainTab.Bookings.route) {
                MyBookingsScreen(
                    onReviewBooking = onReviewBooking,
                    onBookingClick = { bookingId ->
                        navController.navigate(Screen.BookingDetails.createRoute(bookingId))
                    }
                )
            }
            composable(MainTab.Favorites.route) {
                com.example.ui.screens.FavoritesScreen(
                    onStudioClick = { studioId ->
                        onStudioClick(studioId)
                    },
                    onSearchClick = {
                        navController.navigate(MainTab.Search.route) {
                            popUpTo(MainTab.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(MainTab.Profile.route) {
                ProfileScreen(
                    onLogoutClick = onLogoutClick,
                    onNavigateToMenu = { menu ->
                        when(menu) {
                            "My Bookings" -> {
                                navController.navigate(MainTab.Bookings.route) {
                                    popUpTo(MainTab.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            "Favorites" -> {
                                navController.navigate(MainTab.Favorites.route) {
                                    popUpTo(MainTab.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            else -> onProfileMenuClick(menu)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
