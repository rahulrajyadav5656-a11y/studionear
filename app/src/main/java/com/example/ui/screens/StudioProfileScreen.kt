package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.Studio
import com.example.data.models.StudioPackage
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.example.data.MockDataManager
import com.example.data.models.Review
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.lifecycle.viewModelScope
import com.example.data.FavoritesManager
import com.example.di.ServiceLocator
import kotlinx.coroutines.launch

class StudioProfileViewModel : ViewModel() {
    private val _studio = MutableStateFlow<Studio?>(null)
    val studio: StateFlow<Studio?> = _studio.asStateFlow()
    
    private val _packages = MutableStateFlow<List<StudioPackage>>(emptyList())
    val packages: StateFlow<List<StudioPackage>> = _packages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadStudioData(studioId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedStudio = ServiceLocator.studioRepository.getStudioById(studioId)
                _studio.value = fetchedStudio

                val fetchedPackages = ServiceLocator.studioRepository.getPackagesForStudio(studioId)
                _packages.value = fetchedPackages
            } catch (e: Exception) {
                // Log or handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioProfileScreen(
    studioId: String,
    onBack: () -> Unit,
    onBookPackage: (String) -> Unit,
    viewModel: StudioProfileViewModel = viewModel()
) {
    LaunchedEffect(studioId) {
        viewModel.loadStudioData(studioId)
    }
    
    val studio by viewModel.studio.collectAsState()
    val packages by viewModel.packages.collectAsState()
    val favoriteIds by FavoritesManager.favoriteStudioIds.collectAsState()
    val isFavorite = favoriteIds.contains(studioId)
    
    val reviews by MockDataManager.reviews.collectAsState()
    val studioReviews = remember(reviews, studioId) {
        MockDataManager.getReviewsForStudio(studioId)
    }
    val avgRating = if (studioReviews.isNotEmpty()) studioReviews.map { it.overallRating }.average().toFloat() else studio?.rating ?: 0f
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedVideoReview by remember { mutableStateOf<Review?>(null) }
    var selectedPortfolioImage by remember { mutableStateOf<String?>(null) }
    
    if (selectedVideoReview != null) {
        VideoPlayerDialog(
            review = selectedVideoReview!!,
            onDismiss = { selectedVideoReview = null }
        )
    }

    if (selectedPortfolioImage != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedPortfolioImage = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                coil.compose.AsyncImage(
                    model = selectedPortfolioImage,
                    contentDescription = "Full Screen Portfolio",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                IconButton(
                    onClick = { selectedPortfolioImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }

    var showBookingSheet by remember { mutableStateOf(false) }
    var eventType by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSubmittingBooking by remember { mutableStateOf(false) }

    if (showBookingSheet && studio != null) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showBookingSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Request Booking / Inquire", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Studio: ${studio!!.name}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                OutlinedTextField(
                    value = eventType,
                    onValueChange = { eventType = it },
                    label = { Text("Event Type (e.g. Wedding, Birthday)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Event Date (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = eventLocation,
                    onValueChange = { eventLocation = it },
                    label = { Text("City / Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            isSubmittingBooking = true
                            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                val newBooking = com.example.data.models.Booking(
                                    clientId = currentUser.uid,
                                    clientEmail = currentUser.email ?: "",
                                    studioId = studio!!.id,
                                    studioOwnerId = studio!!.ownerId,
                                    studioName = studio!!.name,
                                    eventType = eventType,
                                    eventDate = try {
                                        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).parse(eventDate)?.time ?: System.currentTimeMillis()
                                    } catch (e: Exception) {
                                        System.currentTimeMillis()
                                    },
                                    location = eventLocation,
                                    notes = notes,
                                    status = com.example.data.models.BookingStatus.PENDING
                                )
                                com.example.di.ServiceLocator.bookingRepository.createBookingRequest(newBooking)
                                snackbarHostState.showSnackbar("Booking request sent successfully!")
                                showBookingSheet = false
                            } else {
                                snackbarHostState.showSnackbar("Please log in to book.")
                            }
                            isSubmittingBooking = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isSubmittingBooking && eventType.isNotBlank() && eventDate.isNotBlank()
                ) {
                    if (isSubmittingBooking) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Submit Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar(containerColor = ThemeSurface) {
                Button(
                    onClick = { showBookingSheet = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary)
                ) {
                    Text("Request Booking / Inquire", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(studio?.name ?: "Studio Details", maxLines = 1, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val newFav = FavoritesManager.toggleFavorite(studioId)
                                val studioName = studio?.name ?: "Studio"
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    message = if (newFav) "$studioName added to favorites" else "$studioName removed from favorites",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (studio == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header details
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = studio!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (studio!!.isVerifiedActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified Studio", tint = VerifiedBlue)
                        }
                        if (studio!!.isSponsoredActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFE5A93C),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SPONSORED",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        val displayRating = if (avgRating > 0) String.format(java.util.Locale.US, "%.1f", avgRating) else "New"
                        Text(
                            text = "$displayRating (${studio!!.reviewCount + studioReviews.size} reviews)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Actions
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { com.example.ui.utils.ContactUtils.openDialer(context, studio!!.phone) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimaryContainer.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call Studio",
                                tint = ThemePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Call Studio",
                                color = ThemePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { com.example.ui.utils.ContactUtils.openWhatsApp(context, studio!!.phone) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366).copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp Studio",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WhatsApp",
                                color = Color(0xFF25D366),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                item { HorizontalDivider() }
                
                // Portfolio Section
                item {
                    Text(
                        text = "Portfolio & Past Work",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (studio!!.portfolioUrls.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No portfolio samples uploaded yet.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                } else {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(studio!!.portfolioUrls) { url ->
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedPortfolioImage = url }
                                ) {
                                    coil.compose.AsyncImage(
                                        model = url,
                                        contentDescription = "Portfolio Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                item { HorizontalDivider() }
                
                // Packages
                item {
                    Text(
                        text = "Packages",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (packages.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No packages listed yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "This studio hasn't added customized packages yet. Contact them directly to enquire.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                items(packages) { pkg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(pkg.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("₹${pkg.price.toInt()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(pkg.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Features
                            val features = mutableListOf<String>()
                            if (pkg.includesPhotography) features.add("Photography")
                            if (pkg.includesVideography) features.add("Videography")
                            if (pkg.includesCinematicFilm) features.add("Cinematic Film")
                            if (pkg.includesDrone) features.add("Drone")
                            if (pkg.includesAlbum) features.add("Album")
                            
                            Text("Includes: ${features.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                            Text("Functions: Up to ${pkg.maxFunctions}", style = MaterialTheme.typography.bodySmall)
                            Text("Delivery: ${pkg.deliveryTimelineDays} days", style = MaterialTheme.typography.bodySmall)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onBookPackage(pkg.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Book Package")
                            }
                        }
                    }
                }
                
                // Client Video Reviews Section
                val videoReviews = studioReviews.filter { it.videoUri != null }
                if (videoReviews.isNotEmpty()) {
                    item { HorizontalDivider() }
                    item {
                        Text(
                            text = "Client Video Reviews",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(videoReviews.size) { index ->
                                val review = videoReviews[index]
                                Box(
                                    modifier = Modifier
                                        .size(width = 120.dp, height = 180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.DarkGray)
                                        .clickable { selectedVideoReview = review },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayCircleOutline, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(48.dp))
                                    Text(
                                        "★ ${review.overallRating}",
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Reviews Section
                if (studioReviews.isNotEmpty()) {
                    item { HorizontalDivider() }
                    item {
                        Text(
                            text = "Verified Reviews",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(studioReviews.size) { index ->
                        val review = studioReviews[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(review.clientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                
                                if (review.verifiedBooking) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Verified Booking", color = SuccessColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(review.writtenReview, style = MaterialTheme.typography.bodyMedium)
                                
                                if (review.videoUri != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { selectedVideoReview = review }
                                    ) {
                                        Icon(Icons.Default.PlayCircleOutline, contentDescription = "Play Video", tint = ThemePrimary, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Watch Video Review", color = ThemePrimary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
