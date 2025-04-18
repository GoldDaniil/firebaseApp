package com.example.firebaseapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier

@Composable
fun WelcomeScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    Column(Modifier.padding(16.dp)) {
        Text(text = "Добро пожаловать, ${user.email}!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
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
