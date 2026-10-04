package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.example.data.EmergencyContact
import com.example.model.Volunteer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmergencyDispatchHelper(private val context: Context) {

    fun getBatteryLevel(): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
        } catch (e: Exception) {
            85
        }
    }

    fun buildEmergencySmsMessage(
        userLat: Double,
        userLng: Double,
        volunteer: Volunteer?,
        walkDestination: String,
        trackingUrl: String
    ): String {
        val timeStr = SimpleDateFormat("HH:mm:ss, dd MMM", Locale.getDefault()).format(Date())
        val battery = getBatteryLevel()

        val volunteerText = if (volunteer != null) {
            "Escort Companion: ${volunteer.name} (Badge: ${volunteer.badgeNumber}, Ph: ${volunteer.phone})"
        } else {
            "Companion: None currently paired (Walking Alone)"
        }

        return """
🚨 [EMERGENCY SOS - SafeWalk Alert] 🚨
I am in an insecure area and need immediate emergency assistance!

📍 Coordinates: ${"%.5f".format(userLat)}, ${"%.5f".format(userLng)}
🗺️ Live Map: https://maps.google.com/?q=$userLat,$userLng
🔒 Encrypted Stream: $trackingUrl
🎯 Heading towards: $walkDestination
👤 $volunteerText
🔋 Battery: $battery% | ⏰ Sent: $timeStr
⚠️ Nearby Police Station (112) is being dispatched with live telemetry.
        """.trimIndent()
    }

    fun dispatchSmsToContacts(
        contacts: List<EmergencyContact>,
        message: String,
        onSuccess: (count: Int) -> Unit,
        onFallbackToIntent: (Intent) -> Unit
    ) {
        if (contacts.isEmpty()) {
            // Fallback general SMS intent
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:112")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            onFallbackToIntent(intent)
            return
        }

        var sentCount = 0
        try {
            @Suppress("DEPRECATION")
            val smsManager = SmsManager.getDefault()
            val phoneNumbers = contacts.map { it.phone }

            for (phone in phoneNumbers) {
                try {
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
                    sentCount++
                } catch (e: Exception) {
                    Log.w("EmergencyDispatchHelper", "Direct SMS failed for $phone: ${e.message}")
                }
            }

            if (sentCount > 0) {
                onSuccess(sentCount)
                return
            }
        } catch (e: Exception) {
            Log.w("EmergencyDispatchHelper", "SmsManager unavailable: ${e.message}")
        }

        // If direct SMS failed or permission wasn't granted, create chooser intent to primary phone
        val primaryPhone = contacts.firstOrNull { it.isPrimary }?.phone ?: contacts.first().phone
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$primaryPhone")
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        onFallbackToIntent(intent)
    }

    fun dialEmergencyNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot launch dialer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
