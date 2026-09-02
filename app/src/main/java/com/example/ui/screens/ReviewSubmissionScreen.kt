package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.MockDataManager
import com.example.data.models.Review
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSubmissionScreen(
    bookingId: String,
    studioId: String,
    studioName: String,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    
    var step by remember { mutableStateOf(1) } // 1: Rating, 2: Media, 3: Confirm
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        videoUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate & Review", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (step) {
                1 -> {
                    Text("How was your experience with $studioName?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Star Rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Star $i",
                                tint = StarColor,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { rating = i }
                                    .padding(4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Write your review") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = rating > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Next", color = ThemeOnPrimary)
                    }
                }
                2 -> {
                    Text("Add a Video Review (Optional)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Share your authentic experience. Verified video reviews help other couples choose the right studio.", color = ThemeOnSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (videoUri == null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable { videoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.VideoFile, contentDescription = "Upload Video", modifier = Modifier.size(48.dp), tint = ThemeOnSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Tap to select a video", color = ThemeOnSurfaceVariant)
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = SuccessColor, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Video selected", color = ThemeOnBackground)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(onClick = { videoUri = null }) {
                                        Text("Remove", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "By uploading this video, you confirm that you have the necessary rights/permission to share it and agree to its display according to StudioNear's review policy.",
                            fontSize = 12.sp,
                            color = ThemeOnSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text("Back")
                        }
                        Button(
                            onClick = { step = 3 },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Review", color = ThemeOnPrimary)
                        }
                    }
                }
                3 -> {
                    Text("Confirm Your Review", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Studio:", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                            Text(studioName, color = ThemeOnBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("Your Rating:", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                            Row {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = null,
                                        tint = StarColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (reviewText.isNotBlank()) {
                                Text("Your Review:", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                                Text(reviewText, color = ThemeOnBackground, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            if (videoUri != null) {
                                Text("Video:", color = ThemeOnSurfaceVariant, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Video Attached (Pending Upload)", color = ThemeOnBackground, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (isUploading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(color = ThemePrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Uploading review...", color = ThemeOnSurfaceVariant)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = { step = 2 },
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Text("Edit")
                            }
                            Button(
                                onClick = {
                                    isUploading = true
                                    scope.launch {
                                        // Simulate upload delay
                                        delay(2000)
                                        val review = Review(
                                            bookingId = bookingId,
                                            studioId = studioId,
                                            clientId = AuthManager.getCurrentUser() ?: "Unknown",
                                            clientName = "Client Name", // Or fetch from Auth
                                            studioName = studioName,
                                            overallRating = rating.toFloat(),
                                            writtenReview = reviewText,
                                            videoUri = videoUri?.toString(),
                                            status = com.example.data.models.ReviewStatus.APPROVED
                                        )
                                        MockDataManager.submitReview(review)
                                        isUploading = false
                                        onSubmitSuccess()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Submit Review", color = ThemeOnPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
