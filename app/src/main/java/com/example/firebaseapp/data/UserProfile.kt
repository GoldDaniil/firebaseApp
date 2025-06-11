package com.example.firebaseapp.data

import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val email: String = "",
    val nickname: String = "",
    val isActive: Boolean ,
    val centerId: String = "",
    val bio: String =""
)
