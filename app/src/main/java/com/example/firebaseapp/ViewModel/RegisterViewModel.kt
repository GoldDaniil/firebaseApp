package com.example.firebaseapp.ViewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegisterViewModel(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _registerState.value = RegisterState.Error("Пожалуйста, заполните все поля")
            return
        }
        _registerState.value = RegisterState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registerState.value = RegisterState.Success
                } else {
                    val error = task.exception?.message ?: "Ошибка входа"
                    _registerState.value = RegisterState.Error(error)
                }
            }
    }
}
sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}