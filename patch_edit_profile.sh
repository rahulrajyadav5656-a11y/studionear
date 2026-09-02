cat << 'INNEREOF' > app/src/main/java/com/example/ui/screens/EditProfileScreen.kt
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("CLIENT") }

    LaunchedEffect(Unit) {
        val profile = AuthManager.getUserProfile()
        name = profile.name
        email = profile.email
        mobile = profile.mobile
        city = profile.city
        role = profile.role
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Role Preference", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Role Toggle (Client vs Photographer/Studio Owner)
            val roles = listOf("Client", "Photographer")
            var selectedRoleIndex by remember(role) { 
                mutableStateOf(if (role == "STUDIO_OWNER") 1 else 0) 
            }

            TabRow(
                selectedTabIndex = selectedRoleIndex,
                containerColor = ThemeSurface,
                contentColor = ThemePrimary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedRoleIndex]),
                        color = ThemePrimary
                    )
                },
                modifier = Modifier.fillMaxWidth().background(ThemeSurface, RoundedCornerShape(12.dp))
            ) {
                roles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedRoleIndex == index,
                        onClick = { 
                            selectedRoleIndex = index
                            role = if (index == 1) "STUDIO_OWNER" else "CLIENT"
                        },
                        text = { 
                            Text(
                                title, 
                                fontWeight = if (selectedRoleIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedRoleIndex == index) ThemePrimary else ThemeOnSurfaceVariant
                            ) 
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    AuthManager.saveUserProfile(UserProfile(name, email, mobile, city, role))
                    // If the role changed significantly, the user might need to restart the app to see the right UI flow.
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            if (role != AuthManager.getCurrentRole()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Note: Changing your role will take effect after you save and restart the app.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}
INNEREOF
