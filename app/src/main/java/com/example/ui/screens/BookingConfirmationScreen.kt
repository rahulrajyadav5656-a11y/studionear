package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BookingConfirmationScreen(
    bookingId: String,
    onViewInvoice: () -> Unit,
    onViewTerms: () -> Unit,
    onGoHome: () -> Unit
) {
    Scaffold(
        containerColor = ThemeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = ThemePrimary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Booking Confirmed!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your booking has been successfully placed.", color = ThemeOnSurfaceVariant, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    DetailRow("Booking ID", bookingId)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = ThemeOnSurfaceVariant.copy(alpha = 0.2f))
                    DetailRow("Studio", "Pixel Perfect Studios")
                    DetailRow("Event Date", "25 Oct 2026")
                    DetailRow("Package", "Premium Wedding Story")
                    DetailRow("Total Amount", "₹1,20,000")
                    DetailRow("Payment Status", "Pending Advance")
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = onViewInvoice,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Invoice", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onViewTerms,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ThemePrimary)
            ) {
                Text("View Terms & Conditions", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ThemePrimary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Go to Home", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ThemeOnBackground)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ThemeOnSurfaceVariant, fontSize = 14.sp)
        Text(value, color = ThemeOnBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
