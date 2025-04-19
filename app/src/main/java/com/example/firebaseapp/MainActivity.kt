package com.example.firebaseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.firebaseapp.ui.theme.FirebaseAppTheme
import com.google.firebase.FirebaseApp
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            FirebaseAppTheme {
                val navController = rememberNavController()
                val user = FirebaseAuth.getInstance().currentUser

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController, user) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = if (user == null) "login" else "welcome",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("welcome") { WelcomeScreen(navController) }
                        composable("center") { CenterScreen() }
                        composable("messenger") { MessengerScreen() }
                    }
                }
            }
        }
    }
}
