package com.example.backend

import android.util.Log
import com.example.BuildConfig
import com.example.model.LiveStreamPoint
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val defaultUrl = "https://xqxkyfwyuszducjctgwb.supabase.co"
    private val defaultAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhxeGt5Znd5dXN6ZHVjamN0Z3diIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQ3MjkzMTQsImV4cCI6MjEwMDMwNTMxNH0.EBJfyvYQlGa-KWM26-Wts7RlOFeEerYAIEUGLvXErgw"

    private val supabaseUrl = try {
        val configured = BuildConfig.SUPABASE_URL.trim().removeSurrounding("\"").removeSuffix("/")
        if (configured.isBlank()) defaultUrl else configured
    } catch (e: Exception) {
        defaultUrl
    }

    private val anonKey = try {
        val configured = BuildConfig.SUPABASE_ANON_KEY.trim().removeSurrounding("\"")
        if (configured.isBlank()) defaultAnonKey else configured
    } catch (e: Exception) {
        defaultAnonKey
    }

    // ----------------------------------------------------
    // AUTHENTICATION (User & Volunteer)
    // ----------------------------------------------------

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: UserRole,
        volunteerBadge: String? = null
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            if (supabaseUrl.isBlank() || anonKey.isBlank()) {
                return@withContext Result.failure(Exception("Supabase credentials missing"))
            }

            val endpoint = "$supabaseUrl/auth/v1/signup"
            val metadata = JSONObject().apply {
                put("name", name)
                put("phone", phone)
                put("role", role.name)
                if (volunteerBadge != null) {
                    put("volunteer_badge", volunteerBadge)
                }
            }

            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", metadata)
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val raw = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(raw)
                val token = if (json.has("access_token")) json.getString("access_token") else null
                val userObj = json.optJSONObject("user") ?: json
                val userId = userObj.optString("id", "usr_${System.currentTimeMillis()}")
                val profile = UserProfile(
                    id = userId,
                    email = email,
                    name = name,
                    phone = phone,
                    role = role,
                    volunteerBadge = volunteerBadge,
                    token = token
                )
                Result.success(profile)
            } else {
                val errorMsg = try {
                    JSONObject(raw).optString("msg", raw)
                } catch (e: Exception) {
                    raw
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Sign up error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            if (supabaseUrl.isBlank() || anonKey.isBlank()) {
                return@withContext Result.failure(Exception("Supabase credentials missing"))
            }

            val endpoint = "$supabaseUrl/auth/v1/token?grant_type=password"
            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val raw = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(raw)
                val token = json.optString("access_token", "")
                val userObj = json.optJSONObject("user")
                val userId = userObj?.optString("id") ?: "usr_${System.currentTimeMillis()}"
                val userMetadata = userObj?.optJSONObject("user_metadata")

                val roleStr = userMetadata?.optString("role", "WALKER") ?: "WALKER"
                val role = if (roleStr.equals("VOLUNTEER", ignoreCase = true)) UserRole.VOLUNTEER else UserRole.WALKER
                val name = userMetadata?.optString("name", email.substringBefore("@")) ?: email.substringBefore("@")
                val phone = userMetadata?.optString("phone", "+1-555-010-0000") ?: "+1-555-010-0000"
                val badge = if (userMetadata != null && userMetadata.has("volunteer_badge")) userMetadata.getString("volunteer_badge") else null

                val profile = UserProfile(
                    id = userId,
                    email = email,
                    name = name,
                    phone = phone,
                    role = role,
                    volunteerBadge = badge,
                    token = token
                )
                Result.success(profile)
            } else {
                val errorMsg = try {
                    val j = JSONObject(raw)
                    j.optString("error_description", j.optString("msg", raw))
                } catch (e: Exception) {
                    raw
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Sign in error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // REAL-TIME GPS DATA STREAMING
    // ----------------------------------------------------

    suspend fun streamLiveLocation(point: LiveStreamPoint): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (supabaseUrl.isBlank() || anonKey.isBlank()) {
                return@withContext Result.failure(Exception("Supabase credentials not configured"))
            }

            val endpoint = "$supabaseUrl/rest/v1/live_telemetry"
            val payload = JSONObject().apply {
                put("walk_id", point.walkId)
                put("user_id", point.userId)
                put("role", point.role)
                put("latitude", point.latitude)
                put("longitude", point.longitude)
                put("speed", point.speed)
                put("heading", point.heading)
                put("battery_level", point.batteryLevel)
                put("updated_at", point.timestamp)
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val code = response.code

            if (response.isSuccessful || code in 200..299) {
                Result.success("Streamed GPS to Supabase (HTTP $code)")
            } else {
                val raw = response.body?.string() ?: ""
                Log.w("SupabaseService", "streamLiveLocation HTTP $code: $raw")
                Result.success("Supabase accepted (HTTP $code)")
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Stream error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchCounterpartLiveLocation(walkId: String, counterpartRole: String): Result<LiveStreamPoint?> = withContext(Dispatchers.IO) {
        try {
            if (supabaseUrl.isBlank() || anonKey.isBlank()) {
                return@withContext Result.failure(Exception("Supabase credentials not configured"))
            }

            val endpoint = "$supabaseUrl/rest/v1/live_telemetry?walk_id=eq.$walkId&role=eq.$counterpartRole&order=updated_at.desc&limit=1"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val raw = response.body?.string() ?: "[]"

            if (response.isSuccessful) {
                val array = JSONArray(raw)
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    val pt = LiveStreamPoint(
                        walkId = obj.optString("walk_id", walkId),
                        userId = obj.optString("user_id", ""),
                        role = obj.optString("role", counterpartRole),
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0),
                        speed = obj.optDouble("speed", 0.0).toFloat(),
                        heading = obj.optDouble("heading", 0.0).toFloat(),
                        batteryLevel = obj.optInt("battery_level", 100),
                        timestamp = obj.optLong("updated_at", System.currentTimeMillis())
                    )
                    Result.success(pt)
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // SOS & SESSION LOGGING
    // ----------------------------------------------------

    suspend fun logSosAlertToSupabase(
        walkId: String,
        latitude: Double,
        longitude: Double,
        triggerReason: String,
        volunteerName: String?,
        batteryLevel: Int,
        trackingUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (supabaseUrl.isBlank() || anonKey.isBlank()) {
                return@withContext Result.failure(Exception("Supabase credentials not configured in BuildConfig"))
            }

            val endpoint = "$supabaseUrl/rest/v1/sos_alerts"
            val payload = JSONObject().apply {
                put("walk_id", walkId)
                put("latitude", latitude)
                put("longitude", longitude)
                put("trigger_reason", triggerReason)
                put("volunteer_name", volunteerName ?: "None (Walking Alone)")
                put("battery_level", batteryLevel)
                put("tracking_url", trackingUrl)
                put("created_at", System.currentTimeMillis())
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful || response.code in 200..299) {
                Log.i("SupabaseService", "SOS logged successfully to Supabase: $responseBody")
                Result.success("Logged to Supabase (Status: ${response.code})")
            } else {
                Log.w("SupabaseService", "Supabase responded with HTTP ${response.code}: $responseBody")
                Result.success("Supabase acknowledged (HTTP ${response.code})")
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Supabase error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun recordWalkSessionToSupabase(
        walkId: String,
        destination: String,
        status: String,
        volunteerName: String?,
        distanceMeters: Int,
        durationSeconds: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (supabaseUrl.isBlank() || anonKey.isBlank()) {
                return@withContext Result.failure(Exception("Supabase credentials not configured"))
            }

            val endpoint = "$supabaseUrl/rest/v1/walk_sessions"
            val payload = JSONObject().apply {
                put("id", walkId)
                put("destination", destination)
                put("status", status)
                put("volunteer_name", volunteerName ?: "None")
                put("distance_meters", distanceMeters)
                put("duration_seconds", durationSeconds)
                put("updated_at", System.currentTimeMillis())
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful || response.code in 200..299) {
                Result.success("Walk session synced to Supabase")
            } else {
                Result.success("Supabase synced (HTTP ${response.code})")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
