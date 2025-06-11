package com.example.firebaseapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.firebaseapp.data.Chat
import com.example.firebaseapp.data.ChatWithEmail
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var chats by remember { mutableStateOf<List<ChatWithEmail>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.let { user ->
            firestore.collection("chats")
                .whereArrayContains("userIds", user.uid)
                .addSnapshotListener { snapshot, _ ->
                    val chatDocuments = snapshot?.documents ?: emptyList()

                    chatDocuments.mapNotNull { it.toObject(Chat::class.java) }
                        .let { loadedChats ->
                            //для каждого чата — найти email другого пользователя
                            val chatTasks = loadedChats.map { chat ->
                                val otherUserId = chat.userIds.firstOrNull { it != user.uid }
                                if (otherUserId != null) {
                                    firestore.collection("users")
                                        .document(otherUserId)
                                        .get()
                                        .continueWith { task ->
                                            val email = task.result?.getString("email") ?: "Неизвестный"
                                            ChatWithEmail(chat, email)
                                        }
                                } else {
                                    null
                                }
                            }.filterNotNull()

                            //когда все задачи завершатся
                            Tasks.whenAllSuccess<ChatWithEmail>(chatTasks)
                                .addOnSuccessListener { result ->
                                    chats = result
                                }
                        }
                }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мессенджер") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create_chat") }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
        ) {
            if (chats.isEmpty()) {
                Text("Нет чатов", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn {
                    items(chats) { chatWithEmail ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val otherUserId = chatWithEmail.chat.userIds.first { it != currentUser!!.uid }
//                                    navController.navigate("chat/${chatWithEmail.chat.id}/$otherUserId")
                                    navController.navigate("chat/${chatWithEmail.chat.id}/${chatWithEmail.otherUserEmail}")

                                },
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = "Чат с: ${chatWithEmail.otherUserEmail}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
