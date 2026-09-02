import re

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    content = f.read()

# Add rememberCoroutineScope import if not present
if "import kotlinx.coroutines.launch" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport kotlinx.coroutines.launch")

# Add isLoading state
if "var isLoading by remember { mutableStateOf(false) }" not in content:
    content = content.replace("var errorMessage by remember { mutableStateOf<String?>(null) }", "var errorMessage by remember { mutableStateOf<String?>(null) }\n    var isLoading by remember { mutableStateOf(false) }\n    val scope = rememberCoroutineScope()")

# Replace onClick
old_onclick = """                onClick = {
                    if (credential.isNotBlank() && password.isNotBlank()) {
                        val result = com.example.data.AuthManager.login(credential, password)
                        when (result) {
                            com.example.data.AuthResult.SUCCESS -> {
                                errorMessage = null
                                onLoginSuccess()
                            }
                            com.example.data.AuthResult.ACCOUNT_NOT_FOUND -> {
                                errorMessage = "Account not found. Please create an account first."
                            }
                            com.example.data.AuthResult.INCORRECT_PASSWORD -> {
                                errorMessage = "Incorrect password. Please try again."
                            }
                            else -> {
                                // Should not happen in login
                            }
                        }
                    } else {
                        errorMessage = "Please enter valid credentials"
                    }
                },"""

new_onclick = """                onClick = {
                    if (credential.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = com.example.data.AuthManager.login(credential, password)
                            isLoading = false
                            when (result) {
                                com.example.data.AuthResult.SUCCESS -> {
                                    errorMessage = null
                                    onLoginSuccess()
                                }
                                com.example.data.AuthResult.ACCOUNT_NOT_FOUND -> {
                                    errorMessage = "Account not found. Please create an account first."
                                }
                                com.example.data.AuthResult.INCORRECT_PASSWORD -> {
                                    errorMessage = "Incorrect password. Please try again."
                                }
                                else -> {
                                    errorMessage = "Login failed. Please try again."
                                }
                            }
                        }
                    } else {
                        errorMessage = "Please enter valid credentials (Email required)"
                    }
                },"""

content = content.replace(old_onclick, new_onclick)

# Disable button when loading
if "enabled = !isLoading" not in content:
    content = content.replace("colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary)\n            ) {", "colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),\n                enabled = !isLoading\n            ) {")

# Show loading indicator in button
old_button_content = """Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)"""
new_button_content = """if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ThemeOnPrimary)
                } else {
                    Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }"""
content = content.replace(old_button_content, new_button_content)

# Update text label to Email
content = content.replace("label = { Text(\"Mobile Number or Email\") }", "label = { Text(\"Email Address\") }")
content = content.replace("placeholder = { Text(\"Enter your mobile number or email\") }", "placeholder = { Text(\"Enter your email address\") }")

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(content)
