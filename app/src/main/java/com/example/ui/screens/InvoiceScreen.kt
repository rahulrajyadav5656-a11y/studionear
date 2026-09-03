package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Booking
import com.example.data.models.Studio
import com.example.di.ServiceLocator
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    bookingId: String,
    onBack: () -> Unit
) {
    var booking by remember { mutableStateOf<Booking?>(null) }
    var studio by remember { mutableStateOf<Studio?>(null) }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(bookingId) {
        try {
            if (bookingId.isNotBlank()) {
                val snapshot = ServiceLocator.firestore.collection("bookings").document(bookingId).get().await()
                val b = snapshot.toObject(Booking::class.java)
                booking = b

                if (b != null) {
                    // Fetch Studio details
                    if (b.studioId.isNotBlank()) {
                        studio = ServiceLocator.studioRepository.getStudioById(b.studioId)
                    }
                    // Fetch Client details
                    if (b.clientId.isNotBlank()) {
                        val userSnap = ServiceLocator.firestore.collection("users").document(b.clientId).get().await()
                        clientName = userSnap.getString("fullName") ?: userSnap.getString("name") ?: "Client"
                        clientPhone = userSnap.getString("phoneNumber") ?: userSnap.getString("phone") ?: "Not Provided"
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore error, handle safely in UI
        } finally {
            isLoading = false
        }
    }

    val eventDateStr = remember(booking?.eventDate) {
        val dt = booking?.eventDate ?: 0L
        if (dt > 0L) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dt)) else "Not Specified"
    }

    val invoiceDateStr = remember(booking?.createdAt) {
        val dt = booking?.createdAt ?: System.currentTimeMillis()
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dt))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "STUDIONEAR",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Official Booking Invoice",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        InvoiceSectionCard(title = "INVOICE DETAILS") {
                            InvoiceRow("Invoice Number", "INV-${bookingId.takeLast(6).uppercase()}")
                            InvoiceRow("Invoice Date", invoiceDateStr)
                            InvoiceRow("Booking ID", bookingId)
                            InvoiceRow("Status", booking?.status?.name ?: "PENDING")
                        }
                    }

                    item {
                        InvoiceSectionCard(title = "CLIENT DETAILS") {
                            InvoiceRow("Name", clientName.ifBlank { "Client" })
                            InvoiceRow("Contact", clientPhone.ifBlank { "Not Specified" })
                            InvoiceRow("Event Location", booking?.location?.ifBlank { "Venue" } ?: "Venue")
                        }
                    }

                    item {
                        InvoiceSectionCard(title = "STUDIO DETAILS") {
                            InvoiceRow("Studio Name", studio?.name?.ifBlank { booking?.studioName } ?: "Verified Studio")
                            InvoiceRow("Owner / Contact", studio?.contactPerson?.ifBlank { studio?.phone } ?: "Studio Manager")
                            InvoiceRow("Phone", studio?.phone?.ifBlank { "Not Specified" } ?: "Not Specified")
                            InvoiceRow("City", studio?.city?.ifBlank { "Pan-India" } ?: "Pan-India")
                        }
                    }

                    item {
                        InvoiceSectionCard(title = "EVENT DETAILS & BILLING") {
                            InvoiceRow("Event Type", booking?.eventType?.ifBlank { "Photography Service" } ?: "Photography Service")
                            InvoiceRow("Event Date", eventDateStr)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            InvoiceRow("Total Quoted Amount", "₹${booking?.totalAmount?.toInt() ?: 0}")
                            InvoiceRow("Advance Required (30%)", "₹${((booking?.totalAmount ?: 0.0) * 0.30).toInt()}")
                            InvoiceRow("Advance Paid", "₹${booking?.paidAmount?.toInt() ?: 0}")
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Cancellation & Refund Policy",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Full refund available if cancelled 7+ days prior to event date.\n• Cancellations within 7 days are non-refundable.\n• Verified Studio guarantee active on this booking.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
