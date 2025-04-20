package com.example.firebaseapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseapp.data.VolunteerCenter

@Composable
fun CenterScreen() {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var center by remember { mutableStateOf<VolunteerCenter?>(null) }
    var usersCount by remember { mutableStateOf(0) }
    var usersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isUserListVisible by remember { mutableStateOf(false) }

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

                                // Получаем количество пользователей, привязанных к центру
                                firestore.collection("users")
                                    .whereEqualTo("centerId", centerId)
                                    .get()
                                    .addOnSuccessListener { result ->
                                        usersCount = result.size()
                                    }

                                // Получаем список пользователей
                                firestore.collection("users")
                                    .whereEqualTo("centerId", centerId)
                                    .get()
                                    .addOnSuccessListener { result ->
                                        usersList = result.documents.mapNotNull { document ->
                                            document.toObject(UserProfile::class.java)
                                        }
                                    }
                            }
                    }
                }
        }
    }

    if (center == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Центр не выбран или не найден")
        }
    } else {
        Column(Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
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
                    Spacer(Modifier.height(8.dp))
                    Text("Количество пользователей: $usersCount")
                    Spacer(Modifier.height(8.dp))

                    // Кнопка для показа списка пользователей
                    Button(
                        onClick = { isUserListVisible = !isUserListVisible },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(if (isUserListVisible) "Скрыть список пользователей" else "Показать список пользователей")
                    }

                    // Отображаем список пользователей, если флаг isUserListVisible == true
                    if (isUserListVisible) {
                        Spacer(Modifier.height(16.dp))
                        Column {
                            usersList.forEach { user ->
                                Text("Почта: ${user.email}, Никнейм: ${user.nickname}")
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
