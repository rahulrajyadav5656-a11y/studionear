package com.example.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.di.ServiceLocator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPackages: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val ownerId = AuthManager.getCurrentUser() ?: ""
    val firestore = remember { FirebaseFirestore.getInstance() }

    var studioName by remember { mutableStateOf("My Studio") }
    var studioLocation by remember { mutableStateOf("Prayagraj") }
    var pendingBookingsCount by remember { mutableIntStateOf(0) }
    var upcomingBookingsCount by remember { mutableIntStateOf(0) }
    var totalReviewsCount by remember { mutableIntStateOf(0) }
    var packagesCount by remember { mutableIntStateOf(0) }

    // Fetch studio info & summary live from Firestore
    LaunchedEffect(ownerId) {
        if (ownerId.isNotBlank()) {
            // Get Studio Details
            firestore.collection("studios").whereEqualTo("ownerId", ownerId).limit(1).get()
                .addOnSuccessListener { snap ->
                    val doc = snap.documents.firstOrNull()
                    if (doc != null) {
                        studioName = doc.getString("name") ?: "Sunil Studio"
                        studioLocation = doc.getString("city") ?: doc.getString("address") ?: "Civil Lines, Prayagraj"
                        val currentStudioId = doc.id

                        // Count packages
                        firestore.collection("studios").document(currentStudioId).collection("packages").get()
                            .addOnSuccessListener { pSnap ->
                                packagesCount = pSnap.size()
                            }

                        // Count reviews
                        firestore.collection("studios").document(currentStudioId).collection("reviews").get()
                            .addOnSuccessListener { rSnap ->
                                totalReviewsCount = rSnap.size()
                            }
                    }
                }

            // Count Bookings
            firestore.collection("bookings")
                .whereEqualTo("studioId", ownerId)
                .get()
                .addOnSuccessListener { bSnap ->
                    var pending = 0
                    var upcoming = 0
                    for (d in bSnap.documents) {
                        val status = d.getString("status") ?: ""
                        if (status.equals("PENDING", ignoreCase = true)) pending++
                        if (status.equals("ACCEPTED", ignoreCase = true) || status.equals("CONFIRMED", ignoreCase = true)) upcoming++
                    }
                    pendingBookingsCount = pending
                    upcomingBookingsCount = upcoming
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(studioName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text(studioLocation, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color(0xFFEF4444))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13131A))
            )
        },
        containerColor = Color(0xFF0F0F14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Studio Quick Stats (Sirf Zaroori Cheezein)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatSimpleBox("Pending", pendingBookingsCount.toString(), Color(0xFFF59E0B), Modifier.weight(1f))
                StatSimpleBox("Upcoming", upcomingBookingsCount.toString(), Color(0xFF10B981), Modifier.weight(1f))
                StatSimpleBox("Reviews", totalReviewsCount.toString(), Color(0xFF8B5CF6), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("MANAGEMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Studio Profile
            OwnerSimpleMenuItem(
                title = "Studio Profile",
                subtitle = "Manage studio name, contact & location",
                icon = Icons.Default.Storefront,
                onClick = onNavigateToProfile
            )

            // 2. Packages & Pricing
            OwnerSimpleMenuItem(
                title = "Packages & Pricing",
                subtitle = if (packagesCount > 0) "$packagesCount active packages" else "Add rates & deliverables",
                icon = Icons.Default.Sell,
                badge = if (packagesCount > 0) "$packagesCount" else null,
                onClick = onNavigateToPackages
            )

            // 3. Client Reviews
            OwnerSimpleMenuItem(
                title = "Client Reviews",
                subtitle = "Read customer feedback & ratings",
                icon = Icons.Default.Star,
                badge = if (totalReviewsCount > 0) "$totalReviewsCount" else null,
                onClick = onNavigateToReviews
            )

            // 4. Notifications
            OwnerSimpleMenuItem(
                title = "Notifications",
                subtitle = "New inquiry & booking alerts",
                icon = Icons.Default.Notifications,
                onClick = onNavigateToNotifications
            )

            // 5. Settings
            OwnerSimpleMenuItem(
                title = "Settings",
                subtitle = "App preferences & account",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun StatSimpleBox(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun OwnerSimpleMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF242436), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }

            if (badge != null) {
                Surface(
                    color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        badge,
                        color = Color(0xFF8B5CF6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
        }
    }
}
