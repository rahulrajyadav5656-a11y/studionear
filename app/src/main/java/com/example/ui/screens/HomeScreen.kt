package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    val address: String = "",
    val phone: String = "",
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val startingPrice: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val specialties: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
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

    // Search & Filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf<String?>(null) }
    var studioList by remember { mutableStateOf<List<HomeStudioData>>(emptyList()) }
    var isLoadingStudios by remember { mutableStateOf(true) }

    // Live sync studios from Firestore
    LaunchedEffect(Unit) {
        isLoadingStudios = true
        firestore.collection("studios")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val fetched = snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val city = doc.getString("city") ?: ""
                        val address = doc.getString("address") ?: doc.getString("area") ?: ""
                        val phone = doc.getString("phone") ?: ""
                        val rating = doc.getDouble("rating") ?: 4.9
                        val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 12
                        val startingPrice = doc.getDouble("startingPrice") ?: doc.getDouble("price") ?: 15000.0
                        val lat = doc.getDouble("latitude") ?: 0.0
                        val lng = doc.getDouble("longitude") ?: 0.0
                        val rawSpecialties = doc.get("specialties") as? List<*>
                        val specialties = rawSpecialties?.mapNotNull { it?.toString() } ?: listOf("Wedding", "Candid", "Cinematic")

                        HomeStudioData(
                            id = doc.id,
                            name = name,
                            city = city,
                            address = address,
                            phone = phone,
                            rating = rating,
                            reviewCount = reviewCount,
                            startingPrice = startingPrice,
                            latitude = lat,
                            longitude = lng,
                            specialties = specialties
                        )
                    }
                    studioList = fetched
                }
                isLoadingStudios = false
            }
    }

    // Filter by search query or style
    val displayedStudios = remember(studioList, searchQuery, selectedFilterCategory) {
        studioList.filter { studio ->
            val matchesQuery = searchQuery.isBlank() ||
                    studio.name.contains(searchQuery, ignoreCase = true) ||
                    studio.address.contains(searchQuery, ignoreCase = true) ||
                    studio.city.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedFilterCategory == null ||
                    studio.specialties.any { it.contains(selectedFilterCategory!!, ignoreCase = true) } ||
                    studio.name.contains(selectedFilterCategory!!, ignoreCase = true)

            matchesQuery && matchesCategory
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

    Scaffold(
        containerColor = Color(0xFF0F0F14)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))

                // Location & Notification Bar
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
                                text = "LOCATION",
                                color = Color(0xFF8B5CF6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
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

                Spacer(modifier = Modifier.height(16.dp))

                // Real Working Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Studio ya area ka naam likhein...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B5CF6)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF171720),
                        unfocusedContainerColor = Color(0xFF171720),
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF2E2E3E)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Easy Categories
                Text(
                    text = "WEDDING SERVICES",
                    color = Color(0xFF8B5CF6),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E1E28))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF8B5CF6) else Color(0xFF2A2A38),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(cat.second, contentDescription = cat.first, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat.first,
                                color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedFilterCategory != null) "${selectedFilterCategory} Studios" else "Verified Studios",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${displayedStudios.size} Studios",
                        color = Color(0xFF10B981),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Studio Cards
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
            } else if (displayedStudios.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Koi studio nahi mila", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Search clear karein ya dusra ilaaqa chunein.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(displayedStudios, key = { it.id }) { studio ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp)
                            .clickable { onStudioClick(studio.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A38))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = studio.name,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "★ ${studio.rating}",
                                        color = Color(0xFF10B981),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (studio.address.isNotBlank()) studio.address else studio.city.ifBlank { "Prayagraj" },
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bottom actions: Rates + WhatsApp + Call
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onStudioClick(studio.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Rates & Book Karein", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (studio.phone.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${studio.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF242436), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                val cleanPhone = studio.phone.replace("+", "").replace(" ", "").trim()
                                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=91$cleanPhone&text=Namaste! Mujhe aapke studio se wedding photography ki enquiry karni hai.")
                                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationSheet) {
        LocationPickerBottomSheet(
            onDismiss = { showLocationSheet = false },
            onUseCurrentLocation = {
                showLocationSheet = false
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            },
            onLocationSelected = { area, city ->
                selectedArea = area
                selectedCity = city
                showLocationSheet = false
            }
        )
    }
}
