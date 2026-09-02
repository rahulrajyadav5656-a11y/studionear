package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuthManager {
    private var prefs: SharedPreferences? = null
    
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        }
    }

    suspend fun register(fullName: String, mobile: String, email: String, password: String, role: String = "CLIENT"): String? {
        return try {
            val auth = FirebaseAuth.getInstance()
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()
            val cleanName = fullName.trim()
            val cleanMobile = mobile.trim()
            
            val result = auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword).await()
            val user = result.user ?: return "Registration failed. Please try again."
            val uid = user.uid
            
            prefs?.edit()?.apply {
                putString("current_role", role)
                putString("profile_${uid}_name", cleanName)
                putString("profile_${uid}_email", cleanEmail)
                putString("profile_${uid}_mobile", cleanMobile)
                putString("profile_${uid}_city", "Prayagraj")
                putString("role_$uid", role)
            }?.apply()
            
            try {
                val db = FirebaseFirestore.getInstance()
                val profile = hashMapOf(
                    "uid" to uid,
                    "email" to cleanEmail,
                    "name" to cleanName,
                    "fullName" to cleanName,
                    "mobileNumber" to cleanMobile,
                    "role" to role,
                    "city" to "Prayagraj",
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "profilePhotoUrl" to ""
                )
                db.collection("users").document(uid).set(profile, com.google.firebase.firestore.SetOptions.merge()).await()

                if (role == "STUDIO_OWNER") {
                    val initialStudio = hashMapOf(
                        "studioId" to uid,
                        "id" to uid,
                        "ownerId" to uid,
                        "name" to cleanName,
                        "studioName" to cleanName,
                        "contactPerson" to cleanName,
                        "phone" to cleanMobile,
                        "email" to cleanEmail,
                        "city" to "Prayagraj",
                        "area" to "Civil Lines",
                        "rating" to 0.0,
                        "reviewCount" to 0,
                        "startingPrice" to 0.0,
                        "startingPackagePrice" to 0.0,
                        "verified" to false,
                        "isVerified" to false,
                        "verifiedStatus" to "PENDING",
                        "services" to emptyList<String>(),
                        "servicesOffered" to emptyList<String>(),
                        "coverImageUrl" to "",
                        "bio" to "",
                        "description" to "",
                        "location" to hashMapOf(
                            "latitude" to 25.4526,
                            "longitude" to 81.8349,
                            "address" to "Civil Lines, Prayagraj",
                            "city" to "Prayagraj",
                            "area" to "Civil Lines"
                        ),
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    db.collection("studios").document(uid).set(initialStudio, com.google.firebase.firestore.SetOptions.merge()).await()
                }
            } catch (e: Exception) {
                // Background fallback if firestore offline or local cache
            }
            
            null
        } catch (e: FirebaseAuthUserCollisionException) {
            "An account with this email already exists. Please log in instead."
        } catch (e: FirebaseAuthWeakPasswordException) {
            "Password is too weak. Please use at least 6 characters."
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            "The email address is invalid."
        } catch (e: FirebaseNetworkException) {
            "Network error. Please check your internet connection."
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            when {
                msg.contains("already in use") || msg.contains("email_exists") -> "An account with this email already exists. Please log in instead."
                msg.contains("password should be at least") || msg.contains("weak password") -> "Password is too weak. Please use at least 6 characters."
                msg.contains("badly formatted") || msg.contains("invalid email") -> "The email address is invalid."
                msg.contains("network error") || msg.contains("unreachable") -> "Network error. Please check your internet connection."
                else -> e.localizedMessage ?: "Registration failed. Please try again."
            }
        }
    }

    suspend fun login(email: String, password: String): String? {
        return try {
            val auth = FirebaseAuth.getInstance()
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()
            
            val result = auth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
            val user = result.user ?: return "Account not found with this email."
            val uid = user.uid
            
            var role = "CLIENT"
            var name = user.displayName ?: "User"
            var userEmail = user.email ?: cleanEmail
            var userMobile = ""
            var userCity = "Prayagraj"
            
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val fetchedRole = doc.getString("role")?.trim()
                    if (!fetchedRole.isNullOrBlank()) {
                        role = fetchedRole.uppercase()
                    }
                    name = doc.getString("name") ?: doc.getString("fullName") ?: name
                    userEmail = doc.getString("email") ?: userEmail
                    userMobile = doc.getString("mobileNumber") ?: userMobile
                    userCity = doc.getString("city") ?: userCity
                }
                
                // Extra check: If role is still CLIENT, check if a studio document exists for this UID
                if (!role.equals("STUDIO_OWNER", ignoreCase = true)) {
                    val studioDoc = db.collection("studios").document(uid).get().await()
                    if (studioDoc.exists()) {
                        role = "STUDIO_OWNER"
                    }
                }

                if (!doc.exists()) {
                    // Fallback for legacy user if document doesn't exist
                    val localRole = prefs?.getString("role_$uid", null) ?: prefs?.getString("current_role", null)
                    if (localRole != null) {
                        role = localRole.trim().uppercase()
                    } else if (cleanEmail.contains("studio", ignoreCase = true) || cleanEmail.contains("owner", ignoreCase = true)) {
                        role = "STUDIO_OWNER"
                    }
                    // Write initial user doc for future logins
                    val initialUserDoc = hashMapOf<String, Any>(
                        "uid" to uid,
                        "email" to cleanEmail,
                        "name" to name,
                        "fullName" to name,
                        "role" to role,
                        "city" to userCity,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(uid).set(initialUserDoc, com.google.firebase.firestore.SetOptions.merge()).await()
                }
            } catch (e: Exception) {
                // Fallback to local profile
                val localRole = prefs?.getString("role_$uid", null) ?: prefs?.getString("current_role", "CLIENT") ?: "CLIENT"
                role = localRole.trim().uppercase()
            }
            
            prefs?.edit()?.apply {
                putString("current_role", role)
                putString("profile_${uid}_name", name)
                putString("profile_${uid}_email", userEmail)
                putString("profile_${uid}_mobile", userMobile)
                putString("profile_${uid}_city", userCity)
                putString("role_$uid", role)
            }?.apply()
            
            null
        } catch (e: FirebaseAuthInvalidUserException) {
            "Account not found with this email."
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            "Incorrect password. Please try again."
        } catch (e: FirebaseNetworkException) {
            "Network error. Please check your internet connection."
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            when {
                msg.contains("no user record") || msg.contains("user-not-found") -> "Account not found with this email."
                msg.contains("password is invalid") || msg.contains("invalid_login_credentials") || msg.contains("wrong-password") -> "Incorrect password. Please try again."
                msg.contains("network error") || msg.contains("unreachable") -> "Network error. Please check your internet connection."
                msg.contains("badly formatted") -> "Please enter a valid email address."
                else -> e.localizedMessage ?: "Login failed. Please try again."
            }
        }
    }

    suspend fun sendPasswordReset(email: String): String? {
        return try {
            val auth = FirebaseAuth.getInstance()
            val cleanEmail = email.trim()
            if (cleanEmail.isBlank()) return "Please enter your email address."
            auth.sendPasswordResetEmail(cleanEmail).await()
            null
        } catch (e: FirebaseAuthInvalidUserException) {
            "No account found with this email address."
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            "Please enter a valid email address."
        } catch (e: FirebaseNetworkException) {
            "Network error. Please check your internet connection."
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            when {
                msg.contains("user-not-found") || msg.contains("no user record") -> "No account found with this email address."
                msg.contains("badly formatted") || msg.contains("invalid email") -> "Please enter a valid email address."
                msg.contains("network error") || msg.contains("unreachable") -> "Network error. Please check your internet connection."
                else -> e.localizedMessage ?: "Failed to send reset email. Please try again."
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        prefs?.edit()?.remove("current_role")?.apply()
    }

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun getCurrentUser(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
    
    fun getUserProfile(): UserProfile {
        val uid = getCurrentUser() ?: return UserProfile()
        val p = prefs ?: return UserProfile()
        return UserProfile(
            name = p.getString("profile_${uid}_name", "User") ?: "User",
            email = p.getString("profile_${uid}_email", "") ?: "",
            mobile = p.getString("profile_${uid}_mobile", "") ?: "",
            city = p.getString("profile_${uid}_city", "Prayagraj") ?: "Prayagraj",
            role = p.getString("role_$uid", p.getString("current_role", "CLIENT") ?: "CLIENT") ?: "CLIENT"
        )
    }
    
    suspend fun syncProfileFromFirestore(): String {
        val user = FirebaseAuth.getInstance().currentUser ?: return "CLIENT"
        var role = prefs?.getString("role_${user.uid}", prefs?.getString("current_role", "CLIENT") ?: "CLIENT") ?: "CLIENT"
        try {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("users").document(user.uid).get().await()
            if (doc.exists()) {
                val fetchedRole = doc.getString("role")
                if (!fetchedRole.isNullOrBlank()) {
                    role = fetchedRole.uppercase()
                }
                val name = doc.getString("name") ?: doc.getString("fullName") ?: "User"
                val email = doc.getString("email") ?: user.email ?: ""
                val mobile = doc.getString("mobileNumber") ?: ""
                val city = doc.getString("city") ?: "Prayagraj"
                prefs?.edit()?.apply {
                    putString("profile_${user.uid}_name", name)
                    putString("profile_${user.uid}_email", email)
                    putString("profile_${user.uid}_mobile", mobile)
                    putString("profile_${user.uid}_city", city)
                    putString("role_${user.uid}", role)
                    putString("current_role", role)
                }?.apply()
            }
        } catch(e: Exception) {
            // Ignore offline fallback
        }
        return role
    }
    
    fun getCurrentRole(): String {
        return (prefs?.getString("current_role", "CLIENT") ?: "CLIENT").trim().uppercase()
    }

    fun setRole(newRole: String) {
        val cleanRole = newRole.trim().uppercase()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        prefs?.edit()?.apply {
            putString("current_role", cleanRole)
            if (uid != null) {
                putString("role_$uid", cleanRole)
            }
        }?.apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs?.getBoolean("completed_onboarding", false) ?: false
    }

    fun setOnboardingCompleted() {
        prefs?.edit()?.putBoolean("completed_onboarding", true)?.apply()
    }

    fun saveUserProfile(profile: UserProfile) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        prefs?.edit()?.apply {
            putString("profile_${uid}_name", profile.name)
            putString("profile_${uid}_email", profile.email)
            putString("profile_${uid}_mobile", profile.mobile)
            putString("profile_${uid}_city", profile.city)
            putString("role_$uid", profile.role)
        }?.apply()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val updates = hashMapOf<String, Any>(
                    "name" to profile.name,
                    "fullName" to profile.name,
                    "email" to profile.email,
                    "mobileNumber" to profile.mobile,
                    "city" to profile.city,
                    "role" to profile.role,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("users").document(user.uid).set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
            } catch(e: Exception) {
                // Ignore
            }
        }
    }

    fun getNotificationSetting(key: String, defaultValue: Boolean = true): Boolean {
        val uid = getCurrentUser() ?: return defaultValue
        return prefs?.getBoolean("notif_${uid}_$key", defaultValue) ?: defaultValue
    }

    fun saveNotificationSetting(key: String, value: Boolean) {
        val uid = getCurrentUser() ?: return
        prefs?.edit()?.putBoolean("notif_${uid}_$key", value)?.apply()
    }
}

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val city: String = "",
    val role: String = "CLIENT"
)

enum class AuthResult {
    SUCCESS,
    ACCOUNT_NOT_FOUND,
    INCORRECT_PASSWORD,
    ACCOUNT_ALREADY_EXISTS,
    INVALID_EMAIL,
    WEAK_PASSWORD,
    NETWORK_ERROR,
    UNKNOWN_ERROR
}
