package com.example.data.repositories

import com.example.data.models.Booking
import com.example.data.models.Studio
import com.example.data.models.StudioPackage
import com.example.data.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun updateLocation(lat: Double, lng: Double, address: String)
    suspend fun toggleFavoriteStudio(studioId: String, isFavorite: Boolean)
}

enum class StudioFilter {
    ALL,
    TOP_RATED,
    VERIFIED,
    NEAR_ME,
    BUDGET_FRIENDLY
}

interface StudioRepository {
    fun getStudios(searchQuery: String = "", filter: StudioFilter = StudioFilter.ALL, serviceCategory: String? = null): Flow<List<Studio>>
    fun getNearbyStudios(lat: Double = 25.4358, lng: Double = 81.8463, radiusKm: Double = 50.0): Flow<List<Studio>>
    fun getVerifiedStudios(): Flow<List<Studio>>
    fun getTopRatedStudios(): Flow<List<Studio>>
    suspend fun getStudioById(id: String): Studio?
    suspend fun getStudioByOwnerId(ownerId: String): Studio?
    suspend fun saveStudio(studio: Studio)
    suspend fun getPackagesForStudio(studioId: String): List<StudioPackage>
    fun observePackagesForStudio(studioId: String): Flow<List<StudioPackage>>
    suspend fun savePackage(pkg: StudioPackage)
    suspend fun deletePackage(packageId: String)
    suspend fun updateMonetization(
        studioId: String,
        isVerified: Boolean,
        isSponsored: Boolean,
        subscriptionExpiresAt: Long?,
        sponsoredExpiresAt: Long?
    )
    suspend fun calculateRankingScore(studio: Studio): Double
    suspend fun uploadPortfolioImage(uri: android.net.Uri): String?
    suspend fun removePortfolioImage(imageUrl: String)
}

interface BookingRepository {
    suspend fun createBookingRequest(booking: Booking): String
    fun getClientBookings(clientId: String): Flow<List<Booking>>
    fun getOwnerBookings(ownerId: String): Flow<List<Booking>>
    suspend fun updateBookingStatus(bookingId: String, status: com.example.data.models.BookingStatus)
    suspend fun acceptAgreement(bookingId: String)
}

interface PaymentService {
    suspend fun initializePayment(amount: Double, currency: String = "INR"): String
    suspend fun verifyPayment(paymentId: String, orderId: String, signature: String): Boolean
}

interface NotificationService {
    suspend fun sendPushNotification(userId: String, title: String, message: String)
}
