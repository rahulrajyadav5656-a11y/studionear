package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AuthManager
import com.example.ui.theme.ThemeBackground
import com.example.ui.theme.ThemePrimary
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StudioProfileScreen
import com.example.ui.screens.BookingFlowScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PackageScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.CategoryResultsScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Registration : Screen("registration")
    object Main : Screen("main")
    object StudioProfile : Screen("studio_profile/{studioId}") {
        fun createRoute(studioId: String) = "studio_profile/$studioId"
    }
    object BookingFlow : Screen("booking_flow/{studioId}/{packageId}") {
        fun createRoute(studioId: String, packageId: String) = "booking_flow/$studioId/$packageId"
    }
    object BookingConfirmation : Screen("booking_confirmation/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_confirmation/$bookingId"
    }
    object Invoice : Screen("invoice/{bookingId}") {
        fun createRoute(bookingId: String) = "invoice/$bookingId"
    }
    object Terms : Screen("terms/{bookingId}") {
        fun createRoute(bookingId: String) = "terms/$bookingId"
    }
    object PackageDetails : Screen("package_details/{packageId}") {
        fun createRoute(packageId: String) = "package_details/$packageId"
    }
    object Delivery : Screen("delivery")
    object Complaint : Screen("complaint")
    object Notifications : Screen("notifications")
    object CategoryResults : Screen("category_results/{categoryName}") {
        fun createRoute(categoryName: String) = "category_results/$categoryName"
    }
    
    object ReviewSubmission : Screen("review_submission/{bookingId}/{studioId}/{studioName}") {
        fun createRoute(bookingId: String, studioId: String, studioName: String) = "review_submission/$bookingId/$studioId/$studioName"
    }

    object BookingDetails : Screen("booking_details/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_details/$bookingId"
    }

    object EditProfile : Screen("edit_profile")
    object Payments : Screen("payments")
    object Agreements : Screen("agreements")
    object Deliveries : Screen("deliveries")
    object Complaints : Screen("complaints")
    object MyReviews : Screen("my_reviews")
    object NotificationSettings : Screen("notification_settings")
    object StudioOwnerMain : Screen("studio_owner_main")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    if (com.example.data.AuthManager.isLoggedIn()) {
                        val role = com.example.data.AuthManager.getCurrentRole()
                        val route = if (role.equals("STUDIO_OWNER", ignoreCase = true)) Screen.StudioOwnerMain.route else Screen.Main.route
                        navController.navigate(route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (com.example.data.AuthManager.hasCompletedOnboarding()) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            com.example.ui.screens.LoginScreen(
                onLoginSuccess = { role ->
                    val route = if (role.equals("STUDIO_OWNER", ignoreCase = true)) Screen.StudioOwnerMain.route else Screen.Main.route
                    navController.navigate(route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(Screen.Registration.route)
                }
            )
        }

        composable(Screen.Registration.route) {
            RegistrationScreen(
                onRegistrationSuccess = { role ->
                    val route = if (role.equals("STUDIO_OWNER", ignoreCase = true)) Screen.StudioOwnerMain.route else Screen.Main.route
                    navController.navigate(route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Registration.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(
                onStudioClick = { studioId ->
                    navController.navigate(Screen.StudioProfile.createRoute(studioId))
                },
                onNotificationClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onCategoryClick = { categoryName ->
                    navController.navigate(Screen.CategoryResults.createRoute(categoryName))
                },
                onLogoutClick = {
                    com.example.data.AuthManager.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onReviewBooking = { bookingId, studioId, studioName ->
                    navController.navigate(Screen.ReviewSubmission.createRoute(bookingId, studioId, studioName))
                },
                onProfileMenuClick = { menu ->
                    when (menu) {
                        "Edit Profile" -> navController.navigate(Screen.EditProfile.route)
                        "Payments" -> navController.navigate(Screen.Payments.route)
                        "Agreements" -> navController.navigate(Screen.Agreements.route)
                        "Deliveries" -> navController.navigate(Screen.Deliveries.route)
                        "Complaints" -> navController.navigate(Screen.Complaints.route)
                        "Reviews" -> navController.navigate(Screen.MyReviews.route)
                        "Notification Settings" -> navController.navigate(Screen.NotificationSettings.route)
                    }
                }
            )
        }
        
        composable(Screen.StudioOwnerMain.route) {
            val context = LocalContext.current
            val isLoggedIn = AuthManager.isLoggedIn()
            val role = AuthManager.getCurrentRole()
            
            if (!isLoggedIn || !role.equals("STUDIO_OWNER", ignoreCase = true)) {
                LaunchedEffect(Unit) {
                    Toast.makeText(
                        context,
                        "Access restricted to registered Studio Owners.",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ThemeBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ThemePrimary)
                }
            } else {
                com.example.ui.screens.owner.StudioOwnerMainScreen(
                    onLogoutClick = {
                        AuthManager.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.CategoryResults.route) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: return@composable
            CategoryResultsScreen(
                categoryName = categoryName,
                onBack = { navController.popBackStack() },
                onStudioClick = { studioId ->
                    navController.navigate(Screen.StudioProfile.createRoute(studioId))
                }
            )
        }
        
        composable(Screen.StudioProfile.route) { backStackEntry ->
            val studioId = backStackEntry.arguments?.getString("studioId") ?: return@composable
            StudioProfileScreen(
                studioId = studioId,
                onBack = { navController.popBackStack() },
                onBookPackage = { packageId ->
                    // Usually you view package details first
                    navController.navigate(Screen.PackageDetails.createRoute(packageId))
                }
            )
        }
        
        composable(Screen.PackageDetails.route) { backStackEntry ->
            val packageId = backStackEntry.arguments?.getString("packageId") ?: return@composable
            PackageScreen(
                packageId = packageId,
                onBack = { navController.popBackStack() },
                onBookPackage = {
                    navController.navigate(Screen.BookingFlow.createRoute("studio_1", packageId))
                }
            )
        }
        
        composable(Screen.BookingFlow.route) { backStackEntry ->
            val studioId = backStackEntry.arguments?.getString("studioId") ?: return@composable
            val packageId = backStackEntry.arguments?.getString("packageId") ?: return@composable
            
            BookingFlowScreen(
                studioId = studioId,
                packageId = packageId,
                onBack = { navController.popBackStack() },
                onBookingComplete = { newBookingId ->
                    navController.navigate(Screen.BookingConfirmation.createRoute(newBookingId)) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.BookingConfirmation.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
            com.example.ui.screens.BookingConfirmationScreen(
                bookingId = bookingId,
                onViewInvoice = { navController.navigate(Screen.Invoice.createRoute(bookingId)) },
                onViewTerms = { navController.navigate(Screen.Terms.createRoute(bookingId)) },
                onGoHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Invoice.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
            com.example.ui.screens.InvoiceScreen(
                bookingId = bookingId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Terms.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
            com.example.ui.screens.TermsScreen(
                bookingId = bookingId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ReviewSubmission.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
            val studioId = backStackEntry.arguments?.getString("studioId") ?: return@composable
            val studioName = backStackEntry.arguments?.getString("studioName") ?: return@composable
            
            com.example.ui.screens.ReviewSubmissionScreen(
                bookingId = bookingId,
                studioId = studioId,
                studioName = studioName,
                onBack = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.BookingDetails.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
            com.example.ui.screens.BookingDetailsScreen(
                bookingId = bookingId,
                onBack = { navController.popBackStack() },
                onViewInvoice = { navController.navigate(Screen.Invoice.createRoute(bookingId)) },
                onViewTerms = { navController.navigate(Screen.Terms.createRoute(bookingId)) },
                onViewPayments = { navController.navigate(Screen.Payments.route) },
                onViewDeliveries = { navController.navigate(Screen.Deliveries.route) },
                onViewComplaints = { navController.navigate(Screen.Complaints.route) },
                onReviewBooking = { bId, sId, sName ->
                    navController.navigate(Screen.ReviewSubmission.createRoute(bId, sId, sName))
                }
            )
        }
        
        composable(Screen.EditProfile.route) {
            com.example.ui.screens.EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Payments.route) {
            com.example.ui.screens.PaymentsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Agreements.route) {
            com.example.ui.screens.AgreementsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Deliveries.route) {
            com.example.ui.screens.DeliveriesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Complaints.route) {
            com.example.ui.screens.ComplaintsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MyReviews.route) {
            com.example.ui.screens.MyReviewsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NotificationSettings.route) {
            com.example.ui.screens.NotificationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
