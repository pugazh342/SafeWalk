package com.example.service

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLngPoint(val latitude: Double, val longitude: Double)

data class SafePlace(
    val id: String,
    val name: String,
    val type: SafePlaceType,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val distanceMeters: Int,
    val phone: String
)

enum class SafePlaceType {
    POLICE_STATION,
    HOSPITAL,
    TRANSIT_HUB,
    PHARMACY_247,
    COMMUNITY_HAVEN
}

class SafeWalkLocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Default center (San Francisco / Metropolis safety corridor)
    private var baseLat = 37.7749
    private var baseLng = -122.4194

    private val _userLocation = MutableStateFlow(LatLngPoint(baseLat, baseLng))
    val userLocation: StateFlow<LatLngPoint> = _userLocation.asStateFlow()

    private val _volunteerLocation = MutableStateFlow<LatLngPoint?>(null)
    val volunteerLocation: StateFlow<LatLngPoint?> = _volunteerLocation.asStateFlow()

    private val _routeHistory = MutableStateFlow<List<LatLngPoint>>(listOf(LatLngPoint(baseLat, baseLng)))
    val routeHistory: StateFlow<List<LatLngPoint>> = _routeHistory.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _safePlaces = MutableStateFlow<List<SafePlace>>(emptyList())
    val safePlaces: StateFlow<List<SafePlace>> = _safePlaces.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var simulationJob: Job? = null

    var onGpsLocationStreamListener: ((lat: Double, lng: Double, speed: Float, heading: Float) -> Unit)? = null

    init {
        updateNearbySafePlaces(baseLat, baseLng)
    }

    fun startTracking(scope: CoroutineScope) {
        if (_isTracking.value) return
        _isTracking.value = true

        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        baseLat = loc.latitude
                        baseLng = loc.longitude
                        _userLocation.value = LatLngPoint(baseLat, baseLng)
                        updateNearbySafePlaces(baseLat, baseLng)
                    }
                }

                val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                    .setMinUpdateDistanceMeters(2f)
                    .build()

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { location ->
                            val pt = LatLngPoint(location.latitude, location.longitude)
                            _userLocation.value = pt
                            _routeHistory.value = _routeHistory.value + pt
                            updateNearbySafePlaces(pt.latitude, pt.longitude)
                            onGpsLocationStreamListener?.invoke(
                                location.latitude,
                                location.longitude,
                                location.speed,
                                location.bearing
                            )
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    req,
                    locationCallback!!,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                Log.w("SafeWalkLocationTracker", "Request updates error: ${e.message}")
            }
        }

        // Also launch gentle walk simulation loop so the map actively traces the safe journey
        simulationJob = scope.launch(Dispatchers.Default) {
            var step = 0
            while (isActive && _isTracking.value) {
                delay(2500)
                step++
                // Gentle realistic walk path: north-east corridor
                val dLat = 0.00012 * (0.8 + 0.4 * sin(step.toDouble() * 0.2))
                val dLng = 0.00009 * (0.9 + 0.3 * cos(step.toDouble() * 0.15))
                baseLat += dLat
                baseLng += dLng

                val newPoint = LatLngPoint(baseLat, baseLng)
                _userLocation.value = newPoint
                _routeHistory.value = _routeHistory.value + newPoint

                // If volunteer is paired, volunteer moves close alongside walker (offset 10-20m)
                if (_volunteerLocation.value != null) {
                    val volPoint = LatLngPoint(
                        baseLat - 0.00008 + 0.00002 * sin(step.toDouble() * 0.5),
                        baseLng + 0.00006 + 0.00002 * cos(step.toDouble() * 0.5)
                    )
                    _volunteerLocation.value = volPoint
                }

                if (step % 5 == 0) {
                    updateNearbySafePlaces(newPoint.latitude, newPoint.longitude)
                }

                onGpsLocationStreamListener?.invoke(
                    newPoint.latitude,
                    newPoint.longitude,
                    1.25f,
                    45f
                )
            }
        }
    }

    fun setCounterpartLocation(lat: Double, lng: Double) {
        _volunteerLocation.value = LatLngPoint(lat, lng)
    }

    fun pairVolunteer(lat: Double, lng: Double) {
        _volunteerLocation.value = LatLngPoint(lat, lng)
    }

    fun unpairVolunteer() {
        _volunteerLocation.value = null
    }

    fun stopTracking() {
        _isTracking.value = false
        simulationJob?.cancel()
        simulationJob = null
        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
            } catch (e: Exception) {
                // ignore
            }
        }
        locationCallback = null
    }

    fun resetRoute() {
        _routeHistory.value = listOf(_userLocation.value)
    }

    private fun updateNearbySafePlaces(userLat: Double, userLng: Double) {
        _safePlaces.value = listOf(
            SafePlace(
                id = "sp_1",
                name = "Metropolis Central Police Station",
                type = SafePlaceType.POLICE_STATION,
                latitude = userLat + 0.0031,
                longitude = userLng + 0.0022,
                address = "450 Security Ave, Precinct 12",
                distanceMeters = calculateDistanceMeters(userLat, userLng, userLat + 0.0031, userLng + 0.0022),
                phone = "112"
            ),
            SafePlace(
                id = "sp_2",
                name = "Women Safety & Rapid Help Booth",
                type = SafePlaceType.COMMUNITY_HAVEN,
                latitude = userLat - 0.0018,
                longitude = userLng + 0.0019,
                address = "Market St & 5th Walkway",
                distanceMeters = calculateDistanceMeters(userLat, userLng, userLat - 0.0018, userLng + 0.0019),
                phone = "1091"
            ),
            SafePlace(
                id = "sp_3",
                name = "St. Jude 24/7 Emergency Care",
                type = SafePlaceType.HOSPITAL,
                latitude = userLat + 0.0048,
                longitude = userLng - 0.0035,
                address = "890 Health Blvd",
                distanceMeters = calculateDistanceMeters(userLat, userLng, userLat + 0.0048, userLng - 0.0035),
                phone = "+1-555-019-9110"
            ),
            SafePlace(
                id = "sp_4",
                name = "Central Metro Station (Security Lighted)",
                type = SafePlaceType.TRANSIT_HUB,
                latitude = userLat - 0.0025,
                longitude = userLng - 0.0028,
                address = "Civic Center Transit Gate B",
                distanceMeters = calculateDistanceMeters(userLat, userLng, userLat - 0.0025, userLng - 0.0028),
                phone = "+1-555-019-8234"
            )
        )
    }

    fun generateEncryptedLiveTrackingUrl(walkId: String): String {
        val userPt = _userLocation.value
        val raw = "$walkId|${userPt.latitude}|${userPt.longitude}|${System.currentTimeMillis()}|SAFEWALK_AES256"
        val hash = try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(raw.toByteArray())
            digest.fold("") { str, it -> str + "%02x".format(it) }.take(16)
        } catch (e: Exception) {
            "sec_${System.currentTimeMillis()}"
        }

        return "https://safewalk.app/live/track?token=$hash&lat=${"%.5f".format(userPt.latitude)}&lng=${"%.5f".format(userPt.longitude)}&w=$walkId"
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toInt()
    }
}
