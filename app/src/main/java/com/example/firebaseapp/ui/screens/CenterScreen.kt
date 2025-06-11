package com.example.firebaseapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.firebaseapp.data.Task
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseapp.ViewModel.CenterViewModel

@Composable
fun CenterScreen(navController: NavController) {

    val vm: CenterViewModel = viewModel()
    var center = vm.center.collectAsState()
    var usersCount = vm.usersCount.collectAsState()
    var usersList = vm.usersList.collectAsState()
    var tasks = vm.tasks.collectAsState()
    var completedTasks = vm.completedTasks.collectAsState()
    var isUserListVisible = vm.isUserListVisible.collectAsState()
    var showCompleted = vm.showCompleted.collectAsState()
    var showAddTask = vm.showAddTaskForm.collectAsState()
    var newTaskTitle = vm.newTaskTitle.collectAsState()
    var newTaskDescription = vm.newTaskDescription.collectAsState()
    val scrollState = rememberScrollState()

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
                    Text("Название: ${center.value?.name}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Дата основания: ${center.value?.foundationDate}")
                    Spacer(Modifier.height(8.dp))
                    Text("Описание: ${center.value?.description}")
                    Spacer(Modifier.height(8.dp))
                    Text("Количество пользователей: ${usersCount.value}")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { vm.changeUserList() }) {
                        Text(if (isUserListVisible.value) "Скрыть пользователей" else "Показать пользователей")
                    }
                    if (isUserListVisible.value) {
                        Spacer(Modifier.height(16.dp))
                        if (usersList.value.isEmpty()) {
                            Text("Нет пользователей.")
                        } else {
                            usersList.value.forEach { user ->
                                Text("Почта: ${user.email}, Ник: ${user.nickname}, Статус: ${if (user.isActive) "Активный" else "Неактивный"}")
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                    Button(onClick = { navController.navigate("map") }) {
                        Text("Карта 🗺️")
                    }
                    Spacer(Modifier.height(16.dp))
                    Row {
                        Button(onClick = { vm.setShowCompleted(false) }) {
                            Text("Задачи")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { vm.setShowCompleted(true) }) {
                            Text("Выполненные")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    val tasksToShow = if (showCompleted.value) completedTasks else tasks
                    if (tasksToShow.value.isEmpty()) {
                        Text("Задач пока нет.")
                    } else {
                        tasksToShow.value.forEach { task ->
                            TaskCard(
                                centerId = center.value?.id ?: "",
                                task = task,
                                showCompleted = showCompleted.value,
                                viewModel = vm
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { vm.changeAddTaskForm() }) {
                        Text(if (showAddTask.value) "Скрыть форму" else "Добавить задачу")
                    }
                    if (showAddTask.value) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newTaskTitle.value,
                            onValueChange = { vm.setTitle(it) },
                            label = { Text("Введите название") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTaskDescription.value,
                            onValueChange = { vm.set_new_description(it) },
                            label = { Text("Введите описание") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.publishNewTask(center.value?.id ?: "") }) {
                            Text("Опубликовать")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    centerId: String,
    task: Task,
    viewModel: CenterViewModel,
    showCompleted: Boolean,
) {
    var solutionText by remember { mutableStateOf("") }
    var isSolutionFieldVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📌 ${task.title}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                if (task.isSolutionSent && task.isCompleted) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(Color(0xFFFFA000), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Решение отправлено",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(task.description)
            when {
                task.isCompleted -> {
                    Spacer(Modifier.height(8.dp))
                    Text("Решение: ${task.solution}", fontWeight = FontWeight.SemiBold)
                    Text("✅ Выполнено", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
                !showCompleted -> {
                    Spacer(Modifier.height(8.dp))
                    if (task.isSolutionSent) {
                        Column {
                            Text(
                                "Ваше решение: ${task.solution}",
                                fontStyle = FontStyle.Italic,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.completeTask(centerId, task) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Подтвердить выполнение")
                            }
                        }
                    } else {
                        if (isSolutionFieldVisible) {
                            OutlinedTextField(
                                value = solutionText,
                                onValueChange = { solutionText = it },
                                label = { Text("Ваше решение") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                if (solutionText.isNotBlank()) {
                                    Toast.makeText(context, "Решение отправлено", Toast.LENGTH_SHORT).show()

                                    val updatedTask = task.copy(
                                        solution = solutionText,
                                        isSolutionSent = true
                                    )

                                    viewModel.sendTaskSolution(centerId, updatedTask, solutionText)
                                    isSolutionFieldVisible = false
                                }
                            }) {
                                Text("Отправить решение")
                            }
                        } else {
                            Button(onClick = { isSolutionFieldVisible = true }) {
                                Text("Пометить выполненным")
                            }
                        }
                    }
                }
            }
        }
    }
}