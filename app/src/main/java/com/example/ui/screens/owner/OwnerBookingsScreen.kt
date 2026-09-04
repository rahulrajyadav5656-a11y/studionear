package com.example.ui.screens.owner

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Booking
import com.example.data.models.BookingStatus
import com.example.di.ServiceLocator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingsScreen(
    onBack: () -> Unit = {},
    onNavigateToBooking: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val ownerId = ServiceLocator.auth.currentUser?.uid ?: ""

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("New Inquiries", "Accepted", "Completed", "Cancelled")
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(ownerId) {
        if (ownerId.isNotBlank()) {
            ServiceLocator.bookingRepository.getOwnerBookings(ownerId).collect {
                bookings = it
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    val filteredBookings = remember(selectedTabIndex, bookings) {
        when (selectedTabIndex) {
            0 -> bookings.filter { it.status == BookingStatus.PENDING }
            1 -> bookings.filter { it.status == BookingStatus.ACCEPTED }
            2 -> bookings.filter { it.status == BookingStatus.COMPLETED }
            3 -> bookings.filter { it.status == BookingStatus.CANCELLED || it.status == BookingStatus.REJECTED }
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Studio Leads & Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No inquiries in this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredBookings) { booking ->
                        val currentBookingId = booking.bookingId.ifEmpty { booking.id }
                        OwnerBookingCard(
                            booking = booking,
                            onCardClick = { onNavigateToBooking(currentBookingId) },
                            onAccept = {
                                scope.launch {
                                    ServiceLocator.bookingRepository.updateBookingStatus(currentBookingId, BookingStatus.ACCEPTED)

                                    val notificationData = mapOf(
                                        "recipientId" to booking.clientId,
                                        "title" to "Booking Confirmed!",
                                        "body" to "Studio has accepted your inquiry for ${booking.eventType.ifEmpty { "your event" }}.",
                                        "bookingId" to currentBookingId,
                                        "timestamp" to System.currentTimeMillis(),
                                        "status" to "PENDING_DELIVERY"
                                    )
                                    firestore.collection("notifications").add(notificationData)

                                    Toast.makeText(context, "Inquiry Accepted & Notification Sent!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDecline = {
                                scope.launch {
                                    ServiceLocator.bookingRepository.updateBookingStatus(currentBookingId, BookingStatus.REJECTED)
                                    Toast.makeText(context, "Inquiry Declined.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCallClient = { phone ->
                                if (phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Client phone number not available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerBookingCard(
    booking: Booking,
    onCardClick: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onCallClient: (String) -> Unit
) {
    var clientName by remember { mutableStateOf("Client") }
    var clientPhone by remember { mutableStateOf("") }

    LaunchedEffect(booking.clientId, booking.id) {
        // First check Firestore users collection
        if (booking.clientId.isNotBlank()) {
            try {
                val doc = ServiceLocator.firestore.collection("users").document(booking.clientId).get().await()
                val name = doc.getString("fullName") ?: doc.getString("name")
                val phone = doc.getString("phoneNumber") ?: doc.getString("phone")
                if (!name.isNullOrBlank()) clientName = name
                if (!phone.isNullOrBlank()) clientPhone = phone
            } catch (_: Exception) {}
        }
        // If still empty, check booking document directly
        if (clientPhone.isBlank()) {
            val bookingDocId = booking.bookingId.ifEmpty { booking.id }
            if (bookingDocId.isNotBlank()) {
                try {
                    val bDoc = ServiceLocator.firestore.collection("bookings").document(bookingDocId).get().await()
                    val bName = bDoc.getString("clientName")
                    val bPhone = bDoc.getString("clientPhone")
                    if (!bName.isNullOrBlank()) clientName = bName
                    if (!bPhone.isNullOrBlank()) clientPhone = bPhone
                } catch (_: Exception) {}
            }
        }
    }

    val eventDateStr = if (booking.eventDate > 0L) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(booking.eventDate))
    } else {
        "Date Not Specified"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = clientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (clientPhone.isNotBlank()) {
                        Text(text = clientPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = booking.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Service: " + booking.eventType.ifEmpty { "Event Coverage" }, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Event Date: $eventDateStr", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Location: " + booking.location.ifEmpty { "Venue" }, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Quoted Amount: ₹" + booking.totalAmount.toInt().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onCallClient(clientPhone) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Call Client", fontSize = 13.sp)
                }

                if (booking.status == BookingStatus.PENDING) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDecline,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Decline", fontSize = 13.sp)
                        }
                        Button(
                            onClick = onAccept,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Accept", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
