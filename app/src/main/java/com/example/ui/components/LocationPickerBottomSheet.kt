package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerBottomSheet(
    onDismiss: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onLocationSelected: (area: String, city: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val popularLocations = listOf(
        Pair("Civil Lines", "Prayagraj"),
        Pair("Katra", "Prayagraj"),
        Pair("Naini", "Prayagraj"),
        Pair("Kondhiyara", "Prayagraj"),
        Pair("Gomti Nagar", "Lucknow"),
        Pair("Assi Ghat", "Varanasi"),
        Pair("Connaught Place", "New Delhi")
    )

    val filteredLocations = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            popularLocations
        } else {
            popularLocations.filter {
                it.first.contains(searchQuery, ignoreCase = true) ||
                it.second.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E28)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Shoot Location",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                onClick = onUseCurrentLocation,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2245)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Use Current GPS Location", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Auto-detect village, town or city", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search village, area or city...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF38384E)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                if (searchQuery.isNotBlank() && filteredLocations.isEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationSelected(searchQuery.trim(), searchQuery.trim()) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Use \"$searchQuery\"", color = Color.White, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(filteredLocations) { loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationSelected(loc.first, loc.second) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF8B5CF6))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(loc.first, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(loc.second, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
