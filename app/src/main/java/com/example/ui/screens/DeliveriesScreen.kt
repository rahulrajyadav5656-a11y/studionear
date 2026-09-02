package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataManager
import com.example.data.models.BookingStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesScreen(onBack: () -> Unit) {
    val bookings by MockDataManager.bookings.collectAsState()
    val completedBookings = bookings.filter { it.status == BookingStatus.COMPLETED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deliveries", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { padding ->
        if (completedBookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(64.dp), tint = ThemeOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No deliveries yet", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = ThemeOnSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(completedBookings) { booking ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Booking: ${booking.packageId}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Delivered", color = SuccessColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Studio: ${booking.studioId}", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Mock assets status
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Photos: Ready", color = ThemePrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Video: Ready", color = ThemePrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { /* Download */ }, modifier = Modifier.fillMaxWidth()) {
                                Text("Access Delivery Assets")
                            }
                        }
                    }
                }
            }
        }
    }
}
