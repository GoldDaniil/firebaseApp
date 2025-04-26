package com.example.firebaseapp

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.let { user ->
            firestore.collection("chats")
                .whereArrayContains("userIds", user.uid)
                .addSnapshotListener { snapshot, _ ->
                    chats = snapshot?.documents?.mapNotNull { it.toObject(Chat::class.java) } ?: emptyList()
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
                    items(chats) { chat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val otherUserId = chat.userIds.first { it != currentUser!!.uid }
                                    navController.navigate("chat/${chat.id}/$otherUserId")
                                },
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = "Чат с: ${chat.userIds.first { it != currentUser!!.uid }}",
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
