package com.example.firebaseapp

import android.util.Log
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

data class UserProfile(
    val email: String = "",
    val nickname: String = "Гость",
    val bio: String = "Привет! Я только что зарегистрировался.",
    val status: String = "Активный"
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
    var isEditing by remember { mutableStateOf(false) }

    var editableNickname by remember { mutableStateOf("") }
    var editableBio by remember { mutableStateOf("") }
    var editableStatus by remember { mutableStateOf("") }

    // Загрузка профиля
    LaunchedEffect(user.uid) {
        firestore.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    profile = document.toObject(UserProfile::class.java)
                } else {
                    // Если нет, создаём дефолтный
                    val newProfile = UserProfile(email = user.email ?: "")
                    firestore.collection("users").document(user.uid).set(newProfile)
                    profile = newProfile
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Ошибка загрузки профиля", e)
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
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Почта: ${profile!!.email}", style = MaterialTheme.typography.bodyLarge)
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

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = editableStatus,
                        onValueChange = {},
                        label = { Text("Статус") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Активный", "Неактивный").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    editableStatus = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        val updatedProfile = UserProfile(
                            email = profile!!.email, // неизменно
                            nickname = editableNickname,
                            bio = editableBio,
                            status = editableStatus
                        )
                        firestore.collection("users").document(user.uid).set(updatedProfile)
                        profile = updatedProfile
                        isEditing = false
                    }) {
                        Text("Сохранить")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = {
                        isEditing = false
                        // Восстанавливаем значения из оригинального профиля
                        editableNickname = profile!!.nickname
                        editableBio = profile!!.bio
                        editableStatus = profile!!.status
                    }) {
                        Text("Отмена")
                    }
                }

            } else {
                Text("Никнейм: ${profile!!.nickname}")
                Spacer(Modifier.height(8.dp))
                Text("О себе: ${profile!!.bio}")
                Spacer(Modifier.height(8.dp))
                Text("Статус: ${profile!!.status}")
                Spacer(Modifier.height(16.dp))
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
