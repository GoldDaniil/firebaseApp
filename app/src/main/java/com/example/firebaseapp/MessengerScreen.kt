package com.example.firebaseapp

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.firebaseapp.data.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MessengerScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }

    LaunchedEffect(Unit) {
        firestore.collection("chats")
            .whereArrayContains("userIds", currentUser!!.uid)
            .addSnapshotListener { snapshot, _ ->
                chats = snapshot?.documents?.mapNotNull { it.toObject(Chat::class.java) } ?: emptyList()
            }
    }

    Column(Modifier.padding(16.dp)) {
        Button(onClick = { navController.navigate("create_chat") }) {
            Text("Создать чат")
        }

        Spacer(Modifier.height(16.dp))

        if (chats.isEmpty()) {
            Text("Нет чатов")
        } else {
            LazyColumn {
                items(chats) { chat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                // определяем собеседника
                                val otherUserId = chat.userIds.first { it != currentUser!!.uid }
                                navController.navigate("chat/${chat.id}/$otherUserId")
                            }
                    ) {
                        Text(
                            "Чат с пользователем: ${chat.userIds.first { it != currentUser!!.uid }}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
