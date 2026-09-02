package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
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
fun AgreementsScreen(onBack: () -> Unit) {
    val bookings by MockDataManager.bookings.collectAsState()
    val activeBookings = bookings.filter { it.status != BookingStatus.PENDING && it.status != BookingStatus.REJECTED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agreements", fontWeight = FontWeight.Bold) },
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
        if (activeBookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(64.dp), tint = ThemeOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No agreements found", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = ThemeOnSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(activeBookings) { booking ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Agreement #${booking.id.takeLast(4)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Accepted", color = SuccessColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Studio: ${booking.studioId}", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                            Text("Package: ${booking.packageId}", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                            Text("Amount: ₹${booking.totalAmount.toInt()}", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cancellation: 50% non-refundable if cancelled 7 days before event.", fontSize = 12.sp, color = ThemeOutline)
                            Text("Delivery: Expected 14 days after event.", fontSize = 12.sp, color = ThemeOutline)
                        }
                    }
                }
            }
        }
    }
}
