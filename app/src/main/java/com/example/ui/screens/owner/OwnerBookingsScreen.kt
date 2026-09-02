package com.example.ui.screens.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Booking
import com.example.data.models.BookingStatus
import com.example.di.ServiceLocator
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingsScreen(onNavigateToBooking: (String) -> Unit) {
    val ownerId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
    
    val scope = rememberCoroutineScope()
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    
    LaunchedEffect(ownerId) {
        ServiceLocator.bookingRepository.getOwnerBookings(ownerId).collect {
            bookings = it
        }
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Upcoming", "Completed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ThemeBackground,
                contentColor = ThemePrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            val displayBookings = when (selectedTab) {
                0 -> bookings.filter { it.status == BookingStatus.PENDING }
                1 -> bookings.filter { it.status == BookingStatus.ACCEPTED }
                else -> bookings.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.DECLINED || it.status == BookingStatus.REJECTED || it.status == BookingStatus.CANCELLED }
            }
            
            if (displayBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No bookings found.", color = ThemeOnSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayBookings) { booking ->
                        OwnerBookingCard(
                            booking = booking,
                            onAccept = { 
                                scope.launch { ServiceLocator.bookingRepository.updateBookingStatus(booking.id, BookingStatus.ACCEPTED) }
                            },
                            onDecline = { 
                                scope.launch { ServiceLocator.bookingRepository.updateBookingStatus(booking.id, BookingStatus.DECLINED) }
                            },
                            onComplete = { 
                                scope.launch { ServiceLocator.bookingRepository.updateBookingStatus(booking.id, BookingStatus.COMPLETED) }
                            },
                            onClick = { onNavigateToBooking(booking.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerBookingCard(
    booking: Booking,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Client: ${if (booking.clientEmail.isEmpty()) "Unknown" else booking.clientEmail}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ThemeOnBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Type: ${if (booking.eventType.isEmpty()) "General Booking" else booking.eventType}", color = ThemeOnSurfaceVariant)
            val dateString = if (booking.eventDate > 0) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(booking.eventDate)) else "Unspecified"
            Text("Date: $dateString", color = ThemeOnSurfaceVariant)
            if (booking.location.isNotBlank()) {
                Text("Location: ${booking.location}", color = ThemeOnSurfaceVariant)
            }
            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: ${booking.notes}", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            when (booking.status) {
                BookingStatus.PENDING -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onAccept, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary)) {
                            Text("Accept")
                        }
                        OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f)) {
                            Text("Decline")
                        }
                    }
                }
                BookingStatus.ACCEPTED -> {
                    Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                        Text("Mark Completed")
                    }
                }
                else -> {
                    Text("Status: ${booking.status.name}", color = ThemePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
