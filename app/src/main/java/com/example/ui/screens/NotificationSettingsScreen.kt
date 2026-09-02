package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val settings = listOf(
        "booking" to "Booking updates",
        "payment" to "Payment updates",
        "delivery" to "Delivery updates",
        "message" to "Messages",
        "review" to "Reviews",
        "offer" to "Offers",
        "complaint" to "Complaint/dispute updates"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            settings.forEach { (key, label) ->
                var isChecked by remember { mutableStateOf(AuthManager.getNotificationSetting(key)) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isChecked,
                        onCheckedChange = {
                            isChecked = it
                            AuthManager.saveNotificationSetting(key, it)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = ThemePrimary)
                    )
                }
                HorizontalDivider(color = ThemeOutline.copy(alpha = 0.5f))
            }
        }
    }
}
