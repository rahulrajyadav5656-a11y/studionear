import re

with open("app/src/main/java/com/example/ui/screens/RegistrationScreen.kt", "r") as f:
    content = f.read()

old_error_1 = """                                locationError = "Registration failed: $result"
                                step = 1 // Go back so they can fix credentials"""

new_error_1 = """                                locationError = when (result) {
                                    com.example.data.AuthResult.ACCOUNT_ALREADY_EXISTS -> "Email already in use. Please use another."
                                    com.example.data.AuthResult.INVALID_EMAIL -> "Invalid email format."
                                    com.example.data.AuthResult.WEAK_PASSWORD -> "Password is too weak."
                                    else -> "Registration failed. Please try again."
                                }
                                step = 1 // Go back so they can fix credentials"""
content = content.replace(old_error_1, new_error_1)

old_error_2 = """                                    locationError = "Registration failed: $result"
                                    step = 1"""

new_error_2 = """                                    locationError = when (result) {
                                        com.example.data.AuthResult.ACCOUNT_ALREADY_EXISTS -> "Email already in use. Please use another."
                                        com.example.data.AuthResult.INVALID_EMAIL -> "Invalid email format."
                                        com.example.data.AuthResult.WEAK_PASSWORD -> "Password is too weak."
                                        else -> "Registration failed. Please try again."
                                    }
                                    step = 1"""
content = content.replace(old_error_2, new_error_2)

with open("app/src/main/java/com/example/ui/screens/RegistrationScreen.kt", "w") as f:
    f.write(content)
