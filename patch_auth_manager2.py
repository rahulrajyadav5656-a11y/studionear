import re

with open("app/src/main/java/com/example/data/AuthManager.kt", "r") as f:
    content = f.read()

# Make getUserProfile synchronous by using cached preferences
old_get_user_profile = """    suspend fun getUserProfile(): UserProfile {
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
    }"""

new_get_user_profile = """    fun getUserProfile(): UserProfile {
        val emailOrMobile = getCurrentUser() ?: return UserProfile()
        val p = prefs ?: return UserProfile()
        return UserProfile(
            name = p.getString("profile_${emailOrMobile}_name", "User") ?: "User",
            email = p.getString("profile_${emailOrMobile}_email", "") ?: "",
            mobile = p.getString("profile_${emailOrMobile}_mobile", "") ?: "",
            city = p.getString("profile_${emailOrMobile}_city", "Prayagraj") ?: "Prayagraj",
            role = p.getString("role_$emailOrMobile", "CLIENT") ?: "CLIENT"
        )
    }
    
    suspend fun syncProfileFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        try {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("users").document(user.uid).get().await()
            prefs?.edit()?.apply {
                putString("profile_${user.uid}_name", doc.getString("fullName") ?: "User")
                putString("profile_${user.uid}_email", doc.getString("email") ?: user.email ?: "")
                putString("profile_${user.uid}_mobile", doc.getString("mobileNumber") ?: "")
                putString("profile_${user.uid}_city", doc.getString("city") ?: "Prayagraj")
                putString("role_${user.uid}", doc.getString("role") ?: "CLIENT")
                putString("current_role", doc.getString("role") ?: "CLIENT")
            }?.apply()
        } catch(e: Exception) {
            // Ignore
        }
    }"""
content = content.replace(old_get_user_profile, new_get_user_profile)

# Update saveUserProfile to be synchronous and then async sync to firestore
old_save_user_profile = """    suspend fun saveUserProfile(profile: UserProfile) {
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
    }"""

new_save_user_profile = """    fun saveUserProfile(profile: UserProfile) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val emailOrMobile = user.uid
        prefs?.edit()?.apply {
            putString("profile_${emailOrMobile}_name", profile.name)
            putString("profile_${emailOrMobile}_email", profile.email)
            putString("profile_${emailOrMobile}_mobile", profile.mobile)
            putString("profile_${emailOrMobile}_city", profile.city)
            putString("role_$emailOrMobile", profile.role)
        }?.apply()
        
        kotlinx.coroutines.GlobalScope.launch {
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
            } catch(e: Exception) {
                // Ignore
            }
        }
    }"""

content = content.replace(old_save_user_profile, new_save_user_profile)
content = content.replace("import kotlinx.coroutines.tasks.await", "import kotlinx.coroutines.tasks.await\nimport kotlinx.coroutines.launch")

# Update register to save profile locally
old_register_save = """            db.collection("users").document(user.uid).set(profile).await()
            
            prefs?.edit()?.putString("current_role", role)?.apply()"""

new_register_save = """            db.collection("users").document(user.uid).set(profile).await()
            
            prefs?.edit()?.apply {
                putString("current_role", role)
                putString("profile_${user.uid}_name", fullName)
                putString("profile_${user.uid}_email", email)
                putString("profile_${user.uid}_mobile", mobile)
                putString("profile_${user.uid}_city", "Prayagraj")
                putString("role_${user.uid}", role)
            }?.apply()"""
            
content = content.replace(old_register_save, new_register_save)

# Update login to save profile locally
old_login_save = """            val role = doc.getString("role") ?: "CLIENT"
            
            prefs?.edit()?.putString("current_role", role)?.apply()"""

new_login_save = """            val role = doc.getString("role") ?: "CLIENT"
            
            prefs?.edit()?.apply {
                putString("current_role", role)
                putString("profile_${user.uid}_name", doc.getString("fullName") ?: "User")
                putString("profile_${user.uid}_email", doc.getString("email") ?: user.email ?: "")
                putString("profile_${user.uid}_mobile", doc.getString("mobileNumber") ?: "")
                putString("profile_${user.uid}_city", doc.getString("city") ?: "Prayagraj")
                putString("role_${user.uid}", role)
            }?.apply()"""
content = content.replace(old_login_save, new_login_save)

with open("app/src/main/java/com/example/data/AuthManager.kt", "w") as f:
    f.write(content)
