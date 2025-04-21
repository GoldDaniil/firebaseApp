package com.example.firebaseapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.example.firebaseapp.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun ChatScreen(chatId: String, otherUserId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current // Сохраняем контекст один раз
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(chatId) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                messages = snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) } ?: emptyList()
            }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Чат с пользователем: $otherUserId", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                Text(
                    "${if (message.senderId == currentUser!!.uid) "Вы" else otherUserId}: ${message.message}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text("Сообщение") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            if (messageText.isNotBlank()) {
                val newMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderId = currentUser!!.uid,
                    message = messageText
                )
                firestore.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document(newMessage.id)
                    .set(newMessage)
                    .addOnSuccessListener {
                        messageText = ""
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Ошибка отправки", Toast.LENGTH_SHORT).show()
                    }
            }
        }) {
            Text("Отправить")
        }
    }
}
