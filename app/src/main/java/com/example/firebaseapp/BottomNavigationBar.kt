package com.example.firebaseapp

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person


@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
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
        NavigationBarItem(
            label = { Text("Профиль") },
            selected = currentDestination == "welcome",
            onClick = { navController.navigate("welcome") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Welcome") }
        )
    }
}
