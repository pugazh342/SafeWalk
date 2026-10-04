package com.example.backend

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackendDispatchReport(
    val supabaseStatus: String = "Standby",
    val discordStatus: String = "Standby",
    val emailStatus: String = "Standby",
    val isSuccess: Boolean = false
)

class BackendNotificationManager {

    val supabaseService = SupabaseService()
    val discordService = DiscordWebhookService()
    val emailService = EmailNotificationService()

    private val _dispatchReport = MutableStateFlow(BackendDispatchReport())
    val dispatchReport: StateFlow<BackendDispatchReport> = _dispatchReport.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun dispatchCompleteSosAlert(
        scope: CoroutineScope,
        walkId: String,
        latitude: Double,
        longitude: Double,
        triggerReason: String,
        volunteerName: String?,
        volunteerPhone: String?,
        destination: String,
        batteryLevel: Int,
        trackingUrl: String,
        onComplete: ((report: BackendDispatchReport) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.IO) {
            _isBusy.value = true
            _dispatchReport.value = BackendDispatchReport(
                supabaseStatus = "Syncing with Cloud DB...",
                discordStatus = "Broadcasting to Discord Webhook...",
                emailStatus = "Transmitting to Emergency Email..."
            )

            // 1. Supabase Cloud Database Log
            var supaResult = "Not attempted"
            try {
                val res = supabaseService.logSosAlertToSupabase(
                    walkId = walkId,
                    latitude = latitude,
                    longitude = longitude,
                    triggerReason = triggerReason,
                    volunteerName = volunteerName,
                    batteryLevel = batteryLevel,
                    trackingUrl = trackingUrl
                )
                supaResult = if (res.isSuccess) "Synced to Supabase ✅" else "Logged: ${res.exceptionOrNull()?.message ?: "Error"}"
            } catch (e: Exception) {
                supaResult = "Supabase: ${e.message}"
            }

            // 2. Discord Webhook Broadcast
            var discordResult = "Not attempted"
            try {
                val res = discordService.sendEmergencySosAlert(
                    walkId = walkId,
                    latitude = latitude,
                    longitude = longitude,
                    triggerReason = triggerReason,
                    volunteerName = volunteerName,
                    volunteerPhone = volunteerPhone,
                    destination = destination,
                    batteryLevel = batteryLevel,
                    trackingUrl = trackingUrl
                )
                discordResult = if (res.isSuccess) "Delivered to Discord Channel ✅" else "Discord: ${res.exceptionOrNull()?.message}"
            } catch (e: Exception) {
                discordResult = "Discord error: ${e.message}"
            }

            // 3. Email Alert Dispatch (Gmail SMTP)
            var emailResult = "Not attempted"
            try {
                val res = emailService.sendEmergencyEmail(
                    walkId = walkId,
                    latitude = latitude,
                    longitude = longitude,
                    triggerReason = triggerReason,
                    volunteerName = volunteerName,
                    volunteerPhone = volunteerPhone,
                    destination = destination,
                    batteryLevel = batteryLevel,
                    trackingUrl = trackingUrl
                )
                emailResult = if (res.isSuccess) "Dispatched to Emergency Email ✅" else "Email: ${res.exceptionOrNull()?.message}"
            } catch (e: Exception) {
                emailResult = "Email error: ${e.message}"
            }

            val finalReport = BackendDispatchReport(
                supabaseStatus = supaResult,
                discordStatus = discordResult,
                emailStatus = emailResult,
                isSuccess = supaResult.contains("✅") || discordResult.contains("✅") || emailResult.contains("✅")
            )

            _dispatchReport.value = finalReport
            _isBusy.value = false
            onComplete?.invoke(finalReport)
        }
    }

    fun notifyWalkStarted(
        scope: CoroutineScope,
        walkId: String,
        destination: String,
        volunteerName: String?
    ) {
        scope.launch(Dispatchers.IO) {
            val escortDesc = if (volunteerName != null) "Escorted by $volunteerName" else "Walking alone (Guardian tracking active)"
            discordService.sendWalkStatusNotification(
                title = "🚶‍♀️ SafeWalk Journey Initiated",
                description = "User has started a nighttime protected walk. $escortDesc",
                destination = destination,
                statusColor = 7949311 // Amethyst/Cyan
            )
            supabaseService.recordWalkSessionToSupabase(
                walkId = walkId,
                destination = destination,
                status = "ACTIVE",
                volunteerName = volunteerName,
                distanceMeters = 0,
                durationSeconds = 0
            )
        }
    }

    fun notifyWalkCompleted(
        scope: CoroutineScope,
        walkId: String,
        destination: String,
        volunteerName: String?,
        distanceMeters: Int,
        durationSeconds: Int
    ) {
        scope.launch(Dispatchers.IO) {
            val min = durationSeconds / 60
            val sec = durationSeconds % 60
            discordService.sendWalkStatusNotification(
                title = "🎉 SafeWalk Journey Safely Completed",
                description = "User has safely reached the destination! Total distance: ${distanceMeters}m in %02d:%02d.".format(min, sec),
                destination = destination,
                statusColor = 65398 // Safe Green
            )
            supabaseService.recordWalkSessionToSupabase(
                walkId = walkId,
                destination = destination,
                status = "COMPLETED",
                volunteerName = volunteerName,
                distanceMeters = distanceMeters,
                durationSeconds = durationSeconds
            )
        }
    }
}
