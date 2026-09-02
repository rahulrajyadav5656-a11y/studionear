package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.models.Studio
import com.example.data.repositories.StudioRepository
import com.example.di.ServiceLocator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FavoritesManager {
    private const val TAG = "FavoritesManager"
    private var prefs: SharedPreferences? = null
    private var firestoreListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _favoriteStudioIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteStudioIds: StateFlow<Set<String>> = _favoriteStudioIds.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
        }
        setupAuthListener()
    }

    private fun setupAuthListener() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            firestoreListener?.remove()
            firestoreListener = null

            if (user != null) {
                // 1. Load cached favorites for this user immediately
                loadCachedFavorites(user.uid)
                // 2. Attach real-time snapshot listener on Firestore user document
                listenToFirestoreFavorites(user.uid)
            } else {
                _favoriteStudioIds.value = emptySet()
            }
        }
    }

    private fun loadCachedFavorites(uid: String) {
        val p = prefs ?: return
        val saved = p.getStringSet("favorites_$uid", emptySet()) ?: emptySet()
        _favoriteStudioIds.value = saved
    }

    private fun saveCachedFavorites(uid: String, ids: Set<String>) {
        prefs?.edit()?.putStringSet("favorites_$uid", ids)?.apply()
    }

    private fun listenToFirestoreFavorites(uid: String) {
        val db = FirebaseFirestore.getInstance()
        firestoreListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for favorites", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val rawFavorites = (snapshot.get("favoriteStudioIds") as? List<String>)
                        ?: (snapshot.get("favorites") as? List<String>)
                        ?: emptyList()
                    val set = rawFavorites.toSet()
                    _favoriteStudioIds.value = set
                    saveCachedFavorites(uid, set)
                }
            }
    }

    fun isFavorite(studioId: String): Boolean {
        return _favoriteStudioIds.value.contains(studioId)
    }

    fun isFavoriteFlow(studioId: String): Flow<Boolean> {
        return _favoriteStudioIds.map { it.contains(studioId) }
    }

    suspend fun toggleFavorite(studioId: String): Boolean {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        val currentlyFav = _favoriteStudioIds.value.contains(studioId)
        val newFav = !currentlyFav

        // 1. Optimistic Local State Update
        val updatedSet = if (newFav) {
            _favoriteStudioIds.value + studioId
        } else {
            _favoriteStudioIds.value - studioId
        }
        _favoriteStudioIds.value = updatedSet

        if (uid != null) {
            saveCachedFavorites(uid, updatedSet)

            // 2. Persist to Firestore User Document
            try {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(uid)
                
                val arrayAction = if (newFav) {
                    FieldValue.arrayUnion(studioId)
                } else {
                    FieldValue.arrayRemove(studioId)
                }

                val updateData = hashMapOf<String, Any>(
                    "favoriteStudioIds" to arrayAction,
                    "favorites" to arrayAction,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                userRef.set(updateData, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating favorites in Firestore", e)
                // Firestore offline mode will sync once connected
            }
        }

        return newFav
    }

    fun getFavoriteStudiosFlow(
        studioRepository: StudioRepository = ServiceLocator.studioRepository
    ): Flow<List<Studio>> {
        return combine(
            favoriteStudioIds,
            studioRepository.getStudios()
        ) { favIds, allStudios ->
            if (favIds.isEmpty()) {
                emptyList()
            } else {
                allStudios.filter { favIds.contains(it.id) }
            }
        }
    }
}
