package com.example.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.MockDataManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerReviewsScreen(onBack: () -> Unit) {
    val ownerId = AuthManager.getCurrentUser() ?: return
    val studio = MockDataManager.getStudioByOwnerId(ownerId) ?: return
    
    val allReviews by MockDataManager.reviews.collectAsState()
    val studioReviews = allReviews.filter { it.studioId == studio.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews & Ratings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { padding ->
        if (studioReviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Reviews will appear after completed bookings.", color = ThemeOnSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Average Rating", fontSize = 16.sp, color = ThemeOnSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${studio.rating} ★", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ThemePrimary)
                            Text("Based on ${studio.reviewCount} reviews", fontSize = 14.sp, color = ThemeOnSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                items(studioReviews) { review ->
                    var showReplyDialog by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(review.clientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Row {
                                    Icon(Icons.Default.Star, contentDescription = "Star", tint = ThemePrimary, modifier = Modifier.size(16.dp))
                                    Text(" ${review.overallRating}", fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(review.writtenReview, color = ThemeOnBackground)
                            
                            if (!review.ownerReply.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Your Reply:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(review.ownerReply, fontSize = 14.sp)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { showReplyDialog = true }) {
                                    Text("Reply to Review")
                                }
                            }
                        }
                    }
                    
                    if (showReplyDialog) {
                        var replyText by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showReplyDialog = false },
                            title = { Text("Reply to Review") },
                            text = {
                                OutlinedTextField(
                                    value = replyText,
                                    onValueChange = { replyText = it },
                                    label = { Text("Your Reply") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                            },
                            confirmButton = {
                                Button(onClick = {
                                    if (replyText.isNotBlank()) {
                                        MockDataManager.replyToReview(review.id, replyText)
                                        showReplyDialog = false
                                    }
                                }) { Text("Submit Reply") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showReplyDialog = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
        }
    }
}
