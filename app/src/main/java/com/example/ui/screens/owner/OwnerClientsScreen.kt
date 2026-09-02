package com.example.ui.screens.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.MockDataManager
import com.example.data.UserRepository
import com.example.data.models.BookingStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerClientsScreen(onBack: () -> Unit) {
    val ownerId = AuthManager.getCurrentUser() ?: return
    val studio = MockDataManager.getStudioByOwnerId(ownerId) ?: return
    
    val allBookings by MockDataManager.bookings.collectAsState()
    val studioBookings = allBookings.filter { it.studioId == studio.id }
    val users by UserRepository.users.collectAsState()
    
    val clientIds = studioBookings.map { it.clientId }.distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clients") },
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
        if (clientIds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Clients will appear after your first booking.", color = ThemeOnSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(clientIds) { clientId ->
                    val clientBookings = studioBookings.filter { it.clientId == clientId }
                    val upcoming = clientBookings.count { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.IN_PROGRESS }
                    val completed = clientBookings.count { it.status == BookingStatus.COMPLETED }
                    val totalPaid = clientBookings.sumOf { it.paidAmount }
                    
                    val clientUser = users.find { it.id == clientId }
                    val name = clientUser?.fullName?.takeIf { it.isNotBlank() } ?: "Client #$clientId"
                    val contact = clientUser?.phoneNumber ?: "No contact provided"

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { /* Could open client detail */ },
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(contact, color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Upcoming: $upcoming | Completed: $completed", fontSize = 12.sp)
                                Text("Total Paid: ₹$totalPaid", fontSize = 12.sp, color = ThemePrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
