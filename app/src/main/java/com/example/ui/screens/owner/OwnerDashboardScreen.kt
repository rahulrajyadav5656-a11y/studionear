package com.example.ui.screens.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.MockDataManager
import com.example.data.models.BookingStatus
import com.example.data.models.Studio
import com.example.di.ServiceLocator
import com.example.ui.theme.*
import kotlinx.coroutines.flow.map
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val userProfile = AuthManager.getUserProfile()
    val ownerId = AuthManager.getCurrentUser() ?: "default_owner"

    var currentStudio by remember {
        mutableStateOf(
            MockDataManager.studios.value.find { it.ownerId == ownerId }
        )
    }

    LaunchedEffect(ownerId) {
        try {
            val remoteStudio = ServiceLocator.studioRepository.getStudioByOwnerId(ownerId)
            if (remoteStudio != null) {
                currentStudio = remoteStudio
                ServiceLocator.studioRepository.getPackagesForStudio(remoteStudio.id)
            }
        } catch (e: Exception) {
            // fallback
        }
    }
    
    val allStudios by MockDataManager.studios.collectAsState()
    val allPackages by MockDataManager.packages.collectAsState()
    
    val s = currentStudio ?: allStudios.find { it.ownerId == ownerId } ?: Studio(
        id = ownerId,
        ownerId = ownerId,
        name = if (userProfile.name.isNotBlank()) "${userProfile.name}'s Studio" else "My Studio",
        city = "Prayagraj",
        area = "Civil Lines",
        isVerified = false,
        verified = false,
        rating = 0.0f,
        reviewCount = 0,
        startingPrice = 0.0,
        startingPackagePrice = 0.0
    )

    val studioPackages = allPackages.filter { it.studioId == s.id }
    val bookings by MockDataManager.bookings.collectAsState()
    val studioBookings = bookings.filter { it.studioId == s.id }
    
    val pendingCount = studioBookings.count { it.status == BookingStatus.PENDING }
    val upcomingCount = studioBookings.count { it.status == BookingStatus.ACCEPTED }
    val completedCount = studioBookings.count { it.status == BookingStatus.COMPLETED }
    
    val reviews by MockDataManager.reviews.collectAsState()
    val studioReviews = reviews.filter { it.studioId == s.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(s.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = if (s.area.isNotBlank()) "${s.area}, ${s.city}" else s.city,
                            fontSize = 12.sp,
                            color = ThemeOnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ThemeBackground,
                    titleContentColor = ThemeOnBackground
                ),
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ThemeOnSurfaceVariant)
                    }
                }
            )
        },
        containerColor = ThemeBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StudioStatusCard(s, onPromoteClick = { onNavigate("owner_monetization") })
            }
            item {
                // Growth & Verification Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("owner_monetization") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemePrimaryContainer.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = VerifiedBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (s.isVerifiedActive && s.isSponsoredActive) "VIP Studio Boost Active" else "Get Blue Tick & Top Ranking",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ThemeOnBackground
                                )
                                Text(
                                    text = if (s.isVerifiedActive) "Manage subscription & spotlight" else "Boost discovery by 300% in Prayagraj",
                                    fontSize = 12.sp,
                                    color = ThemeOnSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = ThemePrimary)
                    }
                }
            }
            item {
                DashboardStatsGrid(pendingCount, upcomingCount, completedCount, studioReviews.size)
            }
            item {
                Text("Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                DashboardActionItem("Promote & Verification", Icons.Default.Verified, "owner_monetization", onNavigate)
                DashboardActionItem("Studio Profile", Icons.Default.Edit, "owner_profile", onNavigate)
                DashboardActionItem("Packages & Pricing (${studioPackages.size})", Icons.Default.List, "owner_packages", onNavigate)
                DashboardActionItem("Reviews (${studioReviews.size})", Icons.Default.Star, "owner_reviews", onNavigate)
                DashboardActionItem("Client Management", Icons.Default.People, "owner_clients", onNavigate)
                DashboardActionItem("Earnings Summary", Icons.Default.AttachMoney, "owner_earnings", onNavigate)
                DashboardActionItem("Notifications", Icons.Default.Notifications, "owner_notifications", onNavigate)
                DashboardActionItem("Settings", Icons.Default.Settings, "owner_settings", onNavigate)
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StudioStatusCard(studio: Studio, onPromoteClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPromoteClick() },
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (studio.isVerifiedActive) {
                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = VerifiedBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verified Partner", color = VerifiedBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.Pending, contentDescription = "Pending", tint = Color(0xFFE5A93C))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get Verified Blue Tick", color = Color(0xFFE5A93C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    if (studio.isSponsoredActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE5A93C)
                        ) {
                            Text(
                                text = "SPONSORED",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = ThemePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "From ₹${(if (studio.startingPrice > 0.0) studio.startingPrice else studio.startingPackagePrice).toInt()}",
                        color = ThemePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = ThemeOnSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (studio.area.isNotBlank()) "${studio.area}, ${studio.city}" else studio.city,
                    color = ThemeOnSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "★ ${if (studio.rating > 0) studio.rating else "4.9"} (${if (studio.reviewCount > 0) studio.reviewCount else 156} reviews)",
                    color = Color(0xFFE5A93C),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun DashboardStatsGrid(pending: Int, upcoming: Int, completed: Int, reviews: Int) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Pending", pending.toString(), Modifier.weight(1f))
            StatCard("Upcoming", upcoming.toString(), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Completed", completed.toString(), Modifier.weight(1f))
            StatCard("Reviews", reviews.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ThemePrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = ThemeOnSurfaceVariant)
        }
    }
}

@Composable
fun DashboardActionItem(title: String, icon: ImageVector, route: String, onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onNavigate(route) },
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = ThemePrimary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Medium, color = ThemeOnBackground, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = ThemeOnSurfaceVariant)
        }
    }
}
