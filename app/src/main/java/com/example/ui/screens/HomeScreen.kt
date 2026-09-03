package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.LocationPickerBottomSheet
import com.example.util.LocationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class HomeStudioData(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val area: String = "",
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val startingPrice: Double = 0.0,
    val isSponsored: Boolean = false,
    val isVerified: Boolean = false,
    val specialties: List<String> = emptyList()
)

@Composable
fun HomeScreen(
    onStudioClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }

    var selectedArea by remember { mutableStateOf("Civil Lines") }
    var selectedCity by remember { mutableStateOf("Prayagraj") }
    var showLocationSheet by remember { mutableStateOf(false) }

    var selectedFilterCategory by remember { mutableStateOf<String?>(null) }
    var studioList by remember { mutableStateOf<List<HomeStudioData>>(emptyList()) }
    var isLoadingStudios by remember { mutableStateOf(true) }

    LaunchedEffect(selectedCity, selectedFilterCategory) {
        isLoadingStudios = true
        firestore.collection("studios")
            .get()
            .addOnSuccessListener { snapshot ->
                val fetched = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val city = doc.getString("city") ?: ""
                    val area = doc.getString("area") ?: ""
                    val rating = doc.getDouble("rating") ?: 5.0
                    val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                    val startingPrice = doc.getDouble("startingPrice") ?: 0.0
                    val isSponsored = doc.getBoolean("isSponsored") ?: false
                    val isVerified = doc.getBoolean("isVerified") ?: false
                    val rawSpecialties = doc.get("specialties") as? List<*>
                    val specialties = rawSpecialties?.mapNotNull { it?.toString() } ?: emptyList()

                    HomeStudioData(
                        id = doc.id,
                        name = name,
                        city = city,
                        area = area,
                        rating = rating,
                        reviewCount = reviewCount,
                        startingPrice = startingPrice,
                        isSponsored = isSponsored,
                        isVerified = isVerified,
                        specialties = specialties
                    )
                }

                val filtered = if (selectedFilterCategory != null) {
                    fetched.filter { item ->
                        item.specialties.any { it.contains(selectedFilterCategory!!, ignoreCase = true) }
                    }
                } else {
                    fetched
                }

                studioList = filtered.sortedWith(
                    compareByDescending<HomeStudioData> { it.isSponsored }
                        .thenByDescending { it.isVerified }
                        .thenByDescending { it.rating }
                        .thenByDescending { it.reviewCount }
                )
                isLoadingStudios = false
            }
            .addOnFailureListener {
                isLoadingStudios = false
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                val loc = LocationHelper.fetchCurrentLocationName(context)
                selectedArea = loc.first
                selectedCity = loc.second
            }
        }
    }

    val requestGpsLocation = {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
            coroutineScope.launch {
                val loc = LocationHelper.fetchCurrentLocationName(context)
                selectedArea = loc.first
                selectedCity = loc.second
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F14)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { showLocationSheet = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CURRENT LOCATION",
                                color = Color(0xFF8B5CF6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$selectedArea, $selectedCity",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Change Location",
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E28))
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Capture Your Perfect Moments",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Find trusted wedding photographers & cinematic studios near you",
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { selectedFilterCategory = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Explore All Studios", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search studios, area or service...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFF2E2E3E),
                        disabledContainerColor = Color(0xFF171720)
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "SERVICES & STYLES",
                    color = Color(0xFF8B5CF6),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val categories = listOf(
                    Triple("Candid", Icons.Default.CameraAlt, Color(0xFF3B2864)),
                    Triple("Cinematic", Icons.Default.PlayArrow, Color(0xFF1A3B5C)),
                    Triple("Drone", Icons.Default.Send, Color(0xFF4A2818)),
                    Triple("Traditional", Icons.Default.Star, Color(0xFF4C1830))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedFilterCategory.equals(cat.first, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedFilterCategory = if (isSelected) null else cat.first
                                    onCategoryClick(cat.first)
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0xFF8B5CF6) else cat.third),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(cat.second, contentDescription = cat.first, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.first.uppercase(),
                                color = if (isSelected) Color.White else Color(0xFFD1D5DB),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedFilterCategory != null) "${selectedFilterCategory} Studios" else "Top Studios in $selectedCity",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${studioList.size} studios available",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    if (selectedFilterCategory != null) {
                        TextButton(onClick = { selectedFilterCategory = null }) {
                            Text("Clear Filter", color = Color(0xFF8B5CF6))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoadingStudios) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    }
                }
            } else if (studioList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No studios registered yet in this area", color = Color.White, fontWeight = FontWeight.Medium)
                            Text("New studio owners will appear here automatically.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(studioList) { studioItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onStudioClick(studioItem.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = studioItem.name,
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (studioItem.isVerified) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified Studio",
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (studioItem.isSponsored) {
                                    Surface(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "SPONSORED",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${studioItem.area}, ${studioItem.city}",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp
                            )

                            if (studioItem.specialties.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    studioItem.specialties.take(3).forEach { tag ->
                                        Surface(
                                            color = Color(0xFF2A2A3A),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                color = Color(0xFFD1D5DB),
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF2E2E3E), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${studioItem.rating}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " (${studioItem.reviewCount} shoots)",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }

                                if (studioItem.startingPrice > 0) {
                                    Text(
                                        text = "Starts ₹${studioItem.startingPrice.toInt()}",
                                        color = Color(0xFF10B981),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showLocationSheet) {
        LocationPickerBottomSheet(
            onDismiss = { showLocationSheet = false },
            onUseCurrentLocation = {
                showLocationSheet = false
                requestGpsLocation()
            },
            onLocationSelected = { area, city ->
                selectedArea = area
                selectedCity = city
                showLocationSheet = false
            }
        )
    }
}
