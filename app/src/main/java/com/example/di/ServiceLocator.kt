package com.example.di

import com.example.data.repositories.FirebaseStudioRepository
import com.example.data.repositories.FirebaseUserRepository
import com.example.data.repositories.StudioRepository
import com.example.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ServiceLocator {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val userRepository: UserRepository by lazy {
        FirebaseUserRepository(auth, firestore)
    }

    val studioRepository: StudioRepository by lazy {
        FirebaseStudioRepository(firestore)
    }
    val bookingRepository: com.example.data.repositories.BookingRepository by lazy {
        com.example.data.repositories.FirebaseBookingRepository(firestore)
    }
}
