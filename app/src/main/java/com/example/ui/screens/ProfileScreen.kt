package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit = {},
    onNavigateToMenu: (String) -> Unit = {}
) {
    var userProfile by remember { mutableStateOf(com.example.data.AuthManager.getUserProfile()) }
    
    // Refresh profile on composition
    LaunchedEffect(Unit) {
        userProfile = com.example.data.AuthManager.getUserProfile()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        TopAppBar(
            title = { Text("Profile", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .clickable { onNavigateToMenu("Edit Profile") }
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(ThemePrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ThemePrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(userProfile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(userProfile.city, fontSize = 14.sp, color = ThemeOnSurfaceVariant)
                    }
                }
            }
            
            val menuItems = listOf(
                "My Bookings", "Favorites", "Payments", "Agreements", 
                "Deliveries", "Complaints", "Reviews", "Notification Settings"
            )
            
            items(menuItems.size) { index ->
                val item = menuItems[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeSurface, RoundedCornerShape(12.dp))
                        .clickable { onNavigateToMenu(item) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ThemeOnSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        com.example.data.AuthManager.logout()
                        onLogoutClick()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
