import re

with open("app/src/main/java/com/example/data/AuthManager.kt", "w") as f:
    f.write("""package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthManager {
    private var prefs: SharedPreferences? = null
    
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        }
    }

    suspend fun register(fullName: String, mobile: String, email: String, password: String, role: String = "CLIENT"): AuthResult {
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.UNKNOWN_ERROR
            
            val db = FirebaseFirestore.getInstance()
            val profile = hashMapOf(
                "uid" to user.uid,
                "fullName" to fullName,
                "mobileNumber" to mobile,
                "email" to email,
                "role" to role,
                "createdAt" to System.currentTimeMillis(),
                "city" to "Prayagraj", // Default
                "profilePhotoUrl" to ""
            )
            
            db.collection("users").document(user.uid).set(profile).await()
            
            prefs?.edit()?.putString("current_role", role)?.apply()
            
            AuthResult.SUCCESS
        } catch (e: Exception) {
            when {
                e.message?.contains("email address is already in use") == true -> AuthResult.ACCOUNT_ALREADY_EXISTS
                e.message?.contains("badly formatted") == true -> AuthResult.INVALID_EMAIL
                e.message?.contains("password") == true -> AuthResult.WEAK_PASSWORD
                else -> AuthResult.UNKNOWN_ERROR
            }
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.ACCOUNT_NOT_FOUND
            
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("users").document(user.uid).get().await()
            val role = doc.getString("role") ?: "CLIENT"
            
            prefs?.edit()?.putString("current_role", role)?.apply()
            
            AuthResult.SUCCESS
        } catch (e: Exception) {
            when {
                e.message?.contains("no user record") == true -> AuthResult.ACCOUNT_NOT_FOUND
                e.message?.contains("password is invalid") == true -> AuthResult.INCORRECT_PASSWORD
                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> AuthResult.INCORRECT_PASSWORD
                else -> AuthResult.ACCOUNT_NOT_FOUND // simplified for login
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
    
    suspend fun getUserProfile(): UserProfile {
        val user = FirebaseAuth.getInstance().currentUser ?: return UserProfile()
        return try {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("users").document(user.uid).get().await()
            UserProfile(
                name = doc.getString("fullName") ?: "User",
                email = doc.getString("email") ?: user.email ?: "",
                mobile = doc.getString("mobileNumber") ?: "",
                city = doc.getString("city") ?: "Prayagraj",
                role = doc.getString("role") ?: "CLIENT"
            )
        } catch(e: Exception) {
            UserProfile()
        }
    }
    
    // For synchronous access during navigation if needed
    fun getCurrentRole(): String {
        return prefs?.getString("current_role", "CLIENT") ?: "CLIENT"
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs?.getBoolean("completed_onboarding", false) ?: false
    }

    fun setOnboardingCompleted() {
        prefs?.edit()?.putBoolean("completed_onboarding", true)?.apply()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        try {
            val db = FirebaseFirestore.getInstance()
            val updates = hashMapOf<String, Any>(
                "fullName" to profile.name,
                "email" to profile.email,
                "mobileNumber" to profile.mobile,
                "city" to profile.city,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(user.uid).update(updates).await()
            prefs?.edit()?.putString("current_role", profile.role)?.apply()
        } catch(e: Exception) {
            // Ignore for now
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
    UNKNOWN_ERROR
}
""")
