package com.example.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataManager
import com.example.data.models.BookingStatus
import com.example.data.models.DeliveryStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingDetailsScreen(bookingId: String, onBack: () -> Unit) {
    val bookings by MockDataManager.bookings.collectAsState()
    val booking = bookings.find { it.id == bookingId }

    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Booking not found")
        }
        return
    }
    
    val packageDetails by remember { derivedStateOf { MockDataManager.packages.value.find { it.id == booking.packageId } } }
    
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    
    var showDeliveryDialog by remember { mutableStateOf(false) }
    var deliveryNotes by remember { mutableStateOf(booking.deliveryNotes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking #${booking.id}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status", color = ThemeOnSurfaceVariant)
                    Text(booking.status.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ThemePrimary)
                    if (booking.status == BookingStatus.REJECTED && booking.rejectionReason != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reason: ${booking.rejectionReason}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            // Client Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Client Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Client ID: ${booking.clientId}")
                    // In real app, fetch client name/phone from users DB
                }
            }
            
            // Event Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Package & Event", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Package: ${packageDetails?.name ?: "Unknown"}")
                    Text("Price: ₹${booking.totalAmount}")
                    Text("Paid: ₹${booking.paidAmount}")
                }
            }

            // Actions
            when (booking.status) {
                BookingStatus.PENDING -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.ACCEPTED) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept")
                        }
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Reject")
                        }
                    }
                }
                BookingStatus.ACCEPTED -> {
                    Button(
                        onClick = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.IN_PROGRESS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Work Started")
                    }
                }
                BookingStatus.IN_PROGRESS -> {
                    Button(
                        onClick = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.COMPLETED) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Work Completed")
                    }
                }
                BookingStatus.COMPLETED -> {
                    Button(
                        onClick = { showDeliveryDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage Delivery")
                    }
                }
                else -> {}
            }
        }
    }
    
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Booking") },
            text = {
                Column {
                    Text("Please provide a reason for rejecting this booking:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (rejectReason.isNotBlank()) {
                        MockDataManager.rejectBooking(booking.id, rejectReason)
                        showRejectDialog = false
                    }
                }) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    if (showDeliveryDialog) {
        var currentDeliveryStatus by remember { mutableStateOf(booking.deliveryStatus) }
        AlertDialog(
            onDismissRequest = { showDeliveryDialog = false },
            title = { Text("Delivery Management") },
            text = {
                Column {
                    Text("Status:")
                    val statuses = listOf(DeliveryStatus.NOT_STARTED, DeliveryStatus.IN_PROGRESS, DeliveryStatus.DELIVERED)
                    statuses.forEach { s ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentDeliveryStatus == s,
                                onClick = { currentDeliveryStatus = s }
                            )
                            Text(s.name)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deliveryNotes,
                        onValueChange = { deliveryNotes = it },
                        label = { Text("Delivery Notes / Cloud Link") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    MockDataManager.updateDeliveryStatus(booking.id, currentDeliveryStatus)
                    MockDataManager.updateDeliveryNotes(booking.id, deliveryNotes)
                    showDeliveryDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryDialog = false }) { Text("Cancel") }
            }
        )
    }
}
