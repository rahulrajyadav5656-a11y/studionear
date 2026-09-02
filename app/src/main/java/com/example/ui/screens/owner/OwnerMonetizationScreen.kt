package com.example.ui.screens.owner

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.MockDataManager
import com.example.data.models.Studio
import com.example.di.ServiceLocator
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerMonetizationScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ownerId = AuthManager.getCurrentUser() ?: "default_owner"
    
    val allStudios by MockDataManager.studios.collectAsState()
    var currentStudio by remember {
        mutableStateOf(
            allStudios.find { it.ownerId == ownerId } ?: MockDataManager.getStudioById(ownerId)
        )
    }

    LaunchedEffect(ownerId) {
        try {
            val remote = ServiceLocator.studioRepository.getStudioByOwnerId(ownerId)
            if (remote != null) {
                currentStudio = remote
            }
        } catch (e: Exception) {
            // Local fallback
        }
    }

    val s = currentStudio ?: allStudios.find { it.ownerId == ownerId } ?: Studio(
        id = ownerId,
        ownerId = ownerId,
        name = "My Studio"
    )

    var isUpdating by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promote & Verification", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ThemeBackground,
                    titleContentColor = ThemeOnBackground
                )
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
                Spacer(modifier = Modifier.height(4.dp))
                // Active Status Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeOutline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "CURRENT MONETIZATION STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Blue Tick Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = if (s.isVerifiedActive) VerifiedBlue else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Blue Tick Verification",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = ThemeOnBackground
                                    )
                                    val verifiedExpiryText = when {
                                        !s.isVerifiedActive -> "Not Active / Expired"
                                        s.subscriptionExpiresAt == null -> "Active (Lifetime)"
                                        else -> "Expires: ${dateFormat.format(Date(s.subscriptionExpiresAt))}"
                                    }
                                    Text(
                                        text = verifiedExpiryText,
                                        fontSize = 12.sp,
                                        color = if (s.isVerifiedActive) VerifiedBlue else Color.Gray
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (s.isVerifiedActive) VerifiedBlue.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (s.isVerifiedActive) "ACTIVE" else "INACTIVE",
                                    color = if (s.isVerifiedActive) VerifiedBlue else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = ThemeOutline)

                        // Sponsored Spotlight Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Sponsored",
                                    tint = if (s.isSponsoredActive) Color(0xFFE5A93C) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Sponsored Spotlight",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = ThemeOnBackground
                                    )
                                    val sponsoredExpiryText = when {
                                        !s.isSponsoredActive -> "Not Active / Expired"
                                        s.sponsoredExpiresAt == null -> "Active (Lifetime)"
                                        else -> "Expires: ${dateFormat.format(Date(s.sponsoredExpiresAt))}"
                                    }
                                    Text(
                                        text = sponsoredExpiryText,
                                        fontSize = 12.sp,
                                        color = if (s.isSponsoredActive) Color(0xFFE5A93C) else Color.Gray
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (s.isSponsoredActive) Color(0xFFE5A93C).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (s.isSponsoredActive) "BOOSTED" else "INACTIVE",
                                    color = if (s.isSponsoredActive) Color(0xFFE5A93C) else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Benefits Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemePrimaryContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(ThemePrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ThemePrimary)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Get Up to 4x More Inquiries",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ThemeOnBackground
                            )
                            Text(
                                text = "Verified & Sponsored studios rank at the top of client search and category listings.",
                                fontSize = 12.sp,
                                color = ThemeOnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Plan 1: Blue Tick Verification
            item {
                PlanCard(
                    title = "Blue Tick Verification Badge",
                    price = "₹999",
                    period = "/ Month",
                    badgeText = "TRUST & PRESTIGE",
                    badgeColor = VerifiedBlue,
                    icon = Icons.Default.Verified,
                    features = listOf(
                        "Official Blue Verified Badge on Studio Card & Profile",
                        "Higher Trust Score & Client Booking Confidence",
                        "Priority ranking above unverified studios in search results",
                        "Valid for 30 Days with auto-expiry protection"
                    ),
                    buttonText = if (s.isVerifiedActive) "Extend Blue Tick (30 Days)" else "Get Blue Tick Badge (₹999)",
                    isLoading = isUpdating,
                    onSubscribe = {
                        scope.launch {
                            isUpdating = true
                            val now = System.currentTimeMillis()
                            val currentExp = s.subscriptionExpiresAt ?: now
                            val newExp = (if (currentExp > now) currentExp else now) + (30L * 24 * 60 * 60 * 1000L)
                            
                            ServiceLocator.studioRepository.updateMonetization(
                                studioId = s.id,
                                isVerified = true,
                                isSponsored = s.isSponsored,
                                subscriptionExpiresAt = newExp,
                                sponsoredExpiresAt = s.sponsoredExpiresAt
                            )
                            currentStudio = s.copy(
                                isVerified = true,
                                verified = true,
                                verifiedStatus = "VERIFIED",
                                subscriptionExpiresAt = newExp
                            )
                            isUpdating = false
                            Toast.makeText(context, "Blue Tick Verified Badge activated for 30 days!", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

            // Plan 2: Sponsored Spotlight
            item {
                PlanCard(
                    title = "Featured Sponsored Spotlight",
                    price = "₹1,499",
                    period = "/ Month",
                    badgeText = "MAX VISIBILITY",
                    badgeColor = Color(0xFFE5A93C),
                    icon = Icons.Default.ElectricBolt,
                    features = listOf(
                        "#1 Top Placement in Search & Category results",
                        "Prominent 'SPONSORED' highlight on client cards",
                        "Exclusive highlight in Prayagraj wedding searches",
                        "Valid for 30 Days with auto-expiry protection"
                    ),
                    buttonText = if (s.isSponsoredActive) "Extend Sponsored Boost (30 Days)" else "Boost Studio Spotlight (₹1,499)",
                    isLoading = isUpdating,
                    onSubscribe = {
                        scope.launch {
                            isUpdating = true
                            val now = System.currentTimeMillis()
                            val currentExp = s.sponsoredExpiresAt ?: now
                            val newExp = (if (currentExp > now) currentExp else now) + (30L * 24 * 60 * 60 * 1000L)
                            
                            ServiceLocator.studioRepository.updateMonetization(
                                studioId = s.id,
                                isVerified = s.isVerified,
                                isSponsored = true,
                                subscriptionExpiresAt = s.subscriptionExpiresAt,
                                sponsoredExpiresAt = newExp
                            )
                            currentStudio = s.copy(
                                isSponsored = true,
                                sponsoredExpiresAt = newExp
                            )
                            isUpdating = false
                            Toast.makeText(context, "Sponsored Spotlight activated for 30 days!", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

            // Plan 3: VIP Growth Bundle
            item {
                PlanCard(
                    title = "VIP Studio Growth Bundle",
                    price = "₹1,999",
                    period = "/ Month",
                    badgeText = "BEST VALUE • 25% OFF",
                    badgeColor = ThemePrimary,
                    icon = Icons.Default.WorkspacePremium,
                    features = listOf(
                        "BOTH Blue Tick Verified & Sponsored Spotlight",
                        "Highest possible rank in client discovery algorithm",
                        "Dedicated fast customer support for wedding queries",
                        "30 Days of maximum exposure & verified partner branding"
                    ),
                    buttonText = "Activate VIP Growth Bundle (₹1,999)",
                    isLoading = isUpdating,
                    isBestValue = true,
                    onSubscribe = {
                        scope.launch {
                            isUpdating = true
                            val now = System.currentTimeMillis()
                            val newExp = now + (30L * 24 * 60 * 60 * 1000L)
                            
                            ServiceLocator.studioRepository.updateMonetization(
                                studioId = s.id,
                                isVerified = true,
                                isSponsored = true,
                                subscriptionExpiresAt = newExp,
                                sponsoredExpiresAt = newExp
                            )
                            currentStudio = s.copy(
                                isVerified = true,
                                verified = true,
                                verifiedStatus = "VERIFIED",
                                isSponsored = true,
                                subscriptionExpiresAt = newExp,
                                sponsoredExpiresAt = newExp
                            )
                            isUpdating = false
                            Toast.makeText(context, "VIP Growth Bundle (Verified + Sponsored) activated!", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

            // Expiry Testing Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeOutline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = ThemeOnSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-Expiry Simulation Controls",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeOnSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Easily test timestamp auto-expiry behavior. Setting to Expired will simulate timestamps in the past and automatically remove badges and ranking boosts.",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isUpdating = true
                                        // Set expiry to 1 hour in the past
                                        val past = System.currentTimeMillis() - (3600 * 1000L)
                                        ServiceLocator.studioRepository.updateMonetization(
                                            studioId = s.id,
                                            isVerified = true,
                                            isSponsored = true,
                                            subscriptionExpiresAt = past,
                                            sponsoredExpiresAt = past
                                        )
                                        currentStudio = s.copy(
                                            isVerified = true,
                                            isSponsored = true,
                                            subscriptionExpiresAt = past,
                                            sponsoredExpiresAt = past
                                        )
                                        isUpdating = false
                                        Toast.makeText(context, "Monetization set to EXPIRED (past timestamp). Badges will auto-hide.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                            ) {
                                Text("Simulate Expiry", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        isUpdating = true
                                        // Reset to 30 days from now
                                        val future = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L)
                                        ServiceLocator.studioRepository.updateMonetization(
                                            studioId = s.id,
                                            isVerified = true,
                                            isSponsored = true,
                                            subscriptionExpiresAt = future,
                                            sponsoredExpiresAt = future
                                        )
                                        currentStudio = s.copy(
                                            isVerified = true,
                                            isSponsored = true,
                                            subscriptionExpiresAt = future,
                                            sponsoredExpiresAt = future
                                        )
                                        isUpdating = false
                                        Toast.makeText(context, "Reset to Active (30 days remaining)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary)
                            ) {
                                Text("Reset Active", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    period: String,
    badgeText: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    features: List<String>,
    buttonText: String,
    isLoading: Boolean,
    isBestValue: Boolean = false,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        border = BorderStroke(
            width = if (isBestValue) 2.dp else 1.dp,
            color = if (isBestValue) ThemePrimary else ThemeOutline
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeOnBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ThemeOnBackground
                )
                Text(
                    text = " $period",
                    fontSize = 14.sp,
                    color = ThemeOnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = ThemeOutline)
            Spacer(modifier = Modifier.height(14.dp))

            features.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessColor,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        fontSize = 13.sp,
                        color = ThemeOnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onSubscribe,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBestValue) ThemePrimary else badgeColor
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ThemeOnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (badgeColor == Color(0xFFE5A93C)) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
