package com.example.data.repositories

import android.util.Log
import com.example.data.models.Booking
import com.example.data.models.Location
import com.example.data.models.Studio
import com.example.data.models.StudioPackage
import com.example.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {
    
    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            trySend(user)
                        } else {
                            // User exists in Auth but not in Firestore yet
                            val newUser = User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email,
                                fullName = firebaseUser.displayName ?: ""
                            )
                            trySend(newUser)
                        }
                    }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun saveUser(user: User) {
        val uid = auth.currentUser?.uid ?: return
        val userToSave = user.copy(id = uid)
        firestore.collection("users").document(uid).set(userToSave).await()
    }

    override suspend fun updateLocation(lat: Double, lng: Double, address: String) {
        val uid = auth.currentUser?.uid ?: return
        val location = Location(lat, lng, address)
        firestore.collection("users").document(uid).update("savedLocation", location).await()
    }

    override suspend fun toggleFavoriteStudio(studioId: String, isFavorite: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(uid)
        try {
            val arrayAction = if (isFavorite) {
                com.google.firebase.firestore.FieldValue.arrayUnion(studioId)
            } else {
                com.google.firebase.firestore.FieldValue.arrayRemove(studioId)
            }
            userRef.set(
                mapOf(
                    "favoriteStudioIds" to arrayAction,
                    "favorites" to arrayAction,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            // Fallback transaction
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val user = snapshot.toObject(User::class.java) ?: return@runTransaction
                val currentFavorites = user.favoriteStudioIds.toMutableList()
                if (isFavorite && !currentFavorites.contains(studioId)) {
                    currentFavorites.add(studioId)
                } else if (!isFavorite) {
                    currentFavorites.remove(studioId)
                }
                transaction.update(userRef, "favoriteStudioIds", currentFavorites)
            }.await()
        }
    }
}

class FirebaseStudioRepository(
    private val firestore: FirebaseFirestore
) : StudioRepository {

    private fun studioDocumentToModel(doc: com.google.firebase.firestore.DocumentSnapshot): Studio? {
        if (!doc.exists()) return null
        val data = doc.data ?: return null

        val id = doc.id
        val ownerId = (data["ownerId"] as? String) ?: ((data["studioId"] as? String) ?: id)
        val name = (data["studioName"] as? String) ?: ((data["name"] as? String) ?: "")
        val contactPerson = (data["contactPerson"] as? String) ?: ""
        val phone = (data["phone"] as? String) ?: ""
        val email = (data["email"] as? String) ?: ""
        val area = (data["area"] as? String) ?: ""
        val city = (data["city"] as? String) ?: "Prayagraj"
        val bio = (data["bio"] as? String) ?: ((data["description"] as? String) ?: "")
        val verified = (data["verified"] as? Boolean) ?: ((data["isVerified"] as? Boolean) ?: false)
        val verifiedStatus = (data["verifiedStatus"] as? String) ?: if (verified) "VERIFIED" else "PENDING"
        val isSponsored = (data["isSponsored"] as? Boolean) ?: false
        val rating = (data["rating"] as? Number)?.toFloat() ?: 0.0f
        val reviewCount = (data["reviewCount"] as? Number)?.toInt() ?: 0
        val startingPrice = (data["startingPrice"] as? Number)?.toDouble()
            ?: ((data["startingPackagePrice"] as? Number)?.toDouble() ?: 0.0)
        val workingHours = (data["workingHours"] as? String) ?: "10:00 AM - 08:00 PM"
        val experienceYears = (data["experienceYears"] as? Number)?.toInt() ?: 0
        val deliveryTimeDays = (data["deliveryTimeDays"] as? Number)?.toInt() ?: 30
        val completedBookings = (data["completedBookings"] as? Number)?.toInt() ?: 0
        val deliveryScore = (data["deliveryPerformanceScore"] as? Number)?.toFloat() ?: 0.0f
        val responseScore = (data["responseRateScore"] as? Number)?.toFloat() ?: 0.0f
        val subscriptionExpiresAt = when (val exp = data["subscriptionExpiresAt"]) {
            is com.google.firebase.Timestamp -> exp.toDate().time
            is java.util.Date -> exp.time
            is Number -> exp.toLong()
            else -> null
        }
        val sponsoredExpiresAt = when (val exp = data["sponsoredExpiresAt"]) {
            is com.google.firebase.Timestamp -> exp.toDate().time
            is java.util.Date -> exp.time
            is Number -> exp.toLong()
            else -> null
        }

        @Suppress("UNCHECKED_CAST")
        val servicesList = (data["services"] as? List<String>)
            ?: ((data["servicesOffered"] as? List<String>) ?: emptyList())
        @Suppress("UNCHECKED_CAST")
        val portfolioList = (data["portfolioUrls"] as? List<String>) ?: emptyList()
        val coverImageUrl = (data["coverImageUrl"] as? String) ?: ""
        val address = (data["address"] as? String) ?: ""

        val locationMap = data["location"] as? Map<*, *>
        val location = if (locationMap != null) {
            Location(
                latitude = (locationMap["latitude"] as? Number)?.toDouble() ?: 25.4526,
                longitude = (locationMap["longitude"] as? Number)?.toDouble() ?: 81.8349,
                address = (locationMap["address"] as? String) ?: address,
                city = (locationMap["city"] as? String) ?: city,
                area = (locationMap["area"] as? String) ?: area
            )
        } else {
            Location(address = address, city = city, area = area)
        }

        return Studio(
            id = id,
            ownerId = ownerId,
            name = name,
            studioName = name,
            contactPerson = contactPerson,
            description = bio,
            bio = bio,
            verified = verified || verifiedStatus.equals("VERIFIED", ignoreCase = true),
            isVerified = verified || verifiedStatus.equals("VERIFIED", ignoreCase = true),
            verifiedStatus = verifiedStatus,
            isSponsored = isSponsored,
            rating = rating,
            reviewCount = reviewCount,
            startingPrice = startingPrice,
            startingPackagePrice = startingPrice,
            location = location,
            city = city,
            area = area,
            coverImageUrl = coverImageUrl,
            portfolioUrls = portfolioList,
            services = servicesList,
            servicesOffered = servicesList,
            phone = phone,
            email = email,
            experienceYears = experienceYears,
            completedBookings = completedBookings,
            deliveryPerformanceScore = deliveryScore,
            responseRateScore = responseScore,
            workingHours = workingHours,
            deliveryTimeDays = deliveryTimeDays,
            subscriptionExpiresAt = subscriptionExpiresAt,
            sponsoredExpiresAt = sponsoredExpiresAt
        )
    }

    private fun packageDocumentToModel(doc: com.google.firebase.firestore.DocumentSnapshot, defaultStudioId: String): StudioPackage? {
        if (!doc.exists()) return null
        val data = doc.data ?: return null
        val id = (data["packageId"] as? String) ?: ((data["id"] as? String) ?: doc.id)
        val studioId = (data["studioId"] as? String) ?: defaultStudioId
        val title = (data["title"] as? String) ?: ((data["name"] as? String) ?: "Photography Package")
        val price = (data["price"] as? Number)?.toDouble() ?: 0.0
        val duration = (data["duration"] as? String) ?: ""
        val description = (data["description"] as? String) ?: ""
        val deliverables = (data["deliverables"] as? String) ?: ""
        val includesPhotography = (data["includesPhotography"] as? Boolean) ?: false
        val includesVideography = (data["includesVideography"] as? Boolean) ?: false
        val includesCandid = (data["includesCandidPhotography"] as? Boolean) ?: false
        val includesCinematic = (data["includesCinematicFilm"] as? Boolean) ?: false
        val includesDrone = (data["includesDrone"] as? Boolean) ?: false
        val includesAlbum = (data["includesAlbum"] as? Boolean) ?: false
        val maxFunctions = (data["maxFunctions"] as? Number)?.toInt() ?: 1
        val deliveryDays = (data["deliveryTimelineDays"] as? Number)?.toInt() ?: 30
        val terms = (data["terms"] as? String) ?: ""
        val createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

        return StudioPackage(
            id = id,
            packageId = id,
            studioId = studioId,
            name = title,
            title = title,
            price = price,
            duration = duration,
            description = description,
            deliverables = deliverables,
            includesPhotography = includesPhotography,
            includesVideography = includesVideography,
            includesCandidPhotography = includesCandid,
            includesCinematicFilm = includesCinematic,
            includesDrone = includesDrone,
            includesAlbum = includesAlbum,
            maxFunctions = maxFunctions,
            deliveryTimelineDays = deliveryDays,
            terms = terms,
            createdAt = createdAt
        )
    }

    private fun filterAndSortStudios(
        rawStudios: List<Studio>,
        searchQuery: String,
        filter: StudioFilter,
        serviceCategory: String?
    ): List<Studio> {
        var result = rawStudios

        // 1. Filter by Service Category if specified
        if (!serviceCategory.isNullOrBlank()) {
            val catLower = serviceCategory.trim().lowercase()
            result = result.filter { studio ->
                studio.services.any { it.lowercase().contains(catLower) } ||
                studio.servicesOffered.any { it.lowercase().contains(catLower) } ||
                studio.name.lowercase().contains(catLower) ||
                studio.description.lowercase().contains(catLower)
            }
        }

        // 2. Filter by Search Query (Name, Area, City, Services, Bio)
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.trim().lowercase()
            result = result.filter { studio ->
                studio.name.lowercase().contains(query) ||
                studio.area.lowercase().contains(query) ||
                studio.city.lowercase().contains(query) ||
                (studio.location?.address?.lowercase()?.contains(query) == true) ||
                (studio.location?.area?.lowercase()?.contains(query) == true) ||
                studio.services.any { it.lowercase().contains(query) } ||
                studio.servicesOffered.any { it.lowercase().contains(query) } ||
                studio.description.lowercase().contains(query)
            }
        }

        // 3. Apply Filter Option & Prioritization
        return when (filter) {
            StudioFilter.ALL -> {
                result.sortedWith(
                    compareByDescending<Studio> { it.isSponsoredActive }
                        .thenByDescending { it.isVerifiedActive }
                        .thenByDescending { it.rating }
                        .thenByDescending { it.reviewCount }
                )
            }
            StudioFilter.TOP_RATED -> {
                result.filter { it.rating >= 4.0f }
                    .sortedWith(
                        compareByDescending<Studio> { it.isSponsoredActive }
                            .thenByDescending { it.isVerifiedActive }
                            .thenByDescending { it.rating }
                            .thenByDescending { it.reviewCount }
                    )
            }
            StudioFilter.VERIFIED -> {
                result.filter { it.isVerifiedActive }
                    .sortedWith(
                        compareByDescending<Studio> { it.isSponsoredActive }
                            .thenByDescending { it.rating }
                    )
            }
            StudioFilter.NEAR_ME -> {
                result.sortedWith(
                    compareByDescending<Studio> { it.isSponsoredActive }
                        .thenByDescending { it.isVerifiedActive }
                        .thenBy { if (it.distanceKm > 0.0) it.distanceKm else (it.distance ?: 99.0) }
                )
            }
            StudioFilter.BUDGET_FRIENDLY -> {
                result.sortedWith(
                    compareByDescending<Studio> { it.isSponsoredActive }
                        .thenByDescending { it.isVerifiedActive }
                        .thenBy { if (it.startingPrice > 0.0) it.startingPrice else it.startingPackagePrice }
                )
            }
        }
    }

    override fun getStudios(searchQuery: String, filter: StudioFilter, serviceCategory: String?): Flow<List<Studio>> = callbackFlow {
        // Emit locally cached state first for instant UI response
        val initialCached = com.example.data.MockDataManager.studios.value
        trySend(filterAndSortStudios(initialCached, searchQuery, filter, serviceCategory))

        // Set up real-time listener from Cloud Firestore 'studios' collection
        val listener = firestore.collection("studios").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseStudioRep", "Error in studios snapshot listener: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetched = snapshot.documents.mapNotNull { studioDocumentToModel(it) }
                fetched.forEach { com.example.data.MockDataManager.saveStudio(it) }
                val filtered = filterAndSortStudios(fetched, searchQuery, filter, serviceCategory)
                trySend(filtered)
            }
        }

        awaitClose { listener.remove() }
    }

    override fun getNearbyStudios(lat: Double, lng: Double, radiusKm: Double): Flow<List<Studio>> = flow {
        try {
            val snapshot = firestore.collection("studios").get().await()
            val studios = snapshot.documents.mapNotNull { studioDocumentToModel(it) }
            emit(studios.sortedBy { it.distanceKm })
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error getNearbyStudios: ${e.message}")
            emit(emptyList())
        }
    }

    override fun getVerifiedStudios(): Flow<List<Studio>> = flow {
        try {
            val snapshot = firestore.collection("studios").get().await()
            val studios = snapshot.documents.mapNotNull { studioDocumentToModel(it) }.filter { it.isVerifiedActive }
            emit(studios)
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error getVerifiedStudios: ${e.message}")
            emit(emptyList())
        }
    }

    override fun getTopRatedStudios(): Flow<List<Studio>> = flow {
        try {
            val snapshot = firestore.collection("studios").get().await()
            val studios = snapshot.documents.mapNotNull { studioDocumentToModel(it) }
            emit(studios.sortedByDescending { it.rating })
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error getTopRatedStudios: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getStudioById(id: String): Studio? {
        try {
            val snapshot = firestore.collection("studios").document(id).get().await()
            if (snapshot.exists()) {
                val studio = studioDocumentToModel(snapshot)
                if (studio != null) {
                    com.example.data.MockDataManager.saveStudio(studio)
                    return studio
                }
            }
            // Fallback query by ownerId
            val querySnapshot = firestore.collection("studios").whereEqualTo("ownerId", id).get().await()
            val queryDoc = querySnapshot.documents.firstOrNull()
            if (queryDoc != null && queryDoc.exists()) {
                val studio = studioDocumentToModel(queryDoc)
                if (studio != null) {
                    com.example.data.MockDataManager.saveStudio(studio)
                    return studio
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error getting studio by ID: ${e.message}")
        }
        return com.example.data.MockDataManager.getStudioById(id)
    }

    override suspend fun getStudioByOwnerId(ownerId: String): Studio? {
        try {
            // 1. Direct document lookup at /studios/{ownerUid}
            val docSnapshot = firestore.collection("studios").document(ownerId).get().await()
            if (docSnapshot.exists()) {
                val studio = studioDocumentToModel(docSnapshot)
                if (studio != null) {
                    com.example.data.MockDataManager.saveStudio(studio)
                    return studio
                }
            }

            // 2. Query by ownerId field
            val querySnapshot = firestore.collection("studios").whereEqualTo("ownerId", ownerId).get().await()
            val queryDoc = querySnapshot.documents.firstOrNull()
            if (queryDoc != null && queryDoc.exists()) {
                val studio = studioDocumentToModel(queryDoc)
                if (studio != null) {
                    com.example.data.MockDataManager.saveStudio(studio)
                    return studio
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error getting studio by ownerId: ${e.message}")
        }
        return com.example.data.MockDataManager.getStudioByOwnerId(ownerId)
    }

    override suspend fun saveStudio(studio: Studio) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("FirebaseStudioRep", "Unauthorized: User must be authenticated to update studio profile.")
            return
        }
        val currentUid = currentUser.uid

        // 1. Update local cache with authenticated UID
        val securedStudio = studio.copy(
            id = currentUid,
            ownerId = currentUid
        )
        com.example.data.MockDataManager.saveStudio(securedStudio)

        // 2. Strict mapping with authenticated currentUid
        val studioMap = hashMapOf<String, Any?>(
            "studioId" to currentUid,
            "id" to currentUid,
            "ownerId" to currentUid,
            "studioName" to studio.name,
            "name" to studio.name,
            "contactPerson" to studio.contactPerson,
            "phone" to studio.phone,
            "email" to studio.email,
            "area" to studio.area,
            "city" to (studio.city.ifBlank { "Prayagraj" }),
            "bio" to studio.description,
            "description" to studio.description,
            "services" to studio.services,
            "servicesOffered" to studio.services,
            "verified" to (studio.verified || studio.isVerified),
            "isVerified" to (studio.verified || studio.isVerified),
            "verifiedStatus" to studio.verifiedStatus,
            "isSponsored" to studio.isSponsored,
            "startingPrice" to studio.startingPrice,
            "startingPackagePrice" to studio.startingPrice,
            "experienceYears" to studio.experienceYears,
            "deliveryTimeDays" to studio.deliveryTimeDays,
            "workingHours" to studio.workingHours,
            "rating" to studio.rating,
            "reviewCount" to studio.reviewCount,
            "completedBookings" to studio.completedBookings,
            "deliveryPerformanceScore" to studio.deliveryPerformanceScore,
            "responseRateScore" to studio.responseRateScore,
            "coverImageUrl" to studio.coverImageUrl,
            "portfolioUrls" to studio.portfolioUrls,
            "subscriptionExpiresAt" to if (studio.subscriptionExpiresAt != null) com.google.firebase.Timestamp(java.util.Date(studio.subscriptionExpiresAt)) else null,
            "sponsoredExpiresAt" to if (studio.sponsoredExpiresAt != null) com.google.firebase.Timestamp(java.util.Date(studio.sponsoredExpiresAt)) else null,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        if (studio.location != null) {
            studioMap["location"] = hashMapOf(
                "latitude" to studio.location.latitude,
                "longitude" to studio.location.longitude,
                "address" to studio.location.address,
                "city" to (studio.location.city.ifBlank { "Prayagraj" }),
                "area" to studio.location.area
            )
            studioMap["address"] = studio.location.address
        } else {
            studioMap["location"] = hashMapOf(
                "latitude" to 25.4526,
                "longitude" to 81.8349,
                "address" to "${studio.area}, ${studio.city.ifBlank { "Prayagraj" }}",
                "city" to (studio.city.ifBlank { "Prayagraj" }),
                "area" to studio.area
            )
            studioMap["address"] = "${studio.area}, ${studio.city.ifBlank { "Prayagraj" }}"
        }

        try {
            // Write directly to /studios/{currentUid} ONLY
            firestore.collection("studios").document(currentUid).set(studioMap, com.google.firebase.firestore.SetOptions.merge()).await()
            Log.d("FirebaseStudioRep", "Studio successfully written to Firestore: $currentUid")
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error writing studio to Firestore: ${e.message}", e)
        }
    }

    override suspend fun getPackagesForStudio(studioId: String): List<StudioPackage> {
        try {
            // 1. Fetch from subcollection /studios/{studioId}/packages
            val subSnapshot = firestore.collection("studios").document(studioId)
                .collection("packages").get().await()
            val subPackages = subSnapshot.documents.mapNotNull { packageDocumentToModel(it, studioId) }
            if (subPackages.isNotEmpty()) {
                subPackages.forEach { com.example.data.MockDataManager.savePackage(it) }
                return subPackages
            }

            // 2. Fetch from top-level studioPackages collection
            val topSnapshot = firestore.collection("studioPackages").whereEqualTo("studioId", studioId).get().await()
            val topPackages = topSnapshot.documents.mapNotNull { packageDocumentToModel(it, studioId) }
            if (topPackages.isNotEmpty()) {
                topPackages.forEach { com.example.data.MockDataManager.savePackage(it) }
                return topPackages
            }
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error fetching packages from Firestore: ${e.message}")
        }

        return com.example.data.MockDataManager.getPackagesForStudio(studioId)
    }

    override fun observePackagesForStudio(studioId: String): Flow<List<StudioPackage>> = callbackFlow {
        // Emit initial data
        val initialList = getPackagesForStudio(studioId)
        trySend(initialList)

        // Setup real-time listener on /studios/{studioId}/packages
        val subCollectionRef = firestore.collection("studios").document(studioId).collection("packages")
        val listener = subCollectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val packages = snapshot.documents.mapNotNull { packageDocumentToModel(it, studioId) }
                packages.forEach { com.example.data.MockDataManager.savePackage(it) }
                trySend(packages)
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun savePackage(pkg: StudioPackage) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("FirebaseStudioRep", "Unauthorized: User must be authenticated to update packages.")
            return
        }
        val currentUid = currentUser.uid

        // 1. Save to local state with currentUid
        val securedPkg = pkg.copy(studioId = currentUid)
        com.example.data.MockDataManager.savePackage(securedPkg)

        val packageData = hashMapOf<String, Any?>(
            "packageId" to pkg.id,
            "id" to pkg.id,
            "studioId" to currentUid,
            "title" to (pkg.name.ifBlank { pkg.title }),
            "name" to (pkg.name.ifBlank { pkg.title }),
            "price" to pkg.price,
            "duration" to if (pkg.duration.isNotBlank()) pkg.duration else "${pkg.maxFunctions} Day(s) Coverage",
            "deliverables" to pkg.deliverables,
            "description" to pkg.description,
            "includesPhotography" to pkg.includesPhotography,
            "includesVideography" to pkg.includesVideography,
            "includesCandidPhotography" to pkg.includesCandidPhotography,
            "includesCinematicFilm" to pkg.includesCinematicFilm,
            "includesDrone" to pkg.includesDrone,
            "includesAlbum" to pkg.includesAlbum,
            "maxFunctions" to pkg.maxFunctions,
            "deliveryTimelineDays" to pkg.deliveryTimelineDays,
            "terms" to pkg.terms,
            "createdAt" to pkg.createdAt,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        try {
            // Write inside /studios/{currentUid}/packages/{packageId} subcollection
            firestore.collection("studios").document(currentUid)
                .collection("packages").document(pkg.id)
                .set(packageData, com.google.firebase.firestore.SetOptions.merge()).await()

            // Also write to top-level studioPackages collection
            firestore.collection("studioPackages").document(pkg.id)
                .set(packageData, com.google.firebase.firestore.SetOptions.merge()).await()

            // Update studio document timestamp
            firestore.collection("studios").document(currentUid).set(
                mapOf("updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()

            Log.d("FirebaseStudioRep", "Package successfully written to Firestore: ${pkg.id}")
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error writing package to Firestore: ${e.message}", e)
        }
    }

    override suspend fun deletePackage(packageId: String) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("FirebaseStudioRep", "Unauthorized: User must be authenticated to delete packages.")
            return
        }
        val currentUid = currentUser.uid
        com.example.data.MockDataManager.deletePackage(packageId)

        try {
            firestore.collection("studios").document(currentUid)
                .collection("packages").document(packageId).delete().await()
            firestore.collection("studioPackages").document(packageId).delete().await()
            Log.d("FirebaseStudioRep", "Package successfully deleted from Firestore: $packageId")
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error deleting package from Firestore: ${e.message}", e)
        }
    }

    override suspend fun updateMonetization(
        studioId: String,
        isVerified: Boolean,
        isSponsored: Boolean,
        subscriptionExpiresAt: Long?,
        sponsoredExpiresAt: Long?
    ) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val targetId = currentUser?.uid ?: studioId

        val updateMap = hashMapOf<String, Any?>(
            "isVerified" to isVerified,
            "verified" to isVerified,
            "verifiedStatus" to (if (isVerified) "VERIFIED" else "PENDING"),
            "isSponsored" to isSponsored,
            "subscriptionExpiresAt" to if (subscriptionExpiresAt != null) com.google.firebase.Timestamp(java.util.Date(subscriptionExpiresAt)) else null,
            "sponsoredExpiresAt" to if (sponsoredExpiresAt != null) com.google.firebase.Timestamp(java.util.Date(sponsoredExpiresAt)) else null,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // Update local cache
        val local = com.example.data.MockDataManager.getStudioById(targetId) ?: com.example.data.MockDataManager.getStudioByOwnerId(targetId)
        if (local != null) {
            val updated = local.copy(
                isVerified = isVerified,
                verified = isVerified,
                verifiedStatus = if (isVerified) "VERIFIED" else "PENDING",
                isSponsored = isSponsored,
                subscriptionExpiresAt = subscriptionExpiresAt,
                sponsoredExpiresAt = sponsoredExpiresAt
            )
            com.example.data.MockDataManager.saveStudio(updated)
        }

        try {
            firestore.collection("studios").document(targetId).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).await()
            Log.d("FirebaseStudioRep", "Monetization updated in Firestore for studio: $targetId")
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error updating monetization in Firestore: ${e.message}", e)
        }
    }

    override suspend fun calculateRankingScore(studio: Studio): Double {
        var score = studio.rating * 20.0
        if (studio.isVerifiedActive) score += 30.0
        if (studio.isSponsoredActive) score += 50.0
        score += (studio.completedBookings * 0.2).coerceAtMost(20.0)
        score += (studio.reviewCount * 0.1).coerceAtMost(15.0)
        return score
    }

    override suspend fun uploadPortfolioImage(uri: android.net.Uri): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return null
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            .child("studio_portfolios/${currentUser.uid}/${java.util.UUID.randomUUID()}.jpg")
        
        return try {
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            // Append to Firestore
            firestore.collection("studios").document(currentUser.uid)
                .update("portfolioUrls", com.google.firebase.firestore.FieldValue.arrayUnion(downloadUrl)).await()
                
            // Update local cache
            val local = com.example.data.MockDataManager.getStudioById(currentUser.uid) ?: com.example.data.MockDataManager.getStudioByOwnerId(currentUser.uid)
            if (local != null) {
                val updatedList = local.portfolioUrls.toMutableList()
                if (!updatedList.contains(downloadUrl)) {
                    updatedList.add(downloadUrl)
                }
                com.example.data.MockDataManager.saveStudio(local.copy(portfolioUrls = updatedList, portfolioThumbnails = updatedList))
            }
            
            downloadUrl
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error uploading portfolio image: ${e.message}", e)
            null
        }
    }

    override suspend fun removePortfolioImage(imageUrl: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        
        try {
            // Remove from Firestore
            firestore.collection("studios").document(currentUser.uid)
                .update("portfolioUrls", com.google.firebase.firestore.FieldValue.arrayRemove(imageUrl)).await()
                
            // Update local cache
            val local = com.example.data.MockDataManager.getStudioById(currentUser.uid) ?: com.example.data.MockDataManager.getStudioByOwnerId(currentUser.uid)
            if (local != null) {
                val updatedList = local.portfolioUrls.toMutableList()
                updatedList.remove(imageUrl)
                com.example.data.MockDataManager.saveStudio(local.copy(portfolioUrls = updatedList, portfolioThumbnails = updatedList))
            }

            // Remove from Storage
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            
        } catch (e: Exception) {
            Log.e("FirebaseStudioRep", "Error removing portfolio image: ${e.message}", e)
        }
    }
}
