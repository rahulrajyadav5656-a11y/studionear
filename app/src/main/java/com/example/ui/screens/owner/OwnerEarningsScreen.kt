package com.example.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.MockDataManager
import com.example.data.models.BookingStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerEarningsScreen(onBack: () -> Unit) {
    val ownerId = AuthManager.getCurrentUser() ?: return
    val studio = MockDataManager.getStudioByOwnerId(ownerId) ?: return
    
    val allBookings by MockDataManager.bookings.collectAsState()
    val studioBookings = allBookings.filter { it.studioId == studio.id }
    
    val completedBookings = studioBookings.filter { it.status == BookingStatus.COMPLETED }
    val totalEarnings = completedBookings.sumOf { it.paidAmount }
    val pendingEarnings = studioBookings.filter { it.status != BookingStatus.COMPLETED && it.status != BookingStatus.REJECTED && it.status != BookingStatus.CANCELLED }.sumOf { it.totalAmount - it.paidAmount }
    val commission = totalEarnings * 0.10 // 10% platform commission demo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earnings (DEMO)") },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ThemePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Net Earnings", color = ThemeBackground, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("₹${totalEarnings - commission}", color = ThemeBackground, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Gross Revenue", color = ThemeOnSurfaceVariant)
                        Text("₹$totalEarnings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ThemePrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Est. Commission", color = ThemeOnSurfaceVariant)
                        Text("₹$commission", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pending Amount", color = ThemeOnSurfaceVariant)
                    Text("₹$pendingEarnings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            completedBookings.sortedByDescending { it.createdAt }.take(10).forEach { booking ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Booking #${booking.id}", fontWeight = FontWeight.Bold)
                            Text("Paid: ₹${booking.paidAmount}", color = ThemePrimary)
                        }
                    }
                }
            }
        }
    }
}
