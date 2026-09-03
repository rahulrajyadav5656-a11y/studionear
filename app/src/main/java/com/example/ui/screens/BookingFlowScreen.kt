package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.di.ServiceLocator
import com.example.data.models.Booking
import com.example.data.models.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreen(
    studioId: String,
    packageId: String,
    onBack: () -> Unit,
    onBookingComplete: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    // Form fields
    var eventDate by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("") }
    var acceptedAgreement by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Studio") },
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
                .padding(16.dp)
        ) {
            when (step) {
                1 -> {
                    Text("Step 1: Event Details", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = eventType,
                        onValueChange = { eventType = it },
                        label = { Text("Event Type (e.g., Wedding)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Event Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = eventLocation,
                        onValueChange = { eventLocation = it },
                        label = { Text("Event Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = eventType.isNotBlank() && eventDate.isNotBlank() && eventLocation.isNotBlank()
                    ) {
                        Text("Next: Review & Agreement")
                    }
                }
                2 -> {
                    Text("Step 2: Agreement & Terms", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            item {
                                Text("Digital Booking Agreement", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("1. Payment Terms: 30% advance, 50% on event day, 20% on delivery.")
                                Text("2. Cancellation: Advance is non-refundable if cancelled within 15 days of event.")
                                Text("3. Delivery Timeline: All raw photos within 7 days, edited files within 30 days.")
                                Text("4. Data Retention: Files will be available for download for 90 days after delivery.")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = acceptedAgreement,
                            onCheckedChange = { acceptedAgreement = it }
                        )
                        Text("I accept the booking agreement and terms.")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isSubmitting) return@Button
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val currentUserId = ServiceLocator.auth.currentUser?.uid ?: ""
                                    val studio = ServiceLocator.studioRepository.getStudioById(studioId)
                                    val targetOwnerId = studio?.ownerId ?: studioId

                                    val newBooking = Booking(
                                        bookingId = "",
                                        studioId = studioId,
                                        studioOwnerId = targetOwnerId,
                                        clientId = currentUserId,
                                        packageId = packageId,
                                        eventDate = eventDate,
                                        eventLocation = eventLocation,
                                        eventType = eventType,
                                        status = BookingStatus.PENDING,
                                        createdAt = System.currentTimeMillis()
                                    )

                                    val generatedId = ServiceLocator.bookingRepository.createBookingRequest(newBooking)
                                    onBookingComplete(if (generatedId.isNotBlank()) generatedId else "BK-${System.currentTimeMillis().toString().takeLast(6)}")
                                } catch (e: Exception) {
                                    isSubmitting = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = acceptedAgreement && !isSubmitting
                    ) {
                        Text(if (isSubmitting) "Booking..." else "Confirm Booking")
                    }
                }
            }
        }
    }
}
