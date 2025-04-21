package com.example.firebaseapp.data

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val solution: String = "",
    val isSolutionSent: Boolean = false
)
