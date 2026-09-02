import re

with open("app/src/main/java/com/example/ui/screens/RegistrationScreen.kt", "r") as f:
    content = f.read()

# Make email mandatory in UI
content = content.replace('label = { Text("Email Address (Optional)") }', 'label = { Text("Email Address *") }')

# In Step 1, remove the accountExists check
old_step1_check = """                            if (fullName.isBlank()) {
                                step1Error = "Please enter your full name"
                            } else if (mobileNumber.length != 10 || !mobileNumber.all { it.isDigit() }) {
                                step1Error = "Please enter a valid 10-digit mobile number"
                            } else if (email.isNotBlank() && !email.matches(emailRegex)) {
                                step1Error = "Please enter a valid email address"
                            } else if (password.length < 6) {
                                step1Error = "Password must be at least 6 characters"
                            } else if (password != confirmPassword) {
                                step1Error = "Passwords do not match"
                            } else {
                                if (com.example.data.AuthManager.accountExists(mobileNumber) || (email.isNotBlank() && com.example.data.AuthManager.accountExists(email))) {
                                    step1Error = "Account already exists. Please log in."
                                } else {
                                    step1Error = null
                                    step = 2
                                }
                            }"""

new_step1_check = """                            if (fullName.isBlank()) {
                                step1Error = "Please enter your full name"
                            } else if (mobileNumber.length != 10 || !mobileNumber.all { it.isDigit() }) {
                                step1Error = "Please enter a valid 10-digit mobile number"
                            } else if (email.isBlank() || !email.matches(emailRegex)) {
                                step1Error = "Please enter a valid email address"
                            } else if (password.length < 6) {
                                step1Error = "Password must be at least 6 characters"
                            } else if (password != confirmPassword) {
                                step1Error = "Passwords do not match"
                            } else {
                                step1Error = null
                                step = 2
                            }"""

content = content.replace(old_step1_check, new_step1_check)


# In Step 3, patch the registration calls
old_register_1 = """                        scope.launch {
                            kotlinx.coroutines.delay(1000)
                            com.example.data.AuthManager.register(mobileNumber, email, password, selectedRole)
                            step = 4
                        }"""

new_register_1 = """                        scope.launch {
                            val result = com.example.data.AuthManager.register(fullName, mobileNumber, email, password, selectedRole)
                            if (result == com.example.data.AuthResult.SUCCESS) {
                                step = 4
                            } else {
                                locationError = "Registration failed: $result"
                                step = 1 // Go back so they can fix credentials
                            }
                        }"""

content = content.replace(old_register_1, new_register_1)

old_register_2 = """                        onClick = {
                            com.example.data.AuthManager.register(mobileNumber, email, password, selectedRole)
                            step = 4
                        },"""

new_register_2 = """                        onClick = {
                            isLoading = true
                            scope.launch {
                                val result = com.example.data.AuthManager.register(fullName, mobileNumber, email, password, selectedRole)
                                isLoading = false
                                if (result == com.example.data.AuthResult.SUCCESS) {
                                    step = 4
                                } else {
                                    locationError = "Registration failed: $result"
                                    step = 1
                                }
                            }
                        },"""

content = content.replace(old_register_2, new_register_2)

with open("app/src/main/java/com/example/ui/screens/RegistrationScreen.kt", "w") as f:
    f.write(content)
