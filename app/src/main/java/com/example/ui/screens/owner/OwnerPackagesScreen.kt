package com.example.ui.screens.owner

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

data class StudioPackageItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val deliverables: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPackagesScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val ownerId = AuthManager.getCurrentUser() ?: ""

    var studioDocId by remember { mutableStateOf("") }
    var packagesList by remember { mutableStateOf<List<StudioPackageItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var showAddDialog by remember { mutableStateOf(false) }
    var newPkgName by remember { mutableStateOf("") }
    var newPkgPrice by remember { mutableStateOf("") }
    var newPkgDeliverables by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(ownerId) {
        if (ownerId.isNotBlank()) {
            firestore.collection("studios").whereEqualTo("ownerId", ownerId).limit(1).get()
                .addOnSuccessListener { snap ->
                    val doc = snap.documents.firstOrNull()
                    val targetId = doc?.id ?: ownerId
                    studioDocId = targetId

                    firestore.collection("studios").document(targetId).collection("packages")
                        .addSnapshotListener { pSnap, _ ->
                            if (pSnap != null) {
                                val list = pSnap.documents.mapNotNull { d ->
                                    val name = d.getString("name") ?: return@mapNotNull null
                                    val price = d.getDouble("price") ?: d.getLong("price")?.toDouble() ?: 0.0
                                    val deliv = d.getString("deliverables") ?: d.getString("description") ?: ""
                                    StudioPackageItem(d.id, name, price, deliv)
                                }
                                packagesList = list
                            }
                            isLoading = false
                        }
                }
                .addOnFailureListener { isLoading = false }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Packages & Pricing", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13131A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Package")
            }
        },
        containerColor = Color(0xFF0F0F14)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF8B5CF6))
            }
        } else if (packagesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No packages added yet", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add First Package")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(packagesList, key = { it.id }) { pkg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                if (pkg.deliverables.isNotBlank()) {
                                    Text(pkg.deliverables, fontSize = 12.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 3.dp))
                                }
                                Text("₹${pkg.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF10B981), modifier = Modifier.padding(top = 4.dp))
                            }

                            IconButton(
                                onClick = {
                                    val targetId = if (studioDocId.isNotBlank()) studioDocId else ownerId
                                    firestore.collection("studios").document(targetId).collection("packages").document(pkg.id).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Package removed", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }

        // Add Package Dialog with auto-clean number parser
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { if (!isSaving) showAddDialog = false },
                title = { Text("Add New Package", color = Color.White, fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFF1E1E28),
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newPkgName,
                            onValueChange = { newPkgName = it },
                            label = { Text("Package Name") },
                            placeholder = { Text("e.g. Premium Wedding Package") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPkgPrice,
                            onValueChange = { newPkgPrice = it },
                            label = { Text("Price (₹)") },
                            placeholder = { Text("e.g. 99999") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPkgDeliverables,
                            onValueChange = { newPkgDeliverables = it },
                            label = { Text("Deliverables / Details") },
                            placeholder = { Text("List services, albums, videos included...") },
                            minLines = 4,
                            maxLines = 8,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Strip commas, spaces, currency symbols
                            val sanitizedPrice = newPkgPrice.replace(",", "").replace("₹", "").trim()
                            val priceVal = sanitizedPrice.toDoubleOrNull()

                            if (newPkgName.isBlank() || priceVal == null || priceVal <= 0.0) {
                                Toast.makeText(context, "Please enter valid package name and price", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            val targetId = if (studioDocId.isNotBlank()) studioDocId else ownerId
                            val pkgId = UUID.randomUUID().toString().take(8)
                            val data = hashMapOf(
                                "id" to pkgId,
                                "name" to newPkgName.trim(),
                                "price" to priceVal,
                                "deliverables" to newPkgDeliverables.trim(),
                                "createdAt" to System.currentTimeMillis()
                            )

                            firestore.collection("studios").document(targetId).collection("packages").document(pkgId).set(data)
                                .addOnSuccessListener {
                                    isSaving = false
                                    showAddDialog = false
                                    newPkgName = ""
                                    newPkgPrice = ""
                                    newPkgDeliverables = ""
                                    Toast.makeText(context, "Package Added Successfully!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Save Package")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }, enabled = !isSaving) {
                        Text("Cancel", color = Color(0xFF9CA3AF))
                    }
                }
            )
        }
    }
}
