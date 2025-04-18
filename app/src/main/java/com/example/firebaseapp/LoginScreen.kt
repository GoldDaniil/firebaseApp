package com.example.firebaseapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(Modifier.padding(16.dp)) {
        Text(text = "Вход", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") })
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@Button
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Ошибка входа", Toast.LENGTH_SHORT).show()
                    }
                }
        }) {
            Text("Войти")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Регистрация")
        }
    }
}
