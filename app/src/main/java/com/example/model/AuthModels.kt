package com.example.model

enum class UserRole {
    WALKER,
    VOLUNTEER
}

data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val role: UserRole,
    val volunteerBadge: String? = null,
    val volunteerRating: Float = 5.0f,
    val completedEscorts: Int = 0,
    val token: String? = null
)

data class LiveStreamPoint(
    val walkId: String,
    val userId: String,
    val role: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Float = 0f,
    val heading: Float = 0f,
    val batteryLevel: Int = 100,
    val timestamp: Long = System.currentTimeMillis()
)
