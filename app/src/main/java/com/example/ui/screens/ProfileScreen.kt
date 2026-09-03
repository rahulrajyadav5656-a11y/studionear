
package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

private data class ProfileActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit = {},
    onNavigateToMenu: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var userProfile by remember { mutableStateOf(com.example.data.AuthManager.getUserProfile()) }

    LaunchedEffect(Unit) {
        userProfile = com.example.data.AuthManager.getUserProfile()
    }

    val primaryActions = listOf(
        ProfileActionItem(
            title = "My Bookings & Invoices",
            subtitle = "Track shoot dates and payment receipts",
            icon = Icons.Default.DateRange,
            action = { onNavigateToMenu("Payments") }
        ),
        ProfileActionItem(
            title = "Help & Support",
            subtitle = "Connect via WhatsApp or direct call",
            icon = Icons.Default.HeadsetMic,
            action = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919999999999?text=Hello%2C%20I%20need%20help%20with%20my%20studio%20booking"))
                context.startActivity(intent)
            }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        TopAppBar(
            title = { Text("Account", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // User Header Card
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeSurface, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToMenu("Edit Profile") }
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(ThemePrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ThemePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.name.ifEmpty { "Client User" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userProfile.city.ifEmpty { "Verified Account" },
                            fontSize = 13.sp,
                            color = ThemeOnSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = ThemeOnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Compact 2-Option Action List
            items(primaryActions.size) { index ->
                val item = primaryActions[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeSurface, RoundedCornerShape(14.dp))
                        .clickable { item.action() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(ThemePrimaryContainer.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = ThemePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.subtitle,
                            fontSize = 12.sp,
                            color = ThemeOnSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = ThemeOnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Clean Logout CTA
            item {
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = {
                        com.example.data.AuthManager.logout()
                        onLogoutClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                    )
                ) {
                    Text("Logout", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
