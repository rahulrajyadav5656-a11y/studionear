package com.example.data.models

import java.util.Date

data class User(
    val id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String? = null,
    val role: UserRole = UserRole.CLIENT,
    val savedLocation: Location? = null,
    val favoriteStudioIds: List<String> = emptyList()
)

enum class UserRole {
    CLIENT, STUDIO_OWNER, ADMIN
}

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val city: String = "Prayagraj",
    val area: String = ""
)

data class Studio(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val studioName: String = name,
    val contactPerson: String = "",
    val description: String = "",
    val bio: String = description,
    val verified: Boolean = false,
    val isVerified: Boolean = verified,
    val verifiedStatus: String = if (verified || isVerified) "VERIFIED" else "PENDING",
    val isSponsored: Boolean = false,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val startingPrice: Double = 0.0,
    val startingPackagePrice: Double = startingPrice,
    val location: Location? = null,
    val city: String = "Prayagraj",
    val area: String = "",
    val distanceKm: Double = 0.0,
    val distance: Double? = if (distanceKm > 0.0) distanceKm else null,
    val coverImageUrl: String = "",
    val coverPhoto: String? = if (coverImageUrl.isNotBlank()) coverImageUrl else null,
    val portfolioUrls: List<String> = emptyList(),
    val portfolioThumbnails: List<String> = portfolioUrls,
    val services: List<String> = emptyList(),
    val servicesOffered: List<String> = services,
    val phone: String = "",
    val email: String = "",
    val experienceYears: Int = 0,
    val travelRadiusKm: Int = 0,
    val languages: List<String> = emptyList(),
    val equipment: String = "",
    val teamSize: Int = 1,
    val verificationReason: String? = null,
    val followersCount: Int = 0,
    val completedBookings: Int = 0,
    val deliveryPerformanceScore: Float = 100f, // 0-100
    val responseRateScore: Float = 100f, // 0-100
    val availabilityStatus: StudioAvailability = StudioAvailability.AVAILABLE,
    val workingHours: String = "10:00 AM - 08:00 PM",
    val deliveryTimeDays: Int = 30,
    val subscriptionExpiresAt: Long? = null,
    val sponsoredExpiresAt: Long? = null
) {
    val isVerifiedActive: Boolean
        get() {
            val isV = verified || isVerified
            if (!isV) return false
            return subscriptionExpiresAt == null || subscriptionExpiresAt > System.currentTimeMillis()
        }

    val isSponsoredActive: Boolean
        get() {
            if (!isSponsored) return false
            return sponsoredExpiresAt == null || sponsoredExpiresAt > System.currentTimeMillis()
        }
}

enum class StudioAvailability {
    AVAILABLE, BUSY, UNAVAILABLE
}

data class StudioPackage(
    val id: String = "",
    val packageId: String = id,
    val studioId: String = "",
    val name: String = "",
    val title: String = name,
    val price: Double = 0.0,
    val duration: String = "",
    val description: String = "",
    val deliverables: String = "",
    val includesPhotography: Boolean = false,
    val includesVideography: Boolean = false,
    val includesCandidPhotography: Boolean = false,
    val includesCinematicFilm: Boolean = false,
    val includesDrone: Boolean = false,
    val includesAlbum: Boolean = false,
    val maxFunctions: Int = 1,
    val deliveryTimelineDays: Int = 30,
    val terms: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Booking(
    val id: String = "",
    val bookingId: String = "",
    val clientId: String = "",
    val clientEmail: String = "",
    val studioId: String = "",
    val studioOwnerId: String = "",
    val studioName: String = "",
    val packageId: String = "",
    val eventType: String = "",
    val eventDate: Long = 0L,
    val location: String = "",
    val notes: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val events: List<BookingEvent> = emptyList(),
    val rejectionReason: String? = null,
    val deliveryNotes: String? = null,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val agreementAccepted: Boolean = false,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.NOT_STARTED
)

enum class BookingStatus {
    PENDING, ACCEPTED, IN_PROGRESS, REJECTED, CANCELLED, COMPLETED, DECLINED
}

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class BookingEvent(
    val id: String = "",
    val name: String = "",
    val date: Long = 0,
    val startTime: String = "",
    val endTime: String = "",
    val location: Location? = null,
    val notes: String = ""
)

enum class DeliveryStatus {
    NOT_STARTED, IN_PROGRESS, DELIVERED, CLIENT_DOWNLOADED, DELIVERY_DELAYED, DISPUTED
}

enum class ReviewStatus { PENDING, APPROVED, REJECTED, HIDDEN, REPORTED }

data class Review(
    val id: String = "",
    val bookingId: String = "",
    val studioId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val studioName: String = "",
    val overallRating: Float = 0f,
    val photographyRating: Float = 0f,
    val videographyRating: Float = 0f,
    val behaviorRating: Float = 0f,
    val deliveryRating: Float = 0f,
    val valueForMoneyRating: Float = 0f,
    val writtenReview: String = "",
    val ownerReply: String? = null,
    val videoUri: String? = null,
    val photoUris: List<String> = emptyList(),
    val verifiedBooking: Boolean = true,
    val status: ReviewStatus = ReviewStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

data class Complaint(
    val id: String = "",
    val reporterId: String = "",
    val reportedId: String = "", // Studio or Client ID
    val bookingId: String = "",
    val reason: String = "",
    val description: String = "",
    val evidenceUrls: List<String> = emptyList(),
    val status: ComplaintStatus = ComplaintStatus.SUBMITTED,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ComplaintStatus {
    SUBMITTED, UNDER_REVIEW, WAITING_FOR_OTHER_PARTY, RESOLVED, REJECTED, ESCALATED
}

data class InvoiceItem(
    val id: String = "",
    val amount: Double = 0.0
)

data class Invoice(
    val id: String = "",
    val invoiceNumber: String = "",
    val bookingId: String = "",
    val date: Long = System.currentTimeMillis(),
    val clientName: String = "",
    val clientMobile: String = "",
    val clientEmail: String = "",
    val clientAddress: String = "",
    val studioName: String = "",
    val studioOwner: String = "",
    val studioContact: String = "",
    val studioAddress: String = "",
    val eventType: String = "",
    val eventDate: String = "",
    val eventTime: String = "",
    val eventLocation: String = "",
    val packageName: String = "",
    val packageServices: String = "",
    val basePrice: Double = 0.0,
    val items: List<InvoiceItem> = emptyList(),
    val rejectionReason: String? = null,
    val deliveryNotes: String? = null,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentStatus: String = "UNPAID"
)

data class BookingTerms(
    val id: String = "",
    val bookingId: String = "",
    val version: String = "1.0",
    val accepted: Boolean = false,
    val acceptedAt: Long = 0,
    val clientName: String = "",
    val studioName: String = "",
    val cancellationPolicy: String = "Cancellations made within 30 days of the event are subject to a 20% cancellation fee.",
    val reschedulingPolicy: String = "Rescheduling is allowed subject to studio availability.",
    val deliveryPolicy: String = "Deliverables will be provided within 30 days post-event.",
    val retentionPolicy: String = "Raw and edited files will be retained for 6 months after delivery.",
    val paymentMilestones: String = "20% advance, 60% on event day, 20% on delivery."
)

data class PaymentRecord(
    val id: String = "",
    val bookingId: String = "",
    val invoiceId: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
    val method: String = "UPI"
)

data class PortfolioItem(
    val id: String = "",
    val studioId: String = "",
    val mediaUri: String = "",
    val category: String = "",
    val caption: String = "",
    val date: Long = System.currentTimeMillis(),
    val visibility: Boolean = true
)


data class ChatMessage(
    val id: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ChatThread(
    val id: String = "",
    val clientId: String = "",
    val studioId: String = "",
    val bookingId: String? = null,
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)

data class BlockedDate(
    val id: String = "",
    val studioId: String = "",
    val date: Long = 0,
    val reason: String = ""
)
