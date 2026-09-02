cat << 'INNEREOF' > app/src/main/java/com/example/ui/screens/owner/OwnerSettingsScreen.kt
package com.example.ui.screens.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerSettingsScreen(onBack: () -> Unit, onLogoutClick: () -> Unit, onNavigate: (String) -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
                .verticalScroll(rememberScrollState())
        ) {
            SettingsItem(title = "Account Details & Role", onClick = { onNavigate("owner_edit_profile") })
            SettingsItem(title = "Notification Preferences", onClick = { })
            SettingsItem(title = "Privacy & Security", onClick = { })
            SettingsItem(title = "Help & Support", onClick = { })
            SettingsItem(title = "Terms and Conditions", onClick = { })
            SettingsItem(title = "Privacy Policy", onClick = { })
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ThemeSurface)
            ) {
                Text("Logout", color = ThemePrimary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var showDeleteDialog by remember { mutableStateOf(false) }
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("Delete Account", color = MaterialTheme.colorScheme.error)
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Account?") },
                    text = { Text("This will permanently remove your studio, packages, portfolio, and all records. Active bookings will be cancelled. This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = { 
                                 // In demo, just logout for safety
                                onLogoutClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete Permanently")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ThemeOnSurfaceVariant)
    }
    HorizontalDivider(color = ThemeSurface)
}
INNEREOF
