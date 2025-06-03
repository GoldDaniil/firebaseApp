package com.example.firebaseapp.data

import com.google.android.gms.maps.model.LatLng

data class VolunteerCenter(
    val id: String = "",
    val name: String = "",
    val foundationDate: String = "",
    val description: String = "",
    val goals: String = "",
    val supporters: String = "",
    val address: String = "",
    val metro: String = "",
    val schedule: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
){
    fun toLatLng() = LatLng(latitude, longitude)
}
