package com.example.firebaseapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.firebaseapp.data.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun CreateChatScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    var otherUserId by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = otherUserId,
            onValueChange = { otherUserId = it },
            label = { Text("ID другого пользователя") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (otherUserId.isNotBlank()) {
                firestore.collection("users")
                    .whereEqualTo("email", otherUserId)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val otherUserDoc = documents.documents[0]
                            val otherUserUid = otherUserDoc.id

                            val newChat = Chat(
                                id = UUID.randomUUID().toString(),
                                userIds = listOf(currentUser!!.uid, otherUserUid)
                            )
                            firestore.collection("chats").document(newChat.id)
                                .set(newChat)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Чат создан", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                        } else {
                            Toast.makeText(context, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }) {
            Text("Создать чат")
        }

    }

}
