package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.di.ServiceLocator
import com.example.data.models.Booking
import com.example.data.models.BookingStatus

data class ServiceItem(
    val id: String,
    val title: String,
    val price: Double,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreen(
    studioId: String,
    packageId: String,
    onBack: () -> Unit,
    onBookingComplete: (String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var createdBookingId by remember { mutableStateOf("") }

    // Event Info
    var eventType by remember { mutableStateOf("Wedding") }
    var eventDate by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var acceptedAgreement by remember { mutableStateOf(false) }

    // Custom A-la-carte Services
    val availableServices = remember {
        listOf(
            ServiceItem("trad_photo", "Traditional Photography", 10000.0, "Full event standard photography"),
            ServiceItem("candid_photo", "Candid Photography", 15000.0, "High-end candid & creative portraits"),
            ServiceItem("cinematic_video", "Cinematic Teaser & Video", 20000.0, "4K cinematic highlights & full video"),
            ServiceItem("drone", "Drone Aerial Coverage", 8000.0, "Drone view of venue and rituals"),
            ServiceItem("pre_wedding", "Pre-Wedding Shoot", 15000.0, "1-day outdoor shoot with edited reel"),
            ServiceItem("album", "Premium Hardcover Photobook", 7000.0, "40-page customized photo album")
        )
    }

    val selectedServices = remember { mutableStateListOf("trad_photo", "candid_photo") }

    val totalAmount = remember(selectedServices.toList()) {
        selectedServices.sumOf { id -> availableServices.find { it.id == id }?.price ?: 0.0 }
    }
    val advanceAmount = totalAmount * 0.30

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(text = "Booking Request Submitted!", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Your booking request (ID: $createdBookingId) has been sent to the studio owner. They will call you shortly to confirm timings and details."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBookingComplete(createdBookingId)
                    }
                ) {
                    Text("View Invoice")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Studio Services") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Total: ₹${totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "30% Adv: ₹${advanceAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (step == 1) {
                        Button(
                            onClick = { step = 2 },
                            enabled = eventType.isNotBlank() && eventLocation.isNotBlank() && selectedServices.isNotEmpty()
                        ) {
                            Text("Next: Agreement")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isSubmitting) return@Button
                                isSubmitting = true
                                scope.launch {
                                    try {
                                        val currentUserId = ServiceLocator.auth.currentUser?.uid ?: ""
                                        val studio = ServiceLocator.studioRepository.getStudioById(studioId)
                                        val targetOwnerId = studio?.ownerId ?: studioId
                                        val studioName = studio?.name ?: ""

                                        val newBooking = Booking(
                                            bookingId = "",
                                            studioId = studioId,
                                            studioOwnerId = targetOwnerId,
                                            studioName = studioName,
                                            clientId = currentUserId,
                                            packageId = packageId.ifBlank { "CUSTOM" },
                                            eventType = eventType,
                                            eventDate = System.currentTimeMillis(),
                                            location = eventLocation,
                                            totalAmount = totalAmount,
                                            paidAmount = 0.0,
                                            status = BookingStatus.PENDING,
                                            agreementAccepted = true,
                                            createdAt = System.currentTimeMillis()
                                        )

                                        val generatedId = ServiceLocator.bookingRepository.createBookingRequest(newBooking)
                                        val finalId = if (generatedId.isNotBlank()) generatedId else "BK-${System.currentTimeMillis().toString().takeLast(6)}"
                                        createdBookingId = finalId
                                        showSuccessDialog = true
                                    } catch (e: Exception) {
                                        isSubmitting = false
                                    }
                                }
                            },
                            enabled = acceptedAgreement && !isSubmitting
                        ) {
                            Text(if (isSubmitting) "Submitting..." else "Submit Booking")
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (step == 1) {
                item {
                    Text("1. Event Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = eventType,
                        onValueChange = { eventType = it },
                        label = { Text("Event Type (e.g. Wedding, Engagement, Reception)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = eventLocation,
                        onValueChange = { eventLocation = it },
                        label = { Text("Event City / Venue Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        "2. Select Required Services (A-la-carte)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Choose only what you need. Pricing adjusts automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(availableServices.size) { index ->
                    val service = availableServices[index]
                    val isChecked = selectedServices.contains(service.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedServices.remove(service.id)
                                else selectedServices.add(service.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedServices.add(service.id)
                                    else selectedServices.remove(service.id)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(service.title, fontWeight = FontWeight.SemiBold)
                                Text(service.description, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "₹${service.price.toInt()}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                item {
                    Text("Digital Agreement & Safety Terms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Verified Photographer Guarantee", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("1. Advance Protection: 30% advance is held securely until event check-in.")
                            Text("2. Cancellation: Full refund if cancelled 7+ days before event. Zero refund inside 7 days.")
                            Text("3. Fraud Prevention: Studio verified via Aadhaar & PAN.")
                            Text("4. Delivery: High-res files delivered within 30 business days.")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { acceptedAgreement = !acceptedAgreement },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = acceptedAgreement,
                            onCheckedChange = { acceptedAgreement = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I accept the verified booking agreement terms.")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
