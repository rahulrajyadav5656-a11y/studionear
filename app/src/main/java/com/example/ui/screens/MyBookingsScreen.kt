package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit = {},
    onBookingClick: (String) -> Unit,
    onReviewBooking: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clientId = ServiceLocator.auth.currentUser?.uid ?: ""

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Accepted", "Completed", "Cancelled")
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(clientId) {
        if (clientId.isNotBlank()) {
            ServiceLocator.bookingRepository.getClientBookings(clientId).collect {
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
                title = { Text("My Bookings", fontWeight = FontWeight.Bold) }
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
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No bookings found in this category.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        BookingItemCard(
                            booking = booking,
                            onCardClick = { onBookingClick(currentBookingId) },
                            onCallClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${booking.studioPhone.ifEmpty { "100" }}")
                                }
                                context.startActivity(intent)
                            },
                            onCancelClick = {
                                val now = System.currentTimeMillis()
                                val sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000
                                val timeDifference = booking.eventDate - now

                                if (timeDifference >= sevenDaysInMillis) {
                                    scope.launch {
                                        ServiceLocator.bookingRepository.updateBookingStatus(
                                            currentBookingId,
                                            BookingStatus.CANCELLED
                                        )
                                        Toast.makeText(context, "Booking cancelled successfully.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Cannot cancel within 7 days of event date.", Toast.LENGTH_LONG).show()
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
private fun BookingItemCard(
    booking: Booking,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val eventDateStr = if (booking.eventDate > 0L) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(booking.eventDate))
    } else {
        "Date Not Specified"
    }

    val displayStudioName = when {
        booking.studioName.isNotBlank() -> booking.studioName
        booking.studioId.isNotBlank() -> "Studio (${booking.studioId.take(6)})"
        else -> "Registered Studio"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayStudioName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Event: ${booking.eventType.ifEmpty { "Photography Shoot" }}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Date: $eventDateStr", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Location: ${booking.location.ifEmpty { "Venue Address" }}", style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Quoted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (booking.totalAmount > 0) {
                        Text(
                            text = "₹${booking.totalAmount.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = "Quote Awaited",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = onCallClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Studio",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onCardClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Invoice", fontSize = 12.sp)
                    }

                    if (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.ACCEPTED) {
                        Button(
                            onClick = onCancelClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
