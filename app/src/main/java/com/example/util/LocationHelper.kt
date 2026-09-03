package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocationName(context: Context): Pair<String, String> {
        return suspendCancellableCoroutine { continuation ->
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val (area, city) = resolveAddress(context, location.latitude, location.longitude)
                        continuation.resume(Pair(area, city))
                    } else {
                        continuation.resume(Pair("Civil Lines", "Prayagraj"))
                    }
                }
                .addOnFailureListener {
                    continuation.resume(Pair("Civil Lines", "Prayagraj"))
                }
        }
    }

    private fun resolveAddress(context: Context, lat: Double, lng: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val localArea = address.subLocality 
                    ?: address.locality 
                    ?: address.subAdminArea 
                    ?: "Local Area"
                
                val city = address.locality 
                    ?: address.subAdminArea 
                    ?: "India"

                Pair(localArea, city)
            } else {
                Pair("Selected Area", "India")
            }
        } catch (e: Exception) {
            Pair("Civil Lines", "Prayagraj")
        }
    }
}
