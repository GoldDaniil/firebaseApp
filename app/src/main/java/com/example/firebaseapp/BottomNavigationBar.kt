package com.example.firebaseapp

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Message
import com.google.firebase.auth.FirebaseUser
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Article

@Composable
fun BottomNavigationBar(navController: NavController, user: FirebaseUser?) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        if (user == null) {
            NavigationBarItem(
                label = { Text("Вход") },
                selected = currentDestination == "login",
                onClick = { navController.navigate("login") },
                icon = { Icon(Icons.Default.Person, contentDescription = "Login") }
            )
            NavigationBarItem(
                label = { Text("Регистрация") },
                selected = currentDestination == "register",
                onClick = { navController.navigate("register") },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Register") }
            )
        } else {
            NavigationBarItem(
                label = { Text("Центр") },
                selected = currentDestination == "center",
                onClick = { navController.navigate("center") },
                icon = { Icon(Icons.Default.Home, contentDescription = "Center") }
            )
            NavigationBarItem(
                label = { Text("Мессенджер") },
                selected = currentDestination == "messenger",
                onClick = { navController.navigate("messenger") },
                icon = { Icon(Icons.Default.Message, contentDescription = "Messenger") }
            )
            NavigationBarItem(
                label = { Text("Профиль") },
                selected = currentDestination == "welcome",
                onClick = { navController.navigate("welcome") },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
            )
            NavigationBarItem(
                label = { Text("Новости") },
                selected = currentDestination == "news",
                onClick = { navController.navigate("news") },
                icon = { Icon(Icons.Default.Article, contentDescription = "Новости") }
            )

        }
    }
}
