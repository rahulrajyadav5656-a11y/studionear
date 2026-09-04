package com.example.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.AuthManager
import com.example.data.models.Studio
import com.example.data.models.StudioPackage
import com.example.di.ServiceLocator
import com.example.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPackagesScreen(onBack: () -> Unit) {
    val ownerId = AuthManager.getCurrentUser() ?: ""
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var studio by remember {
        mutableStateOf<Studio?>(null)
    }

    var isOperatingOnPackage by remember { mutableStateOf(false) }
    var isPackagesLoading by remember { mutableStateOf(false) }

    // Fetch Studio details live from Firestore
    LaunchedEffect(ownerId) {
        if (ownerId.isNotBlank()) {
            isPackagesLoading = true
            try {
                val remoteStudio = ServiceLocator.studioRepository.getStudioByOwnerId(ownerId)
                if (remoteStudio != null) {
                    studio = remoteStudio
                    ServiceLocator.studioRepository.getPackagesForStudio(remoteStudio.id)
                }
            } catch (_: Exception) {
            } finally {
                isPackagesLoading = false
            }
        }
    }

    val currentStudioId = studio?.id ?: ownerId
    val packagesFlow = remember(currentStudioId) {
        ServiceLocator.studioRepository.observePackagesForStudio(currentStudioId)
    }
    val studioPackages by packagesFlow.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPackage by remember { mutableStateOf<StudioPackage?>(null) }
    var packageToDelete by remember { mutableStateOf<StudioPackage?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Packages & Pricing", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Text("${studioPackages.size} packages active for booking", fontSize = 12.sp, color = ThemeOnSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ThemeOnBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Package", tint = ThemePrimary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ThemeBackground,
                    titleContentColor = ThemeOnBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ThemePrimary,
                contentColor = ThemeOnPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Package", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = ThemeBackground
    ) { padding ->
        if (studioPackages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(ThemeSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CollectionsBookmark, contentDescription = null, tint = ThemePrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Packages Created Yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Create photography packages to allow clients to calculate custom quotes and book your studio.",
                        fontSize = 13.sp,
                        color = ThemeOnSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create First Package", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isPackagesLoading) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = ThemePrimary,
                            trackColor = ThemeSurfaceVariant
                        )
                    }
                }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ThemeOutline, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(ThemePrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Sell, contentDescription = null, tint = ThemePrimary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Client-Facing Pricing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ThemeOnBackground)
                                Text(
                                    "Packages appear directly in client quote builder and booking requests.",
                                    fontSize = 12.sp,
                                    color = ThemeOnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(studioPackages, key = { it.id }) { pkg ->
                    OwnerPackageCard(
                        pkg = pkg,
                        onEdit = { editingPackage = pkg },
                        onDuplicate = {
                            val duplicated = pkg.copy(
                                id = "pkg_${UUID.randomUUID()}",
                                name = "${pkg.name} (Copy)"
                            )
                            scope.launch {
                                ServiceLocator.studioRepository.savePackage(duplicated)
                                // Sync duplicate to Firestore rates collection for QuoteBuilder
                                firestore.collection("studios").document(currentStudioId)
                                    .collection("rates").document(duplicated.id)
                                    .set(mapOf(
                                        "name" to duplicated.name,
                                        "description" to duplicated.deliverables,
                                        "pricePerDay" to duplicated.price
                                    ))
                                snackbarHostState.showSnackbar("Package duplicated successfully!")
                            }
                        },
                        onDelete = { packageToDelete = pkg }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (packageToDelete != null) {
        val target = packageToDelete!!
        AlertDialog(
            onDismissRequest = { if (!isOperatingOnPackage) packageToDelete = null },
            containerColor = ThemeSurface,
            title = {
                Text("Delete Package?", fontWeight = FontWeight.Bold, color = ThemeOnBackground)
            },
            text = {
                Text("Are you sure you want to delete '${target.name}'? Clients will no longer be able to select this package.", color = ThemeOnSurfaceVariant)
            },
            confirmButton = {
                Button(
                    onClick = {
                        isOperatingOnPackage = true
                        scope.launch {
                            try {
                                ServiceLocator.studioRepository.deletePackage(target.id)
                                // Remove from Firestore rates subcollection
                                firestore.collection("studios").document(currentStudioId)
                                    .collection("rates").document(target.id)
                                    .delete()
                                packageToDelete = null
                                snackbarHostState.showSnackbar("Package deleted successfully")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Delete error: ${e.message ?: ""}")
                            } finally {
                                isOperatingOnPackage = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !isOperatingOnPackage
                ) {
                    if (isOperatingOnPackage) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onError, strokeWidth = 2.dp)
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { packageToDelete = null },
                    enabled = !isOperatingOnPackage
                ) {
                    Text("Cancel", color = ThemeOnSurfaceVariant)
                }
            }
        )
    }

    // Add / Edit Package Dialog
    if (showAddDialog || editingPackage != null) {
        PackageEditDialog(
            studioId = currentStudioId,
            existingPkg = editingPackage,
            isSaving = isOperatingOnPackage,
            onDismiss = {
                if (!isOperatingOnPackage) {
                    showAddDialog = false
                    editingPackage = null
                }
            },
            onSave = { pkg ->
                isOperatingOnPackage = true
                scope.launch {
                    try {
                        ServiceLocator.studioRepository.savePackage(pkg)
                        // Sync directly with Firestore rates subcollection for QuoteBuilderSheet
                        firestore.collection("studios").document(currentStudioId)
                            .collection("rates").document(pkg.id)
                            .set(mapOf(
                                "name" to pkg.name,
                                "description" to pkg.deliverables,
                                "pricePerDay" to pkg.price
                            ))
                        showAddDialog = false
                        editingPackage = null
                        snackbarHostState.showSnackbar("Package saved and synchronized with live quote builder!")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Save error: ${e.message ?: ""}")
                    } finally {
                        isOperatingOnPackage = false
                    }
                }
            }
        )
    }
}

@Composable
fun PackageEditDialog(
    studioId: String,
    existingPkg: StudioPackage?,
    isSaving: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (StudioPackage) -> Unit
) {
    var name by remember { mutableStateOf(existingPkg?.name ?: "") }
    var price by remember { mutableStateOf(if (existingPkg != null && existingPkg.price > 0.0) existingPkg.price.toInt().toString() else "") }
    var maxFunctions by remember { mutableStateOf(existingPkg?.maxFunctions?.toString() ?: "1") }
    var deliveryTimelineDays by remember { mutableStateOf(existingPkg?.deliveryTimelineDays?.toString() ?: "30") }
    var deliverables by remember { 
        mutableStateOf(
            existingPkg?.deliverables ?: "Edited Photos, Highlight Video Coverage"
        ) 
    }
    var description by remember { mutableStateOf(existingPkg?.description ?: "") }
    var terms by remember { mutableStateOf(existingPkg?.terms ?: "") }

    var includesPhotography by remember { mutableStateOf(existingPkg?.includesPhotography ?: true) }
    var includesVideography by remember { mutableStateOf(existingPkg?.includesVideography ?: true) }
    var includesCandidPhotography by remember { mutableStateOf(existingPkg?.includesCandidPhotography ?: true) }
    var includesCinematicFilm by remember { mutableStateOf(existingPkg?.includesCinematicFilm ?: false) }
    var includesDrone by remember { mutableStateOf(existingPkg?.includesDrone ?: false) }
    var includesAlbum by remember { mutableStateOf(existingPkg?.includesAlbum ?: false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.92f),
        containerColor = ThemeSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (existingPkg == null) "Create New Package" else "Edit Package",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ThemeOnBackground
                    )
                    Text("Define pricing and deliverables", fontSize = 12.sp, color = ThemeOnSurfaceVariant)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ThemeOnSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Package Title *") },
                    placeholder = { Text("e.g. Traditional Photography, Cinematic Wedding") },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error)
                    },
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = ThemePrimary) },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { 
                            price = it
                            if (it.isNotBlank()) priceError = null
                        },
                        label = { Text("Price Per Day (₹) *") },
                        placeholder = { Text("15000") },
                        isError = priceError != null,
                        supportingText = {
                            if (priceError != null) Text(priceError!!, color = MaterialTheme.colorScheme.error)
                        },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = ThemePrimary, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = customTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = maxFunctions,
                        onValueChange = { maxFunctions = it },
                        label = { Text("Days / Events") },
                        placeholder = { Text("1") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ThemePrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = customTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.9f)
                    )
                }

                OutlinedTextField(
                    value = deliveryTimelineDays,
                    onValueChange = { deliveryTimelineDays = it },
                    label = { Text("Delivery Timeline (Days)") },
                    placeholder = { Text("30") },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = ThemePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deliverables,
                    onValueChange = { deliverables = it },
                    label = { Text("Key Deliverables *") },
                    placeholder = { Text("e.g. 300 Edited Photos, 1 Traditional Video, 1 Teaser") },
                    leadingIcon = { Icon(Icons.Default.TaskAlt, contentDescription = null, tint = ThemePrimary) },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Package Description") },
                    placeholder = { Text("Detailed description for client reference...") },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Text(
                    text = "Included Services:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ThemeOnBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, ThemeOutline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    PackageCheckboxRow("Traditional Photography", includesPhotography) { includesPhotography = it }
                    PackageCheckboxRow("Traditional Videography", includesVideography) { includesVideography = it }
                    PackageCheckboxRow("Candid Photography", includesCandidPhotography) { includesCandidPhotography = it }
                    PackageCheckboxRow("Cinematic Film / Teaser", includesCinematicFilm) { includesCinematicFilm = it }
                    PackageCheckboxRow("4K Drone Aerial Coverage", includesDrone) { includesDrone = it }
                    PackageCheckboxRow("Luxury Hardcover Photo Album", includesAlbum) { includesAlbum = it }
                }

                OutlinedTextField(
                    value = terms,
                    onValueChange = { terms = it },
                    label = { Text("Payment Terms") },
                    placeholder = { Text("25% Advance booking deposit") },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isBlank()) {
                        nameError = "Package title is required"
                        return@Button
                    }
                    val parsedPrice = price.trim().toDoubleOrNull()
                    if (parsedPrice == null || parsedPrice <= 0) {
                        priceError = "Enter a valid price"
                        return@Button
                    }

                    val newPkg = StudioPackage(
                        id = existingPkg?.id ?: "pkg_${UUID.randomUUID()}",
                        studioId = studioId,
                        name = name.trim(),
                        description = description.trim(),
                        deliverables = deliverables.trim(),
                        price = parsedPrice,
                        maxFunctions = maxFunctions.trim().toIntOrNull() ?: 1,
                        deliveryTimelineDays = deliveryTimelineDays.trim().toIntOrNull() ?: 30,
                        includesPhotography = includesPhotography,
                        includesVideography = includesVideography,
                        includesCandidPhotography = includesCandidPhotography,
                        includesCinematicFilm = includesCinematicFilm,
                        includesDrone = includesDrone,
                        includesAlbum = includesAlbum,
                        terms = terms.trim()
                    )
                    onSave(newPkg)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                shape = RoundedCornerShape(10.dp),
                enabled = !isSaving,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = ThemeOnPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...", fontWeight = FontWeight.Bold, color = ThemeOnPrimary)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, tint = ThemeOnPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Package", fontWeight = FontWeight.Bold, color = ThemeOnPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel", color = ThemeOnSurfaceVariant)
            }
        }
    )
}

@Composable
fun PackageCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = ThemePrimary,
                checkmarkColor = ThemeOnPrimary
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 14.sp, color = if (checked) ThemeOnBackground else ThemeOnSurfaceVariant)
    }
}

@Composable
fun OwnerPackageCard(
    pkg: StudioPackage,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ThemeOutline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pkg.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ThemeOnBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ThemePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${pkg.maxFunctions} ${if (pkg.maxFunctions > 1) "Days / Events" else "Day Coverage"}",
                                color = ThemePrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${pkg.deliveryTimelineDays} Days Delivery",
                            fontSize = 11.sp,
                            color = ThemeOnSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${pkg.price.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ThemePrimary
                    )
                    Text("Per Day / Package", fontSize = 10.sp, color = ThemeOnSurfaceVariant)
                }
            }

            if (pkg.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = pkg.description,
                    color = ThemeOnSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            if (pkg.deliverables.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeSurfaceVariant, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = ThemePrimary,
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Deliverables:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ThemeOnBackground)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(pkg.deliverables, fontSize = 12.sp, color = ThemeOnSurfaceVariant, lineHeight = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (pkg.includesPhotography) SmallFeatureChip("Photo", Icons.Default.CameraAlt)
                if (pkg.includesVideography) SmallFeatureChip("Video", Icons.Default.Videocam)
                if (pkg.includesCinematicFilm) SmallFeatureChip("Cinematic", Icons.Default.Movie)
                if (pkg.includesDrone) SmallFeatureChip("Drone", Icons.Default.Flight)
                if (pkg.includesAlbum) SmallFeatureChip("Album", Icons.Default.MenuBook)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = ThemeOutline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDuplicate,
                    colors = ButtonDefaults.textButtonColors(contentColor = ThemeOnSurfaceVariant)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicate", fontSize = 12.sp)
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary.copy(alpha = 0.15f), contentColor = ThemePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SmallFeatureChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(50),
        color = ThemeSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ThemeOutline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = ThemePrimary, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 10.sp, color = ThemeOnSurface, fontWeight = FontWeight.Medium)
        }
    }
}
