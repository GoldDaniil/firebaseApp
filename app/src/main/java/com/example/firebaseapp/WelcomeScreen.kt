package com.example.firebaseapp

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseapp.data.VolunteerCenter

data class UserProfile(
    val email: String = "",
    val nickname: String = "Гость",
    val bio: String = "Привет! Я только что зарегистрировался.",
    val status: String = "Активный",
    val centerId: String = "" // добавляем привязку к центру
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var centers by remember { mutableStateOf<List<VolunteerCenter>>(emptyList()) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedCenter by remember { mutableStateOf<VolunteerCenter?>(null) }

    var editableNickname by remember { mutableStateOf("") }
    var editableBio by remember { mutableStateOf("") }
    var editableStatus by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        firestore.collection("centers")
            .get()
            .addOnSuccessListener { result ->
                centers = result.documents.mapNotNull { it.toObject(VolunteerCenter::class.java) }
            }
    }

    LaunchedEffect(user.uid) {
        firestore.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    profile = document.toObject(UserProfile::class.java)
                    selectedCenter = centers.find { it.id == profile?.centerId }
                } else {
                    val newProfile = UserProfile(email = user.email ?: "")
                    firestore.collection("users").document(user.uid).set(newProfile)
                    profile = newProfile
                }
            }
    }

    if (profile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = {
                            isEditing = true
                            editableNickname = profile!!.nickname
                            editableBio = profile!!.bio
                            editableStatus = profile!!.status
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Изменить")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Почта: ${profile!!.email}")
            Spacer(Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editableNickname,
                    onValueChange = { editableNickname = it },
                    label = { Text("Никнейм") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editableBio,
                    onValueChange = { editableBio = it },
                    label = { Text("О себе") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editableStatus,
                    onValueChange = { editableStatus = it },
                    label = { Text("Статус") }
                )
                Spacer(Modifier.height(16.dp))

                Text("Выбрать волонтёрский центр:")
                centers.forEach { center ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selectedCenter = center }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedCenter?.id == center.id,
                            onClick = { selectedCenter = center }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(center.name)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        val updatedProfile = profile!!.copy(
                            nickname = editableNickname,
                            bio = editableBio,
                            status = editableStatus,
                            centerId = selectedCenter?.id ?: ""
                        )
                        firestore.collection("users").document(user.uid).set(updatedProfile)
                        profile = updatedProfile
                        isEditing = false
                    }) {
                        Text("Сохранить")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { isEditing = false }) {
                        Text("Отмена")
                    }
                }

            } else {
                Text("Никнейм: ${profile!!.nickname}")
                Spacer(Modifier.height(8.dp))
                Text("О себе: ${profile!!.bio}")
                Spacer(Modifier.height(8.dp))
                Text("Статус: ${profile!!.status}")
                Spacer(Modifier.height(8.dp))
                Text("Центр: ${centers.find { it.id == profile!!.centerId }?.name ?: "Не выбран"}")
                Spacer(Modifier.height(16.dp))

                Button(onClick = {
                    navController.navigate("center")
                }) {
                    Text("Перейти к центру")
                }

                Spacer(Modifier.height(8.dp))

                Button(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Выйти")
                }
            }
        }
    }
}
