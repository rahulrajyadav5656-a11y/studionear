package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    bookingId: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
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
                Text(
                    text = "STUDIONEAR\nBOOKING TERMS & CONDITIONS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeOnBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                TermsSection("BOOKING INFORMATION")
                TermsText("Booking ID: $bookingId")
                TermsText("Client Name: Rahul Yadav")
                TermsText("Studio Name: Pixel Perfect Studios")
                TermsText("Event Date: 25 Nov 2026")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TermsSection("PACKAGE & DELIVERABLES")
                TermsText("Selected Package: Premium Wedding Story")
                TermsText("Included: Cinematic Film, Drone Coverage, Candid Photography, Traditional Photography, Pre-wedding Shoot, 2 Premium Albums.")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TermsSection("PAYMENT TERMS")
                TermsText("Total Amount: ₹1,20,000")
                TermsText("Milestones:\n- 20% Advance to confirm booking.\n- 60% on the day of the event.\n- 20% before final delivery of assets.")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TermsSection("CANCELLATION & RESCHEDULING")
                TermsText("Cancellation: Advance payments are non-refundable if cancelled within 30 days of the event date. Cancellations prior to 30 days are eligible for a 50% refund of the advance.")
                TermsText("Rescheduling: Allowed once without penalty subject to studio availability.")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TermsSection("DELIVERY TERMS")
                TermsText("Raw photos will be provided within 7 days of the event. Edited photos and cinematic films will be delivered within 30-45 days post-event.")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TermsSection("DATA RETENTION POLICY")
                TermsText("The Studio will retain raw and edited files for 6 months after delivery. After this period, files may be permanently archived or deleted. Clients are responsible for downloading and backing up files.")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TermsSection("CLIENT RESPONSIBILITIES")
                TermsText("- Provide accurate event timelines and schedules.")
                TermsText("- Ensure permissions and access for photography at venues.")
                TermsText("- Adhere to the payment schedule.")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TermsSection("STUDIO RESPONSIBILITIES")
                TermsText("- Provide all agreed services professionally.")
                TermsText("- Deliver the specified deliverables within the agreed timeline.")
                TermsText("- Communicate any unavoidable material delays appropriately.")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TermsSection("DISPUTES & COMPLAINTS")
                TermsText("Any disputes or complaints must be raised through the StudioNear platform within 15 days of final delivery. StudioNear will act as a mediator but is not liable for service failures by independent studios.")
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Divider(color = ThemeOnSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Status: ACCEPTED",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemePrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Accepted on: 25 Oct 2026",
                    fontSize = 12.sp,
                    color = ThemeOnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TermsSection(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = ThemeOnBackground,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
private fun TermsText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = ThemeOnSurfaceVariant,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
