import re

with open('app/src/main/java/com/example/ui/screens/owner/StudioOwnerMainScreen.kt', 'r') as f:
    content = f.read()

nav_str = """
            composable("owner_settings") {
                OwnerSettingsScreen(onBack = { navController.popBackStack() }, onLogoutClick = onLogoutClick)
            }
            composable("owner_booking_details/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                OwnerBookingDetailsScreen(bookingId = bookingId, onBack = { navController.popBackStack() })
            }
"""

content = content.replace("""            composable("owner_settings") {
                OwnerSettingsScreen(onBack = { navController.popBackStack() }, onLogoutClick = onLogoutClick)
            }""", nav_str)

with open('app/src/main/java/com/example/ui/screens/owner/StudioOwnerMainScreen.kt', 'w') as f:
    f.write(content)
