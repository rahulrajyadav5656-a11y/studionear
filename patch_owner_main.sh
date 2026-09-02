cat << 'INNEREOF' > app/src/main/java/com/example/ui/screens/owner/StudioOwnerMainScreen.kt
package com.example.ui.screens.owner

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.ThemeBackground
import com.example.ui.theme.ThemeOnSurfaceVariant
import com.example.ui.theme.ThemePrimary

sealed class OwnerBottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : OwnerBottomNavItem("owner_dashboard", Icons.Default.Dashboard, "Dashboard")
    object Bookings : OwnerBottomNavItem("owner_bookings", Icons.Default.Book, "Bookings")
    object Calendar : OwnerBottomNavItem("owner_calendar", Icons.Default.CalendarMonth, "Calendar")
    object Portfolio : OwnerBottomNavItem("owner_portfolio", Icons.Default.PhotoAlbum, "Portfolio")
    object Profile : OwnerBottomNavItem("owner_profile", Icons.Default.Person, "Profile")
}

@Composable
fun StudioOwnerMainScreen(
    onLogoutClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ThemeBackground,
                contentColor = ThemePrimary
            ) {
                val items = listOf(
                    OwnerBottomNavItem.Dashboard,
                    OwnerBottomNavItem.Bookings,
                    OwnerBottomNavItem.Calendar,
                    OwnerBottomNavItem.Portfolio,
                    OwnerBottomNavItem.Profile
                )
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ThemePrimary,
                            selectedTextColor = ThemePrimary,
                            unselectedIconColor = ThemeOnSurfaceVariant,
                            unselectedTextColor = ThemeOnSurfaceVariant,
                            indicatorColor = ThemePrimary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        containerColor = ThemeBackground
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = OwnerBottomNavItem.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(OwnerBottomNavItem.Dashboard.route) {
                OwnerDashboardScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    onLogoutClick = onLogoutClick
                )
            }
            composable(OwnerBottomNavItem.Bookings.route) {
                OwnerBookingsScreen(onNavigateToBooking = { id -> navController.navigate("owner_booking_details/$id") })
            }
            composable(OwnerBottomNavItem.Calendar.route) {
                OwnerCalendarScreen()
            }
            composable(OwnerBottomNavItem.Portfolio.route) {
                OwnerPortfolioScreen()
            }
            composable(OwnerBottomNavItem.Profile.route) {
                OwnerProfileScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    onBack = { if (navController.previousBackStackEntry != null) navController.popBackStack() }
                )
            }
            // Sub-screens
            composable("owner_monetization") {
                OwnerMonetizationScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_packages") {
                OwnerPackagesScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_reviews") {
                OwnerReviewsScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_clients") {
                OwnerClientsScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_earnings") {
                OwnerEarningsScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_notifications") {
                OwnerNotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_settings") {
                OwnerSettingsScreen(
                    onBack = { navController.popBackStack() }, 
                    onLogoutClick = onLogoutClick,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable("owner_edit_profile") {
                com.example.ui.screens.EditProfileScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_booking_details/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                OwnerBookingDetailsScreen(bookingId = bookingId, onBack = { navController.popBackStack() })
            }
        }
    }
}
INNEREOF
