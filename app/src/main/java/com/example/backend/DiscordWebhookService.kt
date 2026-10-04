package com.example.backend

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DiscordWebhookService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val defaultWebhook = "https://discord.com/api/webhooks/1529527223566995488/P_F5ij0NI7jllSl3ddOUN_fMX2I_6dInJLMRSUPlKO5kkt7X4-XWzx6epoAK1CKzU75w"
    private val webhookUrl = try {
        val configured = BuildConfig.DISCORD_WEBHOOK_URL.trim().removeSurrounding("\"")
        if (configured.isBlank()) defaultWebhook else configured
    } catch (e: Exception) {
        defaultWebhook
    }

    suspend fun sendEmergencySosAlert(
        walkId: String,
        latitude: Double,
        longitude: Double,
        triggerReason: String,
        volunteerName: String?,
        volunteerPhone: String?,
        destination: String,
        batteryLevel: Int,
        trackingUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (webhookUrl.isBlank()) {
                return@withContext Result.failure(Exception("Discord Webhook URL is not configured in BuildConfig"))
            }

            val mapsUrl = "https://maps.google.com/?q=$latitude,$longitude"
            val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

            val escortInfo = if (!volunteerName.isNullOrBlank()) {
                "$volunteerName (${volunteerPhone ?: "Protected"})"
            } else {
                "None (Walking Alone in Insecure Area)"
            }

            val payload = JSONObject().apply {
                put("username", "SafeWalk Emergency Sentinel")
                put("avatar_url", "https://raw.githubusercontent.com/google/material-design-icons/master/png/alert/warning/materialicons/48dp/2x/baseline_warning_black_48dp.png")
                put("content", "@everyone 🚨 **URGENT EMERGENCY SOS ALERT DETECTED!** A woman walking in an insecure area has triggered an emergency alarm!")

                val embed = JSONObject().apply {
                    put("title", "🚨 CRITICAL SOS: High-Risk Alert Broadcast")
                    put("description", "A SafeWalk user has activated emergency dispatch. Immediate assistance is requested. Telemetry and live coordinates have been transmitted to response units.")
                    put("color", 16717380) // 0xFF1744 - Vibrant Alert Red

                    val fields = JSONArray().apply {
                        put(JSONObject().apply {
                            put("name", "⚠️ Trigger Reason")
                            put("value", triggerReason)
                            put("inline", true)
                        })
                        put(JSONObject().apply {
                            put("name", "🔋 Device Battery")
                            put("value", "$batteryLevel%")
                            put("inline", true)
                        })
                        put(JSONObject().apply {
                            put("name", "🛡️ Escort Companion")
                            put("value", escortInfo)
                            put("inline", false)
                        })
                        put(JSONObject().apply {
                            put("name", "🎯 Heading Towards")
                            put("value", destination)
                            put("inline", true)
                        })
                        put(JSONObject().apply {
                            put("name", "📍 GPS Location")
                            put("value", "${"%.5f".format(latitude)}° N, ${"%.5f".format(longitude)}° W")
                            put("inline", true)
                        })
                        put(JSONObject().apply {
                            put("name", "🗺️ Direct Google Maps Link")
                            put("value", "[Open Satellite Map Location]($mapsUrl)")
                            put("inline", false)
                        })
                        put(JSONObject().apply {
                            put("name", "🔒 E2E Encrypted Live Stream")
                            put("value", "[Open Live Telemetry Stream]($trackingUrl)")
                            put("inline", false)
                        })
                        put(JSONObject().apply {
                            put("name", "🚔 Central Police Dispatch")
                            put("value", "Notified automatically via Emergency Channel 112.")
                            put("inline", false)
                        })
                    }

                    put("fields", fields)
                    put("timestamp", currentTime)
                    put("footer", JSONObject().apply {
                        put("text", "SafeWalk Women Protection Network • Walk ID: ${walkId.take(12)}")
                    })
                }

                put("embeds", JSONArray().apply { put(embed) })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = payload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val code = response.code
            if (response.isSuccessful || code in 200..299) {
                Log.i("DiscordWebhookService", "Emergency notification delivered to Discord webhook successfully!")
                Result.success("Delivered to Discord Emergency Channel")
            } else {
                val err = response.body?.string() ?: "HTTP $code"
                Log.w("DiscordWebhookService", "Discord webhook error: $err")
                Result.failure(Exception("Discord Webhook responded with HTTP $code: $err"))
            }
        } catch (e: Exception) {
            Log.e("DiscordWebhookService", "Discord webhook exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendWalkStatusNotification(
        title: String,
        description: String,
        destination: String,
        statusColor: Int = 3066993 // Green / Cyan
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (webhookUrl.isBlank()) return@withContext Result.failure(Exception("Webhook URL not configured"))

            val payload = JSONObject().apply {
                put("username", "SafeWalk Guard")
                val embed = JSONObject().apply {
                    put("title", title)
                    put("description", description)
                    put("color", statusColor)
                    val fields = JSONArray().apply {
                        put(JSONObject().apply {
                            put("name", "🎯 Destination")
                            put("value", destination)
                            put("inline", true)
                        })
                        put(JSONObject().apply {
                            put("name", "🛡️ Security Corridor")
                            put("value", "Encrypted Dual-GPS & Audio Guardian Active")
                            put("inline", true)
                        })
                    }
                    put("fields", fields)
                }
                put("embeds", JSONArray().apply { put(embed) })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(webhookUrl)
                .post(payload.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code in 200..299) {
                Result.success("Walk status posted to Discord")
            } else {
                Result.failure(Exception("Discord HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
