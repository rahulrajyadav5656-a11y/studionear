package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.Booking
import com.example.data.models.BookingStatus
import com.example.data.models.Review
import com.example.data.models.ReviewStatus
import com.example.data.models.Complaint
import com.example.data.models.ComplaintStatus
import com.example.data.models.Notification
import com.example.data.models.Studio
import com.example.data.models.StudioPackage
import com.example.data.models.Location
import com.example.data.models.ChatMessage
import com.example.data.models.ChatThread
import com.example.data.models.BlockedDate
import com.example.data.models.PortfolioItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object MockDataManager {
    private var prefs: SharedPreferences? = null
    private val gson = Gson()
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    private val _complaints = MutableStateFlow<List<Complaint>>(emptyList())
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    private val _studios = MutableStateFlow<List<Studio>>(emptyList())
    val studios: StateFlow<List<Studio>> = _studios.asStateFlow()

    private val _packages = MutableStateFlow<List<StudioPackage>>(emptyList())
    val packages: StateFlow<List<StudioPackage>> = _packages.asStateFlow()

    private val _portfolios = MutableStateFlow<List<PortfolioItem>>(emptyList())
    val portfolios: StateFlow<List<PortfolioItem>> = _portfolios.asStateFlow()

    private val _chatThreads = MutableStateFlow<List<ChatThread>>(emptyList())
    val chatThreads: StateFlow<List<ChatThread>> = _chatThreads.asStateFlow()
    
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    
    private val _blockedDates = MutableStateFlow<List<BlockedDate>>(emptyList())
    val blockedDates: StateFlow<List<BlockedDate>> = _blockedDates.asStateFlow()


    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
            loadData()
        }
    }

    private fun loadData() {
        val p = prefs ?: return
        
        val bookingsJson = p.getString("bookings", null)
        if (bookingsJson != null) {
            val type = object : TypeToken<List<Booking>>() {}.type
            _bookings.value = gson.fromJson(bookingsJson, type)
        } else {
            // Initial mock data if empty
            val mockBookings = listOf(
                Booking(
                    id = "SN-1001",
                    clientId = "user_123", // Will match any user for testing if needed
                    studioId = "1",
                    packageId = "pkg_1",
                    status = BookingStatus.COMPLETED,
                    totalAmount = 50000.0,
                    paidAmount = 50000.0
                ),
                Booking(
                    id = "SN-1002",
                    clientId = "user_123",
                    studioId = "2",
                    packageId = "pkg_2",
                    status = BookingStatus.PENDING,
                    totalAmount = 30000.0,
                    paidAmount = 10000.0
                )
            )
            _bookings.value = mockBookings
            saveData()
        }
        
        val studiosJson = p.getString("studios", null)
        if (studiosJson != null) {
            val type = object : TypeToken<List<Studio>>() {}.type
            _studios.value = gson.fromJson(studiosJson, type) ?: emptyList()
        } else {
            _studios.value = emptyList()
        }
        
        val packagesJson = p.getString("packages", null)
        if (packagesJson != null) {
            val type = object : TypeToken<List<StudioPackage>>() {}.type
            _packages.value = gson.fromJson(packagesJson, type) ?: emptyList()
        } else {
            _packages.value = emptyList()
        }

        val portfoliosJson = p.getString("portfolios", null)
        if (portfoliosJson != null) {
            val type = object : TypeToken<List<PortfolioItem>>() {}.type
            _portfolios.value = gson.fromJson(portfoliosJson, type)
        }

        
        val chatThreadsJson = p.getString("chat_threads", null)
        if (chatThreadsJson != null) {
            val type = object : TypeToken<List<ChatThread>>() {}.type
            _chatThreads.value = gson.fromJson(chatThreadsJson, type)
        }
        val chatMsgsJson = p.getString("chat_messages", null)
        if (chatMsgsJson != null) {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            _chatMessages.value = gson.fromJson(chatMsgsJson, type)
        }
        val blockedDatesJson = p.getString("blocked_dates", null)
        if (blockedDatesJson != null) {
            val type = object : TypeToken<List<BlockedDate>>() {}.type
            _blockedDates.value = gson.fromJson(blockedDatesJson, type)
        }

        val reviewsJson = p.getString("reviews", null)
        if (reviewsJson != null) {
            val type = object : TypeToken<List<Review>>() {}.type
            _reviews.value = gson.fromJson(reviewsJson, type)
        }
        val notifJson = p.getString("notifications", null)
        if (notifJson != null) {
            val type = object : TypeToken<List<Notification>>() {}.type
            _notifications.value = gson.fromJson(notifJson, type)
        }
        val complaintsJson = p.getString("complaints", null)
        if (complaintsJson != null) {
            val type = object : TypeToken<List<Complaint>>() {}.type
            _complaints.value = gson.fromJson(complaintsJson, type)
        }
    }

    private fun saveData() {
        val p = prefs ?: return
        p.edit().apply {
            putString("bookings", gson.toJson(_bookings.value))
            putString("reviews", gson.toJson(_reviews.value))
            putString("notifications", gson.toJson(_notifications.value))
            putString("complaints", gson.toJson(_complaints.value))
            putString("studios", gson.toJson(_studios.value))
            putString("packages", gson.toJson(_packages.value))
            putString("portfolios", gson.toJson(_portfolios.value))

            putString("chat_threads", gson.toJson(_chatThreads.value))
            putString("chat_messages", gson.toJson(_chatMessages.value))
            putString("blocked_dates", gson.toJson(_blockedDates.value))

        }.apply()
    }

    fun submitComplaint(complaint: Complaint) {
        val current = _complaints.value.toMutableList()
        current.add(complaint.copy(id = UUID.randomUUID().toString(), status = ComplaintStatus.SUBMITTED))
        _complaints.value = current
        saveData()
    }

    fun submitReview(review: Review) {
        val currentReviews = _reviews.value.toMutableList()
        currentReviews.add(review.copy(id = UUID.randomUUID().toString(), status = ReviewStatus.APPROVED)) // Auto-approve for prototype
        _reviews.value = currentReviews
        
        // Notify Studio Owner
        val title = if (review.videoUri != null) "Client video review received" else "New client review received"
        val message = "Client rating: ${review.overallRating} | Status: APPROVED | Booking ID: ${review.bookingId}"
        
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            userId = review.studioId,
            title = title,
            message = message
        )
        val currentNotifs = _notifications.value.toMutableList()
        currentNotifs.add(0, notification)
        _notifications.value = currentNotifs
        
        saveData()
    }

    fun getReviewsForStudio(studioId: String): List<Review> {
        return _reviews.value.filter { it.studioId == studioId && it.status == ReviewStatus.APPROVED }
    }
    
    fun getReviewsForStudioIncludingPending(studioId: String): List<Review> {
         return _reviews.value.filter { it.studioId == studioId }
    }

    fun hasReviewedBooking(bookingId: String): Boolean {
        return _reviews.value.any { it.bookingId == bookingId }
    }

    fun getStudioByOwnerId(ownerId: String): Studio? {
        return _studios.value.find { it.ownerId == ownerId }
    }

    fun getStudioById(studioId: String): Studio? {
        return _studios.value.find { it.id == studioId }
    }

    fun saveStudio(studio: Studio) {
        val current = _studios.value.toMutableList()
        val index = current.indexOfFirst { it.id == studio.id }
        if (index >= 0) {
            current[index] = studio
        } else {
            current.add(studio)
        }
        _studios.value = current
        saveData()
    }
    
    fun getPackagesForStudio(studioId: String): List<StudioPackage> {
        return _packages.value.filter { it.studioId == studioId }
    }
    
    fun savePackage(pkg: StudioPackage) {
        val current = _packages.value.toMutableList()
        val index = current.indexOfFirst { it.id == pkg.id }
        if (index >= 0) {
            current[index] = pkg
        } else {
            current.add(pkg)
        }
        _packages.value = current
        saveData()
    }
    
    fun deletePackage(packageId: String) {
        _packages.value = _packages.value.filterNot { it.id == packageId }
        saveData()
    }
    
    fun getPortfolioForStudio(studioId: String): List<PortfolioItem> {
        return _portfolios.value.filter { it.studioId == studioId }
    }

    fun savePortfolioItem(item: PortfolioItem) {
        val current = _portfolios.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current[index] = item
        } else {
            current.add(item)
        }
        _portfolios.value = current
        saveData()
    }

    fun deletePortfolioItem(itemId: String) {
        _portfolios.value = _portfolios.value.filterNot { it.id == itemId }
        saveData()
    }
    
    
    fun saveBlockedDate(blockedDate: BlockedDate) {
        val current = _blockedDates.value.toMutableList()
        current.add(blockedDate)
        _blockedDates.value = current
        saveData()
    }

    fun removeBlockedDate(id: String) {
        _blockedDates.value = _blockedDates.value.filterNot { it.id == id }
        saveData()
    }
    
    fun rejectBooking(bookingId: String, reason: String) {
        val current = _bookings.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index >= 0) {
            current[index] = current[index].copy(status = com.example.data.models.BookingStatus.REJECTED, rejectionReason = reason)
            _bookings.value = current
            saveData()
        }
    }
    
    fun updateDeliveryNotes(bookingId: String, notes: String) {
        val current = _bookings.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index >= 0) {
            current[index] = current[index].copy(deliveryNotes = notes)
            _bookings.value = current
            saveData()
        }
    }
    
    fun updateDeliveryStatus(bookingId: String, status: com.example.data.models.DeliveryStatus) {
        val current = _bookings.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index >= 0) {
            current[index] = current[index].copy(deliveryStatus = status)
            _bookings.value = current
            saveData()
        }
    }

    fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        val current = _bookings.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index >= 0) {
            current[index] = current[index].copy(status = status)
            _bookings.value = current
            saveData()
        }
    }


    
    fun replyToReview(reviewId: String, reply: String) {
        val current = _reviews.value.toMutableList()
        val index = current.indexOfFirst { it.id == reviewId }
        if (index >= 0) {
            current[index] = current[index].copy(ownerReply = reply)
            _reviews.value = current
            saveData()
        }
    }
}
