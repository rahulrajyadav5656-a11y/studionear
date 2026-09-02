package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageScreen(
    packageId: String,
    onBack: () -> Unit,
    onBookPackage: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Package Details") },
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
                Text(
                    text = "Premium Cinematic",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeOnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₹85,000",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemePrimary
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Services Included", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val services = listOf(
                    "Photography",
                    "Videography",
                    "Candid Photography",
                    "Cinematic Film",
                    "Drone Coverage",
                    "Premium Album (40 pages)"
                )
                
                services.forEach { service ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ThemePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(service, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Package Rules", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Covers up to 3 functions.", fontSize = 14.sp)
                Text("• Delivery timeline: 45 days.", fontSize = 14.sp)
                Text("• Booking amount: 30%.", fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onBookPackage,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary)
                ) {
                    Text("Proceed to Book", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
