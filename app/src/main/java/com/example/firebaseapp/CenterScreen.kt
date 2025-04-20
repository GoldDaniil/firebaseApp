package com.example.firebaseapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseapp.data.VolunteerCenter

@Composable
fun CenterScreen() {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var center by remember { mutableStateOf<VolunteerCenter?>(null) }

    LaunchedEffect(Unit) {
        if (user != null) {
            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { userDoc ->
                    val centerId = userDoc.getString("centerId")
                    if (!centerId.isNullOrEmpty()) {
                        firestore.collection("centers").document(centerId)
                            .get()
                            .addOnSuccessListener { centerDoc ->
                                center = centerDoc.toObject(VolunteerCenter::class.java)
                            }
                    }
                }
        }
    }

    if (center == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Центр не выбран или не найден")
        }
    } else {
        Column(Modifier.padding(16.dp)) {
            Text("Название: ${center!!.name}", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Дата основания: ${center!!.foundationDate}")
            Spacer(Modifier.height(8.dp))
            Text("Описание: ${center!!.description}")
            Spacer(Modifier.height(8.dp))
            Text("Цели: ${center!!.goals}")
            Spacer(Modifier.height(8.dp))
            Text("Поддержка: ${center!!.supporters}")
        }
    }
}
