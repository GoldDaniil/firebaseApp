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
    var otherUserEmail by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = otherUserEmail,
            onValueChange = { otherUserEmail = it },
            label = { Text("Email другого пользователя") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (otherUserEmail.isNotBlank()) {
                firestore.collection("users")
                    .whereEqualTo("email", otherUserEmail)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val otherUserDoc = documents.documents[0]
                            val otherUserUid = otherUserDoc.id

                            val newChatId = UUID.randomUUID().toString()
                            val newChat = hashMapOf(
                                "id" to newChatId,
                                "userIds" to listOf(currentUser!!.uid, otherUserUid)
                            )
                            firestore.collection("chats")
                                .document(newChatId)
                                .set(newChat)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Чат создан", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Ошибка создания чата", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Ошибка поиска пользователя", Toast.LENGTH_SHORT).show()
                    }
            }
        }) {
            Text("Создать чат")
        }
    }
}
