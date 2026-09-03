package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QuoteBuilderSheet
import com.google.firebase.firestore.FirebaseFirestore

data class StudioDetailData(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val area: String = "",
    val phone: String = "",
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val startingPrice: Double = 0.0,
    val isVerified: Boolean = false,
    val about: String = "",
    val specialties: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioDetailScreen(
    studioId: String,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }

    var studio by remember { mutableStateOf<StudioDetailData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showQuoteSheet by remember { mutableStateOf(false) }
    var bookingSuccessId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(studioId) {
        isLoading = true
        firestore.collection("studios").document(studioId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val rawSpecialties = doc.get("specialties") as? List<*>
                    studio = StudioDetailData(
                        id = doc.id,
                        name = doc.getString("name") ?: "Studio Partner",
                        city = doc.getString("city") ?: "",
                        area = doc.getString("area") ?: "",
                        phone = doc.getString("phone") ?: "",
                        rating = doc.getDouble("rating") ?: 5.0,
                        reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                        startingPrice = doc.getDouble("startingPrice") ?: 0.0,
                        isVerified = doc.getBoolean("isVerified") ?: false,
                        about = doc.getString("about") ?: "Professional wedding and candid photography studio specializing in cinematic films and traditional coverage.",
                        specialties = rawSpecialties?.mapNotNull { it?.toString() } ?: emptyList()
                    )
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F14),
        bottomBar = {
            if (studio != null) {
                Surface(
                    color = Color(0xFF1A1A24),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (!studio?.phone.isNullOrBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${studio?.phone}")
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call")
                        }

                        Button(
                            onClick = { showQuoteSheet = true },
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check Rates & Book", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF8B5CF6))
            }
        } else if (studio == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Studio details not found", color = Color.Gray)
            }
        } else {
            val currentStudio = studio!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E28))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentStudio.name,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (currentStudio.isVerified) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFF2E244D),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${currentStudio.rating}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentStudio.area}, ${currentStudio.city}",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFF2E2E3E), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "ABOUT STUDIO",
                                color = Color(0xFF8B5CF6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentStudio.about,
                                color = Color(0xFFD1D5DB),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )

                            if (currentStudio.specialties.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "SPECIALTIES",
                                    color = Color(0xFF8B5CF6),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    currentStudio.specialties.forEach { tag ->
                                        Surface(
                                            color = Color(0xFF2A2A3A),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                color = Color(0xFFE5E7EB),
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showQuoteSheet && studio != null) {
        QuoteBuilderSheet(
            studioId = studio!!.id,
            studioName = studio!!.name,
            onDismiss = { showQuoteSheet = false },
            onBookingSubmitted = { bookingId ->
                bookingSuccessId = bookingId
                showQuoteSheet = false
            }
        )
    }

    if (bookingSuccessId != null) {
        AlertDialog(
            onDismissRequest = { bookingSuccessId = null },
            title = { Text("Booking Sent Successfully!", color = Color.White) },
            text = { Text("Studio owner has been notified. Booking ID: $bookingSuccessId", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = { bookingSuccessId = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("OK")
                }
            },
            containerColor = Color(0xFF1E1E28)
        )
    }
}
