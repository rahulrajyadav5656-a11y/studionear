package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BookingQuote
import com.example.model.ServiceRate
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteBuilderSheet(
    studioId: String,
    studioName: String,
    onDismiss: () -> Unit,
    onBookingSubmitted: (String) -> Unit
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var isSubmitting by remember { mutableStateOf(false) }

    // Owner services rates state
    var services by remember {
        mutableStateOf(
            listOf(
                ServiceRate("1", "Traditional Photography", "Standard stage & family coverage", 8000.0, true),
                ServiceRate("2", "Candid Photography", "Creative portrait & emotional candid shots", 14000.0, false),
                ServiceRate("3", "Cinematic Teaser & Video", "High-end cinematic video with gimbal", 18000.0, false),
                ServiceRate("4", "Drone Aerial Coverage", "4K Drone aerial shots per day", 9000.0, false)
            )
        )
    }

    var numberOfDays by remember { mutableIntStateOf(1) }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }

    // Realtime Calculations
    val selectedServices = services.filter { it.isSelected }
    val dailyRate = selectedServices.sumOf { it.pricePerDay }
    val subtotal = dailyRate * numberOfDays
    val platformFee = if (subtotal > 0) 500.0 else 0.0
    val totalAmount = subtotal + platformFee
    val advanceRequired = totalAmount * 0.25 // 25% Advance

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E28)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Customize Quote - $studioName",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select services & days to calculate estimated budget",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SELECT SERVICES",
                    color = Color(0xFF8B5CF6),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Services Checkboxes
            items(services) { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            services = services.map {
                                if (it.id == service.id) it.copy(isSelected = !it.isSelected) else it
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (service.isSelected) Color(0xFF2E244D) else Color(0xFF262635)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(service.description, color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${service.pricePerDay.toInt()}/day", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Checkbox(
                            checked = service.isSelected,
                            onCheckedChange = { checked ->
                                services = services.map {
                                    if (it.id == service.id) it.copy(isSelected = checked) else it
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF8B5CF6),
                                checkmarkColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Number of Days Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Duration (Days)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("E.g. Sangeet + Wedding = 2 Days", color = Color.Gray, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (numberOfDays > 1) numberOfDays-- },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E2E3E))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                        }
                        Text(
                            text = "$numberOfDays",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = { numberOfDays++ },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF8B5CF6))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Client Inputs
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Your Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF38384E)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Contact Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF38384E)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Shoot Date (e.g. 24 Nov 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF38384E)
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Price Summary Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14141D))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Services Total (${selectedServices.size} selected x $numberOfDays days)", color = Color.Gray, fontSize = 12.sp)
                            Text("₹${subtotal.toInt()}", color = Color.White, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Platform & Booking Support", color = Color.Gray, fontSize = 12.sp)
                            Text("₹${platformFee.toInt()}", color = Color.White, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFF2E2E3E), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Estimated Amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹${totalAmount.toInt()}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("25% Advance to Confirm", color = Color(0xFFF59E0B), fontSize = 12.sp)
                            Text("₹${advanceRequired.toInt()}", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Booking Button
                Button(
                    onClick = {
                        if (selectedServices.isNotEmpty() && clientName.isNotBlank() && clientPhone.isNotBlank()) {
                            isSubmitting = true
                            val quote = BookingQuote(
                                studioId = studioId,
                                studioName = studioName,
                                clientName = clientName.trim(),
                                clientPhone = clientPhone.trim(),
                                eventDate = eventDate.trim(),
                                selectedServices = selectedServices,
                                numberOfDays = numberOfDays,
                                subtotal = subtotal,
                                platformFee = platformFee,
                                totalAmount = totalAmount,
                                advanceRequired = advanceRequired,
                                status = "PENDING",
                                createdAt = System.currentTimeMillis()
                            )

                            firestore.collection("bookings")
                                .add(quote)
                                .addOnSuccessListener { docRef ->
                                    isSubmitting = false
                                    onBookingSubmitted(docRef.id)
                                }
                                .addOnFailureListener {
                                    isSubmitting = false
                                }
                        }
                    },
                    enabled = !isSubmitting && selectedServices.isNotEmpty() && clientName.isNotBlank() && clientPhone.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Booking & Lock Dates", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
