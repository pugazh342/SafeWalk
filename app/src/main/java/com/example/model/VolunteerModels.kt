package com.example.model

enum class AppMode {
    USER_WALKER,
    VOLUNTEER
}

data class Volunteer(
    val id: String,
    val name: String,
    val badgeNumber: String,
    val phone: String,
    val rating: Float,
    val completedEscorts: Int,
    val isVerified: Boolean = true,
    val distanceMeters: Int,
    val verificationTag: String,
    val estimatedArrivalMin: Int,
    val avatarInitials: String
)

data class WalkRequest(
    val id: String,
    val walkerName: String,
    val pickupLocation: String,
    val destination: String,
    val distanceMeters: Int,
    val estimatedWalkMin: Int,
    val urgencyLevel: UrgencyLevel,
    val timestamp: Long = System.currentTimeMillis(),
    val walkerPhone: String,
    val status: RequestStatus = RequestStatus.PENDING
)

enum class UrgencyLevel {
    HIGH_UNLIT_AREA,
    MODERATE_NIGHT_WALK,
    STANDARD_TRANSIT
}

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    SAFELY_COMPLETED
}
