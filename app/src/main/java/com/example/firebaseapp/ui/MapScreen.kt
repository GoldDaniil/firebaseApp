package com.example.firebaseapp.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.firebaseapp.data.VolunteerCenter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(55.7558, 37.6173), 10f)
    }
    var selectedCenter by remember { mutableStateOf<VolunteerCenter?>(null) }
    val db = FirebaseFirestore.getInstance()
    val centers = remember { mutableStateListOf<VolunteerCenter>() }

    LaunchedEffect(Unit) {
        db.collection("centers")
            .get()
            .addOnSuccessListener { result ->
                centers.clear()
                for (document in result) {
                    val center = document.toObject(VolunteerCenter::class.java)
                    centers.add(center)
                }
            }
            .addOnFailureListener {
                Log.d("DDD","Центры не найдены")
            }
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        centers.forEach { center ->
            Marker(
                state = MarkerState(position = center.toLatLng()),
                title = center.name,
                onClick = {
                    selectedCenter = center
                    true
                }
            )
        }
    }

    selectedCenter?.let { center ->
        AlertDialog(
            onDismissRequest = { selectedCenter = null },
            title = { Text(center.name) },
            text = {
                Text(
                    """
                    📍 Адрес: ${center.address}
                    🚇 Метро: ${center.metro}
                    🕐 График: ${center.schedule}
                    """.trimIndent()
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedCenter = null }) {
                    Text("ОК")
                }
            }
        )
    }
}
