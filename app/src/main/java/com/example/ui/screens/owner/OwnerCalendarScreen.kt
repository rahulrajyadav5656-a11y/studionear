package com.example.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.MockDataManager
import com.example.data.models.BlockedDate
import com.example.data.models.BookingStatus
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerCalendarScreen() {
    val ownerId = AuthManager.getCurrentUser() ?: return
    val studio = MockDataManager.getStudioByOwnerId(ownerId) ?: return
    
    val allBookings by MockDataManager.bookings.collectAsState()
    val blockedDates by MockDataManager.blockedDates.collectAsState()
    
    val studioBookings = allBookings.filter { it.studioId == studio.id }
    val studioBlockedDates = blockedDates.filter { it.studioId == studio.id }
    
    var showBlockDateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBlockDateDialog = true }, containerColor = ThemePrimary) {
                Icon(Icons.Default.Add, contentDescription = "Block Date")
            }
        },
        containerColor = ThemeBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Upcoming Bookings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            val upcoming = studioBookings.filter { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.PENDING }
            if (upcoming.isEmpty()) {
                item { Text("No upcoming bookings.", color = ThemeOnSurfaceVariant) }
            } else {
                items(upcoming) { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Booking #${booking.id}", fontWeight = FontWeight.Bold)
                            Text("Status: ${booking.status.name}", color = ThemePrimary)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Blocked Dates", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (studioBlockedDates.isEmpty()) {
                item { Text("No blocked dates.", color = ThemeOnSurfaceVariant) }
            } else {
                items(studioBlockedDates) { bd ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Blocked: ${bd.reason}", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { MockDataManager.removeBlockedDate(bd.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Unblock")
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showBlockDateDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBlockDateDialog = false },
            title = { Text("Block a Date") },
            text = {
                Column {
                    Text("In a real app, a date picker would appear here. For this demo, this will create a generic block.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason (e.g. Personal, Holiday)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newBlock = BlockedDate(
                        id = UUID.randomUUID().toString(),
                        studioId = studio.id,
                        date = System.currentTimeMillis() + 86400000, // Tomorrow
                        reason = reason.ifBlank { "Unavailable" }
                    )
                    MockDataManager.saveBlockedDate(newBlock)
                    showBlockDateDialog = false
                }) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDateDialog = false }) { Text("Cancel") }
            }
        )
    }
}
