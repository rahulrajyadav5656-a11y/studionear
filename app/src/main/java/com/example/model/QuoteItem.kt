package com.example.model

data class ServiceRate(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val pricePerDay: Double = 0.0,
    val isSelected: Boolean = false
)

data class BookingQuote(
    val studioId: String = "",
    val studioName: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val eventType: String = "Wedding",
    val eventDate: String = "",
    val eventLocation: String = "",
    val selectedServices: List<ServiceRate> = emptyList(),
    val numberOfDays: Int = 1,
    val subtotal: Double = 0.0,
    val platformFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val advanceRequired: Double = 0.0,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)
