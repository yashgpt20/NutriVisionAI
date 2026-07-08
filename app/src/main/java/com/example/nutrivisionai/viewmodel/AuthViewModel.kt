package com.example.nutrivisionai.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutrivisionai.repository.AuthRepository
import com.google.firebase.auth.AuthResult

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableLiveData<Result<AuthResult>>()
    val authState: LiveData<Result<AuthResult>> = _authState

    fun login(email: String, password: String) {
        repository.login(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = Result.success(task.result!!)
            } else {
                _authState.value = Result.failure(task.exception!!)
            }
        }
    }

    fun register(email: String, password: String) {
        repository.register(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = Result.success(task.result!!)
            } else {
                _authState.value = Result.failure(task.exception!!)
            }
        }
    }
}
