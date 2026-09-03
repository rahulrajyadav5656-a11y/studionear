package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    bookingId: String,
    onBack: () -> Unit = {}
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var isLoading by remember { mutableStateOf(true) }
    var bookingData by remember { mutableStateOf<Map<String, Any>?>(null) }

    // Fetch live booking details from Firestore
    LaunchedEffect(bookingId) {
        if (bookingId.isNotBlank()) {
            val docId = if (bookingId.startsWith("BK-")) bookingId.removePrefix("BK-") else bookingId
            firestore.collection("bookings").document(docId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        bookingData = snapshot.data
                        isLoading = false
                    } else {
                        // Fallback query if stored under full bookingId
                        firestore.collection("bookings").document(bookingId).get()
                            .addOnSuccessListener { directSnap ->
                                bookingData = directSnap.data
                                isLoading = false
                            }
                            .addOnFailureListener { isLoading = false }
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    // Extract dynamic fields with fallbacks
    val studioName = bookingData?.get("studioName") as? String ?: "Studio"
    val eventDate = bookingData?.get("eventDate") as? String ?: "Date Not Set"
    val eventType = bookingData?.get("eventType") as? String ?: (bookingData?.get("serviceTitle") as? String ?: "Photography Service")
    val clientName = bookingData?.get("clientName") as? String ?: "Valued Client"
    val clientPhone = bookingData?.get("clientPhone") as? String ?: "Provided at Venue"
    val eventLocation = bookingData?.get("eventLocation") as? String ?: "Event Venue"
    val status = (bookingData?.get("status") as? String ?: "PENDING").uppercase()

    val totalAmount = (bookingData?.get("quotedAmount") as? Number)?.toDouble()
        ?: (bookingData?.get("totalAmount") as? Number)?.toDouble()
        ?: 0.0

    val advancePaid = (bookingData?.get("advanceAmount") as? Number)?.toDouble()
        ?: (bookingData?.get("advancePaid") as? Number)?.toDouble()
        ?: 0.0

    val advanceRequired = if (totalAmount > 0.0) totalAmount * 0.30 else 0.0
    val balanceDue = if (totalAmount > advancePaid) totalAmount - advancePaid else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Invoice", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13131A))
            )
        },
        containerColor = Color(0xFF0F0F14)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF8B5CF6))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InvoiceRow("Invoice Number", "INV-${bookingId.takeLast(6)}")
                        InvoiceRow("Booking ID", bookingId)
                        InvoiceRow("Status", status, highlightColor = if (status == "CONFIRMED" || status == "ACCEPTED") Color(0xFF10B981) else Color(0xFFF59E0B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Client Details
                Text("CLIENT DETAILS", color = Color(0xFF8B5CF6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InvoiceRow("Name", clientName)
                        InvoiceRow("Contact", clientPhone)
                        InvoiceRow("Event Location", eventLocation)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Studio Details
                Text("STUDIO DETAILS", color = Color(0xFF8B5CF6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InvoiceRow("Studio Name", studioName)
                        InvoiceRow("Service", eventType)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event & Billing Details
                Text("EVENT DETAILS & BILLING", color = Color(0xFF8B5CF6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InvoiceRow("Event Date", eventDate)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF2A2A38))
                        InvoiceRow("Total Quoted Amount", currencyFormat.format(totalAmount))
                        InvoiceRow("Advance Required (30%)", currencyFormat.format(advanceRequired))
                        InvoiceRow("Advance Paid", currencyFormat.format(advancePaid))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF2A2A38))
                        InvoiceRow("Balance Due at Venue", currencyFormat.format(balanceDue), highlightColor = Color(0xFFEF4444), isBold = true)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InvoiceRow(
    label: String,
    value: String,
    highlightColor: Color = Color.White,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF9CA3AF), fontSize = 13.sp)
        Text(
            text = value,
            color = highlightColor,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
