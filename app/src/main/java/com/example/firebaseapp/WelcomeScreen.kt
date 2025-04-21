package com.example.firebaseapp

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseapp.data.VolunteerCenter
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

data class UserProfile(
    val email: String = "",
    val nickname: String = "Гость",
    val bio: String = "Привет! Я только что зарегистрировался.",
    val status: String = "Активный",
    val centerId: String = "",
    val avatarRes: String = "black"
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
    var selectedAvatar by remember { mutableStateOf("black") }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        firestore.collection("centers")
            .get()
            .addOnSuccessListener { result ->
                centers = result.documents.mapNotNull { it.toObject(VolunteerCenter::class.java) }

                firestore.collection("users").document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            profile = document.toObject(UserProfile::class.java)
                            selectedCenter = centers.find { it.id == profile?.centerId }
                            selectedAvatar = profile?.avatarRes ?: "black"
                        } else {
                            val newProfile = UserProfile(email = user.email ?: "")
                            firestore.collection("users").document(user.uid).set(newProfile)
                            profile = newProfile
                        }
                    }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = {
                            isEditing = true
                            editableNickname = profile!!.nickname
                            editableBio = profile!!.bio
                            editableStatus = profile!!.status
                            selectedAvatar = profile!!.avatarRes
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
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
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            profile?.let {
                val avatarResId = when (it.avatarRes) {
                    "black" -> R.drawable.black
                    "blue" -> R.drawable.blue
                    "reg" -> R.drawable.reg
                    "yellowjpg" -> R.drawable.yellowjpg
                    else -> R.drawable.black
                }

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }

                Spacer(Modifier.height(16.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Почта", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(profile!!.email, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editableNickname,
                    onValueChange = { editableNickname = it },
                    label = { Text("Никнейм") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editableBio,
                    onValueChange = { editableBio = it },
                    label = { Text("О себе") },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                val statusOptions = listOf("Активный", "Неактивный")

                Text("Статус", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = editableStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Статус") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statusOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    editableStatus = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Выберите волонтёрский центр:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                centers.forEach { center ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
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
                Text("Выберите аватар:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("black", "blue", "reg", "yellowjpg").forEach { avatarName ->
                        val resId = when (avatarName) {
                            "black" -> R.drawable.black
                            "blue" -> R.drawable.blue
                            "reg" -> R.drawable.reg
                            "yellowjpg" -> R.drawable.yellowjpg
                            else -> R.drawable.black
                        }

                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = avatarName,
                            modifier = Modifier
                                .size(64.dp)
                                .border(
                                    2.dp,
                                    if (selectedAvatar == avatarName) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedAvatar = avatarName }
                        )
                    }
                }

                    Spacer(Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = {
                            val updatedProfile = profile!!.copy(
                                nickname = editableNickname,
                                bio = editableBio,
                                status = editableStatus,
                                centerId = selectedCenter?.id ?: "",
                                avatarRes = selectedAvatar
                            )
                            firestore.collection("users").document(user.uid).set(updatedProfile)
                            profile = updatedProfile
                            isEditing = false
                        }) {
                            Text("Сохранить")
                        }

                        OutlinedButton(onClick = { isEditing = false }) {
                            Text("Отмена")
                        }
                    }
                } else {
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Почта", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(it.email, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("Никнейм", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(it.nickname, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("О себе", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(it.bio, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("Статус", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(it.status, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("Центр", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(selectedCenter?.name ?: "Не выбран", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            navController.navigate("center")
                        }) {
                            Text("Перейти к центру")
                        }

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
    }
}