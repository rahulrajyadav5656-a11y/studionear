package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegistrationSuccess: (role: String) -> Unit,
    onLoginClick: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("CLIENT") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onLoginClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Login"
                        )
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
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Join StudioNear",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeOnBackground,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Connect with top wedding photography studios",
                color = ThemeOnSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "I want to:",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = ThemeOnBackground,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = selectedRole == "CLIENT",
                    onClick = { selectedRole = "CLIENT" },
                    label = { Text("Book a Studio") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemePrimary,
                        selectedLabelColor = ThemeOnPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedRole == "STUDIO_OWNER",
                    onClick = { selectedRole = "STUDIO_OWNER" },
                    label = { Text("Register Studio") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemePrimary,
                        selectedLabelColor = ThemeOnPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { 
                    fullName = it
                    errorMessage = null
                },
                label = { Text("Full Name *") },
                placeholder = { Text("Enter your full name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { 
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        mobileNumber = it
                    }
                    errorMessage = null
                },
                label = { Text("Mobile Number *") },
                placeholder = { Text("10-digit mobile number") },
                prefix = { Text("+91 ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = null
                },
                label = { Text("Email Address *") },
                placeholder = { Text("Enter your email address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = null
                },
                label = { Text("Password *") },
                placeholder = { Text("At least 6 characters") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirm Password *") },
                placeholder = { Text("Re-enter your password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading
            )
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (errorMessage!!.contains("already exists", ignoreCase = true) || errorMessage!!.contains("already in use", ignoreCase = true)) {
                            ThemePrimary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = errorMessage!!,
                            color = if (errorMessage!!.contains("already exists", ignoreCase = true) || errorMessage!!.contains("already in use", ignoreCase = true)) {
                                ThemeOnBackground
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                            fontSize = 14.sp
                        )
                        if (errorMessage!!.contains("already exists", ignoreCase = true) || errorMessage!!.contains("already in use", ignoreCase = true) || errorMessage!!.contains("log in", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onLoginClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Go to Login Screen", color = ThemeOnPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
                    val cleanEmail = email.trim()
                    val cleanName = fullName.trim()
                    val cleanMobile = mobileNumber.trim()
                    
                    if (cleanName.isBlank()) {
                        errorMessage = "Please enter your full name"
                    } else if (cleanMobile.length != 10) {
                        errorMessage = "Please enter a valid 10-digit mobile number"
                    } else if (cleanEmail.isBlank() || !cleanEmail.matches(emailRegex)) {
                        errorMessage = "Please enter a valid email address"
                    } else if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                    } else if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                    } else {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val error = AuthManager.register(
                                fullName = cleanName,
                                mobile = cleanMobile,
                                email = cleanEmail,
                                password = password,
                                role = selectedRole
                            )
                            isLoading = false
                            if (error == null) {
                                errorMessage = null
                                onRegistrationSuccess(selectedRole)
                            } else {
                                errorMessage = error
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ThemeOnPrimary)
                } else {
                    Text(
                        text = if (selectedRole == "STUDIO_OWNER") "Register Studio" else "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text("Already have an account? ", color = ThemeOnSurfaceVariant)
                TextButton(onClick = onLoginClick) {
                    Text("Log In", color = ThemePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
