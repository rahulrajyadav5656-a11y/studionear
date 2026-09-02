package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    bookingId: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Invoice", fontWeight = FontWeight.Bold) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("STUDIONEAR", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ThemePrimary)
                    Text("Booking Invoice", fontSize = 16.sp, color = ThemeOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                InvoiceSectionTitle("INVOICE DETAILS")
                InvoiceRow("Invoice Number", "INV-${bookingId.takeLast(4)}")
                InvoiceRow("Invoice Date", "25 Oct 2026")
                InvoiceRow("Booking ID", bookingId)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                InvoiceSectionTitle("CLIENT DETAILS")
                InvoiceRow("Name", "Rahul Yadav")
                InvoiceRow("Mobile", "+91 9876543210")
                InvoiceRow("Email", "rahul@example.com")
                InvoiceRow("Address", "Civil Lines, Prayagraj")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                InvoiceSectionTitle("STUDIO DETAILS")
                InvoiceRow("Studio Name", "Pixel Perfect Studios")
                InvoiceRow("Owner", "Amit Kumar")
                InvoiceRow("Mobile", "+91 8765432109")
                InvoiceRow("Address", "MG Marg, Prayagraj")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                InvoiceSectionTitle("EVENT DETAILS")
                InvoiceRow("Event Type", "Wedding & Reception")
                InvoiceRow("Event Date", "25 Nov 2026")
                InvoiceRow("Event Time", "06:00 PM - 02:00 AM")
                InvoiceRow("Location", "Grand Hotel, Prayagraj")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                InvoiceSectionTitle("PACKAGE DETAILS")
                InvoiceRow("Package", "Premium Wedding Story")
                InvoiceRow("Services", "Cinematic Film, Drone, Candid, Traditional, Album")
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = ThemeOnSurfaceVariant.copy(alpha = 0.2f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Amount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground)
                    Text("₹1,20,000", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemePrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Amount Paid", fontSize = 14.sp, color = ThemeOnSurfaceVariant)
                    Text("₹0", fontSize = 14.sp, color = ThemeOnSurfaceVariant)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Amount Remaining", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ThemeOnBackground)
                    Text("₹1,20,000", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ThemeOnBackground)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Payment Status: PENDING",
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        color = ThemePrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { /* Download */ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Download PDF")
                    }
                    Button(
                        onClick = { /* Share */ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Share Invoice")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InvoiceSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = ThemePrimary,
        modifier = Modifier.padding(bottom = 8.dp),
        letterSpacing = 1.sp
    )
}

@Composable
private fun InvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = ThemeOnSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            color = ThemeOnBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
