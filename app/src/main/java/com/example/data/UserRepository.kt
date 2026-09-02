package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.User
import com.example.data.models.UserRole
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object UserRepository {
    private var prefs: SharedPreferences? = null
    private val gson = Gson()
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("users_data", Context.MODE_PRIVATE)
            loadData()
        }
    }

    private fun loadData() {
        val p = prefs ?: return
        val usersJson = p.getString("users", null)
        if (usersJson != null) {
            val type = object : TypeToken<List<User>>() {}.type
            _users.value = gson.fromJson(usersJson, type)
        } else {
            // Mock default users
            _users.value = listOf(
                User(id = "user_123", fullName = "Rahul Yadav", phoneNumber = "9876543210", email = "rahul@example.com", role = UserRole.CLIENT),
                User(id = "client_2", fullName = "Priya Singh", phoneNumber = "9876543211", email = "priya@example.com", role = UserRole.CLIENT)
            )
            saveData()
        }
    }

    private fun saveData() {
        prefs?.edit()?.putString("users", gson.toJson(_users.value))?.apply()
    }

    fun getUserById(id: String): User? {
        return _users.value.find { it.id == id }
    }

    fun getUserByPhoneOrEmail(identifier: String): User? {
        return _users.value.find { it.phoneNumber == identifier || it.email == identifier }
    }

    fun saveUser(user: User) {
        val current = _users.value.toMutableList()
        val index = current.indexOfFirst { it.id == user.id }
        if (index >= 0) {
            current[index] = user
        } else {
            current.add(user)
        }
        _users.value = current
        saveData()
    }
}
