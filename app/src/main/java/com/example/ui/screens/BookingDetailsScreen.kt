package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
fun BookingDetailsScreen(
    bookingId: String,
    onBack: () -> Unit,
    onViewInvoice: () -> Unit,
    onViewTerms: () -> Unit,
    onViewPayments: () -> Unit,
    onViewDeliveries: () -> Unit,
    onViewComplaints: () -> Unit,
    onReviewBooking: (String, String, String) -> Unit
) {
    val bookings by MockDataManager.bookings.collectAsState()
    val booking = bookings.find { it.id == bookingId }

    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Booking not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Booking ID: ${booking.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Status: ${booking.status}", color = ThemePrimary, fontWeight = FontWeight.Bold)
                        Text("Package: ${booking.packageId}", color = ThemeOnSurfaceVariant)
                        Text("Total Amount: ₹${booking.totalAmount.toInt()}", color = ThemeOnSurfaceVariant)
                    }
                }
            }

            val menuItems = listOf(
                "Invoice" to onViewInvoice,
                "Terms & Conditions" to onViewTerms,
                "Payment Information" to onViewPayments,
                "Delivery Information" to onViewDeliveries,
                "Complaint / Dispute" to onViewComplaints
            )

            items(menuItems.size) { index ->
                val (label, action) = menuItems[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeSurface, RoundedCornerShape(12.dp))
                        .clickable { action() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ThemeOnSurfaceVariant)
                }
            }

            if (booking.status == BookingStatus.COMPLETED) {
                item {
                    val hasReviewed = MockDataManager.hasReviewedBooking(booking.id)
                    val studioName = if (booking.studioId == "1") "Prayagraj Moments" else "Cinematic Wedz"
                    if (!hasReviewed) {
                        Button(
                            onClick = { onReviewBooking(booking.id, booking.studioId, studioName) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary)
                        ) {
                            Text("Rate & Review", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Review Submitted", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            } else {
                item {
                    Text("Review available after your booking is completed.", fontSize = 12.sp, color = ThemeOnSurfaceVariant)
                }
            }
        }
    }
}
