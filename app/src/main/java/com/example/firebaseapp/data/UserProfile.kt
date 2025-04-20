package com.example.firebaseapp.data

data class UserProfile(
    val email: String = "",
    val nickname: String = "",
    val isActive: Boolean = true,
    val centerId: String = ""
)
