package com.example.data.repositories

import android.util.Log
import com.example.data.models.Booking
import com.example.data.models.BookingStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseBookingRepository(
    private val firestore: FirebaseFirestore
) : BookingRepository {

    override suspend fun createBookingRequest(booking: Booking): String {
        return try {
            val docRef = if (booking.bookingId.isNotBlank()) {
                firestore.collection("bookings").document(booking.bookingId)
            } else {
                firestore.collection("bookings").document()
            }
            val newBooking = booking.copy(
                id = docRef.id,
                bookingId = docRef.id
            )
            docRef.set(newBooking).await()
            newBooking.id
        } catch (e: Exception) {
            Log.e("FirebaseBooking", "Error creating booking: \${e.message}")
            ""
        }
    }

    override fun getClientBookings(clientId: String): Flow<List<Booking>> = callbackFlow {
        val listener = firestore.collection("bookings")
            .whereEqualTo("clientId", clientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseBooking", "Listen failed: \${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java)
                    trySend(bookings)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getOwnerBookings(ownerId: String): Flow<List<Booking>> = callbackFlow {
        val listener = firestore.collection("bookings")
            .whereEqualTo("studioOwnerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseBooking", "Listen failed: \${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java)
                    trySend(bookings)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        try {
            firestore.collection("bookings").document(bookingId)
                .update("status", status.name).await()
        } catch (e: Exception) {
            Log.e("FirebaseBooking", "Error updating status: \${e.message}")
        }
    }

    override suspend fun acceptAgreement(bookingId: String) {
        try {
            firestore.collection("bookings").document(bookingId)
                .update("agreementAccepted", true).await()
        } catch (e: Exception) {
            Log.e("FirebaseBooking", "Error accepting agreement: \${e.message}")
        }
    }
}
