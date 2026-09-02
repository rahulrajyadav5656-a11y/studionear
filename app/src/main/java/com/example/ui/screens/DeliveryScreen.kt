package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Status", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Status: In Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ThemePrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Estimated Delivery: Dec 25, 2024", color = ThemeOnSurfaceVariant)
                        Text("Files will be retained for 90 days after delivery.", color = ThemeOnSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                val files = listOf("Photos (Raw)", "Photos (Edited)", "Cinematic Film", "Album Design")
                
                files.forEach { file ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(file, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        IconButton(onClick = { }, enabled = false) { // Disabled until ready
                            Icon(Icons.Default.Download, contentDescription = "Download")
                        }
                    }
                    Divider(color = ThemeOutline)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedButton(
                    onClick = { /* Navigate to Complaint Screen */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Report Delivery Delay", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
