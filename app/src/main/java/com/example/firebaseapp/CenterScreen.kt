package com.example.firebaseapp

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.firebaseapp.data.Task
import com.example.firebaseapp.data.UserProfile
import com.example.firebaseapp.data.VolunteerCenter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun CenterScreen() {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var center by remember { mutableStateOf<VolunteerCenter?>(null) }
    var usersCount by remember { mutableStateOf(0) }
    var usersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    var isUserListVisible by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }
    var showAddTask by remember { mutableStateOf(false) }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        user?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { userDoc ->
                    val centerId = userDoc.getString("centerId")
                    if (!centerId.isNullOrEmpty()) {
                        firestore.collection("centers").document(centerId)
                            .get()
                            .addOnSuccessListener { centerDoc ->
                                center = centerDoc.toObject(VolunteerCenter::class.java)

                                firestore.collection("users")
                                    .whereEqualTo("centerId", centerId)
                                    .addSnapshotListener { snapshot, _ ->
                                        if (snapshot != null) {
                                            usersList = snapshot.documents.mapNotNull {
                                                it.toObject(UserProfile::class.java)
                                            }
                                            usersCount = usersList.size
                                        }
                                    }

                                firestore.collection("centers").document(centerId)
                                    .collection("tasks")
                                    .addSnapshotListener { snapshot, _ ->
                                        if (snapshot != null) {
                                            tasks = snapshot.documents.mapNotNull {
                                                it.toObject(Task::class.java)
                                            }
                                        }
                                    }
                            }
                    }
                }
        }
    }

    if (center == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Центр не выбран или не найден")
        }
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Название: ${center!!.name}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Дата основания: ${center!!.foundationDate}")
                    Spacer(Modifier.height(8.dp))
                    Text("Описание: ${center!!.description}")
                    Spacer(Modifier.height(8.dp))
                    Text("Количество пользователей: $usersCount")

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { isUserListVisible = !isUserListVisible }) {
                        Text(if (isUserListVisible) "Скрыть пользователей" else "Показать список пользователей")
                    }

                    if (isUserListVisible) {
                        Spacer(Modifier.height(16.dp))
                        if (usersList.isEmpty()) {
                            Text("Нет пользователей.")
                        } else {
                            usersList.forEach { user ->
                                Text("Почта: ${user.email}, Ник: ${user.nickname}, Статус: ${if (user.isActive) "Активный" else "Неактивный"}")
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row {
                        Button(onClick = { showCompleted = false }) {
                            Text("Показать задачи")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { showCompleted = true }) {
                            Text("Выполненные")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    val filteredTasks = tasks.filter { it.isCompleted == showCompleted }
                    if (filteredTasks.isEmpty()) {
                        Text("Задач пока нет.")
                    } else {
                        filteredTasks.forEach { task ->
                            TaskCard(centerId = center!!.id, task = task, firestore = firestore, showCompleted = showCompleted)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { showAddTask = !showAddTask }) {
                        Text(if (showAddTask) "Скрыть форму" else "Добавить задачу")
                    }

                    if (showAddTask) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Введите название") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTaskDescription,
                            onValueChange = { newTaskDescription = it },
                            label = { Text("Введите описание") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            if (newTaskTitle.isNotBlank() && newTaskDescription.isNotBlank()) {
                                val newTask = Task(
                                    id = UUID.randomUUID().toString(),
                                    title = newTaskTitle,
                                    description = newTaskDescription,
                                    isCompleted = false,
                                    solution = ""
                                )
                                firestore.collection("centers").document(center!!.id)
                                    .collection("tasks").document(newTask.id)
                                    .set(newTask)

                                newTaskTitle = ""
                                newTaskDescription = ""
                                showAddTask = false
                            }
                        }) {
                            Text("Опубликовать")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(centerId: String, task: Task, firestore: FirebaseFirestore, showCompleted: Boolean) {
    var solutionText by remember { mutableStateOf("") }
    var showSolutionField by remember { mutableStateOf(false) }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Название: ${task.title}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Описание: ${task.description}")

            // Если задача выполнена, показываем решение и скрываем задачу из текущих
            if (task.isCompleted) {
                Spacer(Modifier.height(8.dp))
                Text("Решение: ${task.solution}")
            } else {
                Spacer(Modifier.height(8.dp))
                if (showSolutionField) {
                    OutlinedTextField(
                        value = solutionText,
                        onValueChange = { solutionText = it },
                        label = { Text("Введите решение") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            if (solutionText.isNotBlank()) {
                                val updatedTask = task.copy(
                                    isCompleted = true,
                                    solution = solutionText
                                )
                                firestore.collection("centers").document(centerId)
                                    .collection("tasks").document(task.id)
                                    .set(updatedTask)
                                    .addOnSuccessListener {
                                        showSolutionField = false
                                        solutionText = ""
                                    }
                            }
                        }) {
                            Text("Отправить")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            // Отменяем ввод
                            showSolutionField = false
                            solutionText = ""
                        }) {
                            Text("Отмена")
                        }
                    }
                } else {
                    Button(onClick = {
                        showSolutionField = true
                    }) {
                        Text("Сделано")
                    }
                }
            }
        }
    }
}
