package com.example.firebaseapp.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseapp.data.Task
import com.example.firebaseapp.data.UserProfile
import com.example.firebaseapp.data.VolunteerCenter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*

class CenterViewModel : ViewModel() {

    private val _center = MutableStateFlow<VolunteerCenter?>(null)
    val center: StateFlow<VolunteerCenter?> = _center

    private val _usersCount = MutableStateFlow(0)
    val usersCount: StateFlow<Int> = _usersCount

    private val _usersList = MutableStateFlow<List<UserProfile>>(emptyList())
    val usersList: StateFlow<List<UserProfile>> = _usersList

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _completedTasks = MutableStateFlow<List<Task>>(emptyList())
    val completedTasks: StateFlow<List<Task>> = _completedTasks

    private val _showAddTaskForm = MutableStateFlow(false)
    val showAddTaskForm: StateFlow<Boolean> = _showAddTaskForm

    private val _newTaskTitle = MutableStateFlow("")
    val newTaskTitle: StateFlow<String> = _newTaskTitle

    private val _newTaskDescription = MutableStateFlow("")
    val newTaskDescription: StateFlow<String> = _newTaskDescription

    private val _isUserListVisible = MutableStateFlow(false)
    val isUserListVisible: StateFlow<Boolean> = _isUserListVisible

    private val _showCompleted = MutableStateFlow(false)
    val showCompleted: StateFlow<Boolean> = _showCompleted

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { userDoc ->
                val centerId = userDoc.getString("centerId")
                if (!centerId.isNullOrEmpty()) {
                    loadCenter(centerId)
                    loadUsers(centerId)
                    loadTasks(centerId)
                    loadCompletedTasks(centerId)
                }
            }
    }

    private fun loadCenter(centerId: String) {
        firestore.collection("centers").document(centerId)
            .get()
            .addOnSuccessListener { doc ->
                _center.value = doc.toObject(VolunteerCenter::class.java)
            }
    }

    private fun loadUsers(centerId: String) {
        firestore.collection("users")
            .whereEqualTo("centerId", centerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val users = snapshot.toObjects(UserProfile::class.java)
                    _usersList.value = users
                    _usersCount.value = users.size
                }
            }
    }

    private fun loadTasks(centerId: String) {
        firestore.collection("centers").document(centerId)
            .collection("tasks")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _tasks.value = snapshot.toObjects(Task::class.java)
                }
            }
    }

    private fun loadCompletedTasks(centerId: String) {
        firestore.collection("centers").document(centerId)
            .collection("completedTasks")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _completedTasks.value = snapshot.toObjects(Task::class.java)
                }
            }
    }

    fun sendTaskSolution(centerId: String, task: Task, solution: String) {
        val updates = hashMapOf<String, Any>(
            "solution" to solution,
            "solutionSent" to true
        )
        firestore.collection("centers")
            .document(centerId)
            .collection("tasks")
            .document(task.id)
            .update(updates)
            .addOnSuccessListener {
                _tasks.value = _tasks.value.map { t ->
                    if (t.id == task.id) task.copy(solution = solution, isSolutionSent = true)
                    else t
                }
            }
            .addOnFailureListener { e ->
                Log.e("CenterViewModel", "Ошибка при обновлении задачи: $e")
            }
    }

    fun completeTask(centerId: String, task: Task) {
        val completedTask = task.copy(isCompleted = true)
        firestore.collection("centers")
            .document(centerId)
            .collection("completedTasks")
            .document(completedTask.id)
            .set(completedTask)
            .addOnSuccessListener {
                firestore.collection("centers")
                    .document(centerId)
                    .collection("tasks")
                    .document(task.id)
                    .delete()
                    .addOnSuccessListener {
                        _tasks.value = _tasks.value.filter { it.id != task.id }
                        _completedTasks.value = _completedTasks.value + completedTask
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CenterViewModel", "Ошибка при завершении задачи", e)
            }
    }

    fun changeUserList() {
        _isUserListVisible.value = !_isUserListVisible.value
    }

    fun setShowCompleted(show: Boolean) {
        _showCompleted.value = show
    }

    fun changeAddTaskForm() {
        _showAddTaskForm.value = !_showAddTaskForm.value
    }

    fun setTitle(title: String) {
        _newTaskTitle.value = title
    }

    fun set_new_description(description: String) {
        _newTaskDescription.value = description
    }

    fun publishNewTask(centerId: String) {
        val title = _newTaskTitle.value
        val description = _newTaskDescription.value
        if (title.isNotBlank() && description.isNotBlank()) {
            val newTask = Task(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                description = description
            )
            firestore.collection("centers").document(centerId)
                .collection("tasks").document(newTask.id)
                .set(newTask)
                .addOnSuccessListener {
                    _newTaskTitle.value = ""
                    _newTaskDescription.value = ""
                    _showAddTaskForm.value = false
                }
        }
    }
}