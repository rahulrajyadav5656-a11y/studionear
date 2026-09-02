package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataManager
import com.example.data.models.BookingStatus
import com.example.data.models.Complaint
import com.example.data.models.ComplaintStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen(onBack: () -> Unit) {
    val complaints by MockDataManager.complaints.collectAsState()
    val bookings by MockDataManager.bookings.collectAsState()
    val eligibleBookings = bookings.filter { it.status != BookingStatus.PENDING && it.status != BookingStatus.REJECTED }
    
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaints & Disputes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        floatingActionButton = {
            if (eligibleBookings.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = ThemePrimary,
                    contentColor = ThemeOnPrimary
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Raise Complaint", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = ThemeBackground
    ) { padding ->
        if (complaints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(64.dp), tint = ThemeOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No complaints found", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = ThemeOnSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(complaints) { complaint ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(complaint.reason, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(complaint.status.name, color = if (complaint.status == ComplaintStatus.RESOLVED) SuccessColor else ThemeOnSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Booking ID: ${complaint.bookingId}", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(complaint.description, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateComplaintDialog(
            bookings = eligibleBookings,
            onDismiss = { showCreateDialog = false },
            onSubmit = { bookingId, reason, desc ->
                MockDataManager.submitComplaint(
                    Complaint(
                        bookingId = bookingId,
                        reason = reason,
                        description = desc
                    )
                )
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateComplaintDialog(
    bookings: List<com.example.data.models.Booking>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var selectedBookingId by remember { mutableStateOf(bookings.firstOrNull()?.id ?: "") }
    var reason by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Payment issue", "Delivery delay", "Service issue", "False information", "Agreement violation", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Raise Complaint") },
        text = {
            Column {
                if (bookings.isNotEmpty()) {
                    Text("Select Booking", fontSize = 12.sp, color = ThemeOnSurfaceVariant)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = "Booking ID: $selectedBookingId",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            bookings.forEach { booking ->
                                DropdownMenuItem(
                                    text = { Text(booking.packageId.ifEmpty { booking.id }) },
                                    onClick = {
                                        selectedBookingId = booking.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                var categoryExpanded by remember { mutableStateOf(false) }
                Text("Category", fontSize = 12.sp, color = ThemeOnSurfaceVariant)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = reason.ifEmpty { "Select Category" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    reason = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (selectedBookingId.isNotBlank() && reason.isNotBlank() && desc.isNotBlank()) onSubmit(selectedBookingId, reason, desc) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
