package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val isPolice: Boolean = false
)

@Entity(tableName = "walk_sessions")
data class WalkSession(
    @PrimaryKey
    val id: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val destination: String,
    val status: String, // ACTIVE, COMPLETED, SOS_DISPATCHED
    val volunteerName: String? = null,
    val volunteerPhone: String? = null,
    val volunteerRating: Float = 5.0f,
    val distanceMeters: Int = 0,
    val durationSeconds: Int = 0,
    val encryptedTrackingToken: String
)

@Entity(tableName = "sos_logs")
data class SosLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerReason: String,
    val latitude: Double,
    val longitude: Double,
    val dispatchedContactsCount: Int,
    val policeNotified: Boolean,
    val audioEvidenceSaved: Boolean
)
