package com.example.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.MockDataManager
import com.example.data.models.Location
import com.example.data.models.Studio
import com.example.di.ServiceLocator
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProfileScreen(
    onNavigate: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val ownerId = AuthManager.getCurrentUser() ?: "default_owner"
    val userProfile = AuthManager.getUserProfile()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var studio by remember {
        mutableStateOf(
            MockDataManager.getStudioByOwnerId(ownerId) ?: Studio(
                id = UUID.randomUUID().toString(),
                ownerId = ownerId,
                name = if (userProfile.name.isNotBlank()) "${userProfile.name}'s Studio" else "Royal Shutter Studios",
                city = "Prayagraj",
                area = "Civil Lines",
                phone = if (userProfile.mobile.isNotBlank()) userProfile.mobile else "+91 98765 43210",
                email = if (userProfile.email.isNotBlank()) userProfile.email else "owner@studionear.com",
                startingPrice = 45000.0,
                startingPackagePrice = 45000.0,
                services = listOf("Wedding Photography", "Candid", "Cinematic Film", "Drone", "Pre-Wedding"),
                servicesOffered = listOf("Wedding Photography", "Candid", "Cinematic Film", "Drone", "Pre-Wedding"),
                verified = true,
                isVerified = true
            )
        )
    }

    // Form fields
    var studioName by remember { mutableStateOf(studio.name) }
    var ownerName by remember { mutableStateOf(if (userProfile.name.isNotBlank()) userProfile.name else "Studio Director") }
    var phone by remember { mutableStateOf(if (studio.phone.isNotBlank()) studio.phone else userProfile.mobile) }
    var email by remember { mutableStateOf(if (studio.email.isNotBlank()) studio.email else userProfile.email) }
    var city by remember { mutableStateOf(if (studio.city.isNotBlank()) studio.city else "Prayagraj") }
    var selectedArea by remember { mutableStateOf(if (studio.area.isNotBlank()) studio.area else "Civil Lines") }
    var customAreaInput by remember { mutableStateOf("") }
    var address by remember { mutableStateOf(studio.location?.address ?: "Near Subhash Chauraha, Civil Lines, Prayagraj") }
    var description by remember {
        mutableStateOf(
            if (studio.description.isNotBlank()) studio.description 
            else "Premier luxury wedding photography and cinematic storytelling studio in Prayagraj with over a decade of excellence."
        )
    }
    var experienceYears by remember { mutableStateOf(if (studio.experienceYears > 0) studio.experienceYears.toString() else "8") }
    var startingPrice by remember {
        mutableStateOf(
            if (studio.startingPrice > 0.0) studio.startingPrice.toInt().toString()
            else if (studio.startingPackagePrice > 0.0) studio.startingPackagePrice.toInt().toString()
            else "45000"
        )
    }
    var workingHours by remember { mutableStateOf(if (studio.workingHours.isNotBlank()) studio.workingHours else "09:30 AM - 08:30 PM") }
    var deliveryTimeDays by remember { mutableStateOf(if (studio.deliveryTimeDays > 0) studio.deliveryTimeDays.toString() else "30") }

    // Selected specialties / services
    val initialSpecialties = if (studio.services.isNotEmpty()) studio.services else studio.servicesOffered
    var selectedServices by remember { mutableStateOf(initialSpecialties.toSet()) }
    var newCustomTag by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var isLoadingInitial by remember { mutableStateOf(false) }

    // Fetch latest profile from Firestore on screen launch
    LaunchedEffect(ownerId) {
        isLoadingInitial = true
        try {
            val remoteStudio = ServiceLocator.studioRepository.getStudioByOwnerId(ownerId)
            if (remoteStudio != null) {
                studio = remoteStudio
                studioName = remoteStudio.name
                if (remoteStudio.contactPerson.isNotBlank()) {
                    ownerName = remoteStudio.contactPerson
                }
                phone = if (remoteStudio.phone.isNotBlank()) remoteStudio.phone else phone
                email = if (remoteStudio.email.isNotBlank()) remoteStudio.email else email
                city = if (remoteStudio.city.isNotBlank()) remoteStudio.city else "Prayagraj"
                selectedArea = if (remoteStudio.area.isNotBlank()) remoteStudio.area else "Civil Lines"
                address = remoteStudio.location?.address ?: address
                description = if (remoteStudio.description.isNotBlank()) remoteStudio.description else description
                experienceYears = if (remoteStudio.experienceYears > 0) remoteStudio.experienceYears.toString() else "8"
                startingPrice = if (remoteStudio.startingPrice > 0.0) remoteStudio.startingPrice.toInt().toString() else startingPrice
                workingHours = if (remoteStudio.workingHours.isNotBlank()) remoteStudio.workingHours else workingHours
                deliveryTimeDays = if (remoteStudio.deliveryTimeDays > 0) remoteStudio.deliveryTimeDays.toString() else "30"
                val specs = if (remoteStudio.services.isNotEmpty()) remoteStudio.services else remoteStudio.servicesOffered
                if (specs.isNotEmpty()) {
                    selectedServices = specs.toSet()
                }
            }
        } catch (e: Exception) {
            // fallback to initial state
        } finally {
            isLoadingInitial = false
        }
    }

    // Errors
    var studioNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    val prayagrajAreas = listOf(
        "Civil Lines", "Katra", "Ashok Nagar", "George Town", "Chowk",
        "Naini", "Rajrooppur", "Kareli", "Tagore Town", "Allahpur", "Other"
    )

    val availableSpecialties = listOf(
        "Wedding Photography", "Candid", "Cinematic Film", "Drone", 
        "Pre-Wedding", "Traditional", "Haldi & Mehndi", "Engagement", 
        "Maternity", "Album Design", "Fashion Shoot"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Studio Profile", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Text("Manage information & Prayagraj location", fontSize = 12.sp, color = ThemeOnSurfaceVariant)
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ThemeOnBackground)
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onNavigate("owner_packages")
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Collections, contentDescription = null, tint = ThemePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Packages", color = ThemePrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ThemeBackground,
                    titleContentColor = ThemeOnBackground
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ThemeSurface,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            // Validation
                            var hasError = false
                            if (studioName.trim().isBlank()) {
                                studioNameError = "Studio name cannot be empty"
                                hasError = true
                            } else {
                                studioNameError = null
                            }

                            if (phone.trim().isBlank() || phone.trim().length < 8) {
                                phoneError = "Please enter a valid phone number"
                                hasError = true
                            } else {
                                phoneError = null
                            }

                            if (email.trim().isBlank() || !email.contains("@")) {
                                emailError = "Please enter a valid email address"
                                hasError = true
                            } else {
                                emailError = null
                            }

                            val parsedPrice = startingPrice.trim().toDoubleOrNull()
                            if (parsedPrice == null || parsedPrice <= 0) {
                                priceError = "Please enter a valid positive starting price"
                                hasError = true
                            } else {
                                priceError = null
                            }

                            if (hasError) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please fix the highlighted fields before saving.")
                                }
                                return@Button
                            }

                            val effectiveArea = if (selectedArea == "Other" && customAreaInput.isNotBlank()) {
                                customAreaInput.trim()
                            } else {
                                selectedArea
                            }

                            val updatedStudio = studio.copy(
                                name = studioName.trim(),
                                studioName = studioName.trim(),
                                contactPerson = ownerName.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                city = city.trim(),
                                area = effectiveArea,
                                description = description.trim(),
                                bio = description.trim(),
                                experienceYears = experienceYears.trim().toIntOrNull() ?: 5,
                                startingPrice = parsedPrice ?: 45000.0,
                                startingPackagePrice = parsedPrice ?: 45000.0,
                                workingHours = workingHours.trim(),
                                deliveryTimeDays = deliveryTimeDays.trim().toIntOrNull() ?: 30,
                                services = selectedServices.toList(),
                                servicesOffered = selectedServices.toList(),
                                location = Location(
                                    address = address.trim(),
                                    city = city.trim(),
                                    area = effectiveArea,
                                    latitude = 25.4526,
                                    longitude = 81.8349
                                )
                            )

                            isSaving = true
                            scope.launch {
                                try {
                                    // Save to StudioRepository (which updates Firestore and local MockDataManager)
                                    ServiceLocator.studioRepository.saveStudio(updatedStudio)
                                    
                                    // Update user profile
                                    val currentUp = AuthManager.getUserProfile()
                                    AuthManager.saveUserProfile(
                                        currentUp.copy(
                                            name = ownerName.trim(),
                                            mobile = phone.trim(),
                                            email = email.trim()
                                        )
                                    )
                                    
                                    studio = updatedStudio
                                    isSaving = false
                                    snackbarHostState.showSnackbar("Profile updated successfully")
                                } catch (e: Exception) {
                                    isSaving = false
                                    snackbarHostState.showSnackbar("Profile updated locally: ${e.message ?: "Saved"}")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = ThemeOnPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Saving Profile...", fontWeight = FontWeight.Bold, color = ThemeOnPrimary)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = ThemeOnPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Studio Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ThemeOnPrimary)
                        }
                    }
                }
            }
        },
        containerColor = ThemeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (isLoadingInitial) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ThemePrimary,
                    trackColor = ThemeSurfaceVariant
                )
            }
            
            // Verification & Studio Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ThemeOutline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(ThemePrimary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (studio.isVerifiedActive) Icons.Default.Verified else Icons.Default.PendingActions,
                                    contentDescription = null,
                                    tint = if (studio.isVerifiedActive) VerifiedBlue else ThemePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (studio.isVerifiedActive) "Verified Business Studio" else "Standard Studio Profile",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ThemeOnBackground
                                )
                                Text(
                                    text = if (studio.isSponsoredActive) "Sponsored Boost Active • Prayagraj" else "Prayagraj Wedding Hub",
                                    fontSize = 12.sp,
                                    color = if (studio.isSponsoredActive) Color(0xFFE5A93C) else ThemeOnSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (studio.isVerifiedActive) VerifiedBlue.copy(alpha = 0.15f) else Color(0xFFE5A93C).copy(alpha = 0.15f),
                            modifier = Modifier.clickable { onNavigate("owner_monetization") }
                        ) {
                            Text(
                                text = if (studio.isVerifiedActive) "VERIFIED" else "GET BLUE TICK",
                                color = if (studio.isVerifiedActive) VerifiedBlue else Color(0xFFE5A93C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = ThemeOutline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileMetric(
                            title = "Rating",
                            value = "${if (studio.rating > 0f) studio.rating else 4.9} ★",
                            color = Color(0xFFE5A93C)
                        )
                        ProfileMetric(
                            title = "Reviews",
                            value = "${if (studio.reviewCount > 0) studio.reviewCount else 156}",
                            color = ThemeOnBackground
                        )
                        ProfileMetric(
                            title = "Bookings",
                            value = "${if (studio.completedBookings > 0) studio.completedBookings else 240}+",
                            color = ThemePrimary
                        )
                        ProfileMetric(
                            title = "Score",
                            value = "${studio.deliveryPerformanceScore.toInt()}%",
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Section: Basic Info
            SectionHeader(title = "Basic Information", icon = Icons.Default.Business)

            OutlinedTextField(
                value = studioName,
                onValueChange = { 
                    studioName = it
                    if (it.isNotBlank()) studioNameError = null
                },
                label = { Text("Studio Name *") },
                placeholder = { Text("e.g. Royal Shutter Studios") },
                isError = studioNameError != null,
                supportingText = {
                    if (studioNameError != null) Text(studioNameError!!, color = MaterialTheme.colorScheme.error)
                },
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = ThemePrimary) },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Contact Person / Lead Photographer *") },
                placeholder = { Text("e.g. Rahul Sharma") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ThemePrimary) },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it
                        if (it.isNotBlank()) phoneError = null
                    },
                    label = { Text("Mobile Number *") },
                    placeholder = { Text("+91 98765 43210") },
                    isError = phoneError != null,
                    supportingText = {
                        if (phoneError != null) Text(phoneError!!, color = MaterialTheme.colorScheme.error)
                    },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ThemePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        if (it.isNotBlank()) emailError = null
                    },
                    label = { Text("Email Address *") },
                    placeholder = { Text("info@studio.com") },
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) Text(emailError!!, color = MaterialTheme.colorScheme.error)
                    },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ThemePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // Section: Location & Locality in Prayagraj
            SectionHeader(title = "Location in Prayagraj", icon = Icons.Default.LocationOn)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, ThemeOutline, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Select Primary Locality / Hub in Prayagraj:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(prayagrajAreas) { area ->
                        val isSelected = selectedArea.equals(area, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) ThemePrimary else ThemeSurfaceVariant,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline),
                            modifier = Modifier.clickable {
                                selectedArea = area
                            }
                        ) {
                            Text(
                                text = area,
                                color = if (isSelected) ThemeOnPrimary else ThemeOnSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                if (selectedArea == "Other") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customAreaInput,
                        onValueChange = { customAreaInput = it },
                        label = { Text("Specify Locality Name") },
                        placeholder = { Text("e.g. Jhalwa, Dhoomanganj, Phaphamau") },
                        colors = customTextFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Full Studio Address / Landmark") },
                placeholder = { Text("e.g. 2nd Floor, MG Marg, Near Subhash Chauraha, Civil Lines") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = ThemePrimary) },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Section: Photography Specialties / Services
            SectionHeader(title = "Photography Specialties & Services", icon = Icons.Default.CameraAlt)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, ThemeOutline, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Tap to toggle your studio's offerings:",
                    fontSize = 13.sp,
                    color = ThemeOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Flow of chips
                OptInChipsGroup(
                    allOptions = availableSpecialties,
                    selectedOptions = selectedServices,
                    onToggle = { specialty ->
                        selectedServices = if (selectedServices.contains(specialty)) {
                            selectedServices - specialty
                        } else {
                            selectedServices + specialty
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                // Add custom specialty tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCustomTag,
                        onValueChange = { newCustomTag = it },
                        placeholder = { Text("Add custom specialty (e.g. Aerial 360)") },
                        colors = customTextFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newCustomTag.isNotBlank()) {
                                selectedServices = selectedServices + newCustomTag.trim()
                                newCustomTag = ""
                            }
                        }),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCustomTag.isNotBlank()) {
                                selectedServices = selectedServices + newCustomTag.trim()
                                newCustomTag = ""
                            }
                        },
                        modifier = Modifier
                            .background(ThemePrimary, RoundedCornerShape(8.dp))
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Tag", tint = ThemeOnPrimary)
                    }
                }
            }

            // Section: Business & Pricing
            SectionHeader(title = "Pricing & Operational Details", icon = Icons.Default.Payments)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startingPrice,
                    onValueChange = { 
                        startingPrice = it
                        if (it.isNotBlank()) priceError = null
                    },
                    label = { Text("Starting Price (₹) *") },
                    placeholder = { Text("45000") },
                    isError = priceError != null,
                    supportingText = {
                        if (priceError != null) Text(priceError!!, color = MaterialTheme.colorScheme.error)
                    },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = ThemePrimary, fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = { Text("Experience (Years)") },
                    placeholder = { Text("8") },
                    leadingIcon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = ThemePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = deliveryTimeDays,
                    onValueChange = { deliveryTimeDays = it },
                    label = { Text("Avg Delivery (Days)") },
                    placeholder = { Text("30") },
                    leadingIcon = { Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = ThemePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text("Working Hours") },
                    placeholder = { Text("10 AM - 8 PM") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = ThemePrimary) },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // Section: Studio Bio / Description
            SectionHeader(title = "About / Studio Bio", icon = Icons.Default.Description)

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Studio Bio / Overview") },
                placeholder = { Text("Tell prospective couples and clients about your photography vision, equipment, and storytelling philosophy...") },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProfileMetric(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = title, fontSize = 11.sp, color = ThemeOnSurfaceVariant)
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = ThemePrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeOnBackground
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptInChipsGroup(
    allOptions: List<String>,
    selectedOptions: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        allOptions.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) ThemePrimary else ThemeSurfaceVariant,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ThemeOutline),
                modifier = Modifier.clickable { onToggle(option) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = ThemeOnPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = option,
                        color = if (isSelected) ThemeOnPrimary else ThemeOnSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun customTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ThemePrimary,
        unfocusedBorderColor = ThemeOutline,
        focusedContainerColor = ThemeSurface,
        unfocusedContainerColor = ThemeSurface,
        focusedLabelColor = ThemePrimary,
        unfocusedLabelColor = ThemeOnSurfaceVariant,
        focusedTextColor = ThemeOnBackground,
        unfocusedTextColor = ThemeOnBackground,
        cursorColor = ThemePrimary
    )
}
