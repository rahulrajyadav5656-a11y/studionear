package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.content.ActivityNotFoundException

object ContactUtils {
    fun sanitizePhoneNumber(phone: String): String {
        val clean = phone.replace(Regex("[^0-9+]"), "")
        if (clean.isEmpty()) return ""
        if (!clean.startsWith("+")) {
            // Assume Indian number if 10 digits
            if (clean.length == 10) {
                return "+91$clean"
            }
            return "+$clean"
        }
        return clean
    }

    fun openDialer(context: Context, phone: String) {
        if (phone.isBlank()) {
            Toast.makeText(context, "Studio contact details not updated yet.", Toast.LENGTH_SHORT).show()
            return
        }
        val sanitizedPhone = sanitizePhoneNumber(phone)
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitizedPhone"))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Dialer app not found.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, phone: String, message: String = "Hello! I found your studio on StudioNear and would like to inquire about booking.") {
        if (phone.isBlank()) {
            Toast.makeText(context, "Studio contact details not updated yet.", Toast.LENGTH_SHORT).show()
            return
        }
        val sanitizedPhone = sanitizePhoneNumber(phone)
        val encodedMessage = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$sanitizedPhone&text=$encodedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp or Web Browser not found.", Toast.LENGTH_SHORT).show()
        }
    }
}
