package com.example.nutrivisionai.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivisionai.model.Userdetails
import com.example.nutrivisionai.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _profileExists = MutableLiveData<Boolean>()
    val profileExists: LiveData<Boolean> = _profileExists

    fun checkProfileExists(userId: String) {
        viewModelScope.launch {
            val exists = repository.isProfileComplete(userId)
            _profileExists.postValue(exists)
        }
    }

    fun getUserProfile(userId: String): StateFlow<Userdetails?> {
        return repository.getUserProfile(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun saveProfile(userId: String, name: String, age: Int, weight: Double) {
        viewModelScope.launch {
            val profile = Userdetails(
                userId = userId,
                name = name,
                age = age,
                weight = weight
            )
            repository.saveUserProfile(profile)
            _saveStatus.postValue(true)
        }
    }
}