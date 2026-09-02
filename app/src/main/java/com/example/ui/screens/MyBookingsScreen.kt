package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Booking
import com.example.data.models.BookingStatus
import com.example.di.ServiceLocator
import com.example.ui.theme.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onReviewBooking: (String, String, String) -> Unit = { _, _, _ -> },
    onBookingClick: (String) -> Unit = {}
) {
    val clientId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Upcoming", "Completed", "Cancelled/Declined")
    
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    
    LaunchedEffect(clientId) {
        ServiceLocator.bookingRepository.getClientBookings(clientId).collect {
            bookings = it
        }
    }
    
    val filteredBookings = remember(selectedTabIndex, bookings) {
        when(selectedTabIndex) {
            0 -> bookings.filter { it.status == BookingStatus.PENDING }
            1 -> bookings.filter { it.status == BookingStatus.ACCEPTED }
            2 -> bookings.filter { it.status == BookingStatus.COMPLETED }
            3 -> bookings.filter { it.status == BookingStatus.CANCELLED || it.status == BookingStatus.REJECTED || it.status == BookingStatus.DECLINED }
            else -> emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        TopAppBar(
            title = { Text("My Bookings", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
        )
        
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = ThemeBackground,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = ThemePrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { 
                        Text(
                            title, 
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) ThemePrimary else ThemeOnSurfaceVariant
                        ) 
                    }
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredBookings.isEmpty()) {
                item {
                    Text("No bookings found in this category.", color = ThemeOnSurfaceVariant, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(filteredBookings) { booking ->
                    BookingItem(
                        booking = booking,
                        hasReviewed = false, // Simplified for this context
                        onReviewBooking = {
                            val studioName = booking.studioName.ifEmpty { "Studio" }
                            onReviewBooking(booking.id, booking.studioId, studioName)
                        },
                        onClick = { onBookingClick(booking.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingItem(
    booking: Booking,
    hasReviewed: Boolean,
    onReviewBooking: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val studioName = booking.studioName.ifEmpty { "Studio Name Not Provided" }
            Text(studioName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground)
            Spacer(modifier = Modifier.height(4.dp))
            
            val eventDateStr = if (booking.eventDate > 0) {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(booking.eventDate))
            } else {
                "Not Specified"
            }
            Text("Event Date: $eventDateStr", fontSize = 14.sp, color = ThemeOnSurfaceVariant)
            if (booking.eventType.isNotBlank()) {
                Text("Type: ${booking.eventType}", fontSize = 14.sp, color = ThemeOnSurfaceVariant)
            }
            if (booking.location.isNotBlank()) {
                Text("Location: ${booking.location}", fontSize = 14.sp, color = ThemeOnSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Status: ${booking.status}", color = ThemePrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}
