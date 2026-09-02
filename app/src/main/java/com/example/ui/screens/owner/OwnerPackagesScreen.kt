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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.AuthManager
import com.example.data.MockDataManager
import com.example.data.models.Studio
import com.example.data.models.StudioPackage
import com.example.di.ServiceLocator
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPackagesScreen(onBack: () -> Unit) {
    val ownerId = AuthManager.getCurrentUser() ?: "default_owner"
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Get studio
    var studio by remember {
        mutableStateOf(
            MockDataManager.getStudioByOwnerId(ownerId) ?: Studio(
                id = "1",
                ownerId = ownerId,
                name = "Royal Shutter Studios"
            )
        )
    }

    var isOperatingOnPackage by remember { mutableStateOf(false) }
    var isPackagesLoading by remember { mutableStateOf(false) }

    // Sync studio and packages from Firestore on screen launch
    LaunchedEffect(ownerId) {
        isPackagesLoading = true
        try {
            val remoteStudio = ServiceLocator.studioRepository.getStudioByOwnerId(ownerId)
            if (remoteStudio != null) {
                studio = remoteStudio
                ServiceLocator.studioRepository.getPackagesForStudio(remoteStudio.id)
            }
        } catch (e: Exception) {
            // fallback
        } finally {
            isPackagesLoading = false
        }
    }

    val packagesFlow = remember(studio.id) {
        ServiceLocator.studioRepository.observePackagesForStudio(studio.id)
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
                        "Create photography packages to allow clients in Prayagraj to view pricing and book your services.",
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
                    // Summary Banner
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
                                    "Packages appear directly on your studio profile and booking requests.",
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
                                snackbarHostState.showSnackbar("Package '${pkg.name}' duplicated successfully!")
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
                                packageToDelete = null
                                snackbarHostState.showSnackbar("Package deleted successfully")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Deleted locally: ${e.message ?: ""}")
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
            studioId = studio.id,
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
                        showAddDialog = false
                        editingPackage = null
                        snackbarHostState.showSnackbar("Package saved successfully")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Saved locally: ${e.message ?: ""}")
                    } finally {
                        isOperatingOnPackage = false
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
            existingPkg?.deliverables ?: "300+ Edited Photos, 1 Full Length Traditional Video, 1 Cinematic Highlight Reel"
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
                // Package Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Package Title *") },
                    placeholder = { Text("e.g. Silver Wedding Package, Pre-Wedding Shoot") },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error)
                    },
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = ThemePrimary) },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Price and Functions row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { 
                            price = it
                            if (it.isNotBlank()) priceError = null
                        },
                        label = { Text("Price (₹) *") },
                        placeholder = { Text("45000") },
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

                // Delivery Timeline
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

                // Key Deliverables
                OutlinedTextField(
                    value = deliverables,
                    onValueChange = { deliverables = it },
                    label = { Text("Key Deliverables *") },
                    placeholder = { Text("e.g. 300 Edited Photos, 1 Traditional Video, 1 Cinematic Teaser, 1 Photo Album") },
                    leadingIcon = { Icon(Icons.Default.TaskAlt, contentDescription = null, tint = ThemePrimary) },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Package Description") },
                    placeholder = { Text("Detailed notes on rituals covered, team size, equipment, and shoot expectations...") },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Included In Package Checkboxes
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

                // Terms & Notes
                OutlinedTextField(
                    value = terms,
                    onValueChange = { terms = it },
                    label = { Text("Payment & Cancellation Terms") },
                    placeholder = { Text("e.g. 25% Advance booking deposit, balance on event day") },
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
                        priceError = "Enter a valid positive price"
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
            // Header: Title & Actions
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

                // Price Tag
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${pkg.price.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ThemePrimary
                    )
                    Text("Starting Price", fontSize = 10.sp, color = ThemeOnSurfaceVariant)
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

            // Deliverables Box
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

            // Included tags
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

            // Divider & Actions
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
