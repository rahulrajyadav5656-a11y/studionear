package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataManager
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(onBack: () -> Unit) {
    val allReviews by MockDataManager.reviews.collectAsState()
    val currentUser = com.example.data.AuthManager.getCurrentUser() ?: ""
    val myReviews = allReviews.filter { it.clientId == currentUser || it.clientName == "Client Name" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reviews", fontWeight = FontWeight.Bold) },
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
        if (myReviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(64.dp), tint = ThemeOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No reviews submitted yet", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = ThemeOnSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(myReviews) { review ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(review.studioName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Row {
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = if (i <= review.overallRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null,
                                            tint = StarColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val dateStr = sdf.format(Date(review.createdAt))
                            Text("Booking ID: ${review.bookingId} • $dateStr", color = ThemeOnSurfaceVariant, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                if (review.verifiedBooking) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Verified Booking", color = SuccessColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                                Text(
                                    text = review.status.name, 
                                    color = if(review.status.name == "APPROVED") SuccessColor else ThemePrimary, 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(review.writtenReview, fontSize = 14.sp)
                            
                            if (review.videoUri != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Video review attached", color = ThemePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
