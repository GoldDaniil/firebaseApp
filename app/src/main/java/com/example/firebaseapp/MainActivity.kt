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
import com.example.firebaseapp.ui.screens.BottomNavigationBar
import com.example.firebaseapp.ui.screens.CenterScreen
import com.example.firebaseapp.ui.screens.ChatScreen
import com.example.firebaseapp.ui.screens.CreateChatScreen
import com.example.firebaseapp.ui.screens.LoginScreen
import com.example.firebaseapp.ui.screens.MapScreen
import com.example.firebaseapp.ui.screens.MessengerScreen
import com.example.firebaseapp.ui.screens.RegisterScreen
import com.example.firebaseapp.ui.screens.WelcomeScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            FirebaseAppTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val userState = remember { mutableStateOf(auth.currentUser) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener {
                        userState.value = it.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController, userState.value) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = if (userState.value == null) "login" else "welcome",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("welcome") { WelcomeScreen(navController) }
                        composable("center") { CenterScreen(navController = navController) }
                        composable ("map"){ MapScreen() }

                        // экраны мессенджера
                        composable("messenger") { MessengerScreen(navController) }
                        composable("create_chat") { CreateChatScreen(navController) }
                        composable(
                            "chat/{chatId}/{otherUserId}"
                        ) { backStackEntry ->
                            val chatId = backStackEntry.arguments?.getString("chatId")!!
                            val otherUserId = backStackEntry.arguments?.getString("otherUserId")!!
                            ChatScreen(chatId, otherUserId)
                        }
                    }
                }
            }
        }
    }
}
