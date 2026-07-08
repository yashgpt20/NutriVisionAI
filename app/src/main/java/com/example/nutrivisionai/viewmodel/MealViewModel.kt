package com.example.nutrivisionai.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutrivisionai.model.MealDao
import com.example.nutrivisionai.model.MealLog
import com.example.nutrivisionai.repository.AiRepository
import com.google.firebase.auth.FirebaseAuth // <-- Added Firebase Auth import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiMealState {
    object Idle : AiMealState
    object Loading : AiMealState
    data class Success(val mealLog: MealLog) : AiMealState
    data class Error(val message: String) : AiMealState
}

class MealViewModel(
    private val aiRepository: AiRepository,
    private val mealDao: MealDao
) : ViewModel() {

    private val _aiMealState = MutableStateFlow<AiMealState>(AiMealState.Idle)
    val aiMealState: StateFlow<AiMealState> = _aiMealState.asStateFlow()

    fun analyzeMealImage(bitmap: Bitmap) {
        _aiMealState.value = AiMealState.Loading

        viewModelScope.launch {
            try {
                val aiResult = aiRepository.analyzeMeal(bitmap)
                val currentUser = FirebaseAuth.getInstance().currentUser
                val uid = currentUser?.uid ?: "guest_user"

                val userSpecificMeal = aiResult.copy(userId = uid)
                mealDao.insertMeal(userSpecificMeal)

                _aiMealState.value = AiMealState.Success(userSpecificMeal)

            } catch (e: Exception) {
                Log.e("MealViewModel", "AI Analysis Error: ${e.message}", e)
                if (e.message?.contains("NOT_FOOD_ERROR") == true) {
                    _aiMealState.value = AiMealState.Error("That doesn't look like food!")
                } else {
                    // Handle normal network or JSON errors
                    _aiMealState.value = AiMealState.Error("Analysis failed: ${e.message}")
                }
            }
        }
    }

    fun resetState() {
        _aiMealState.value = AiMealState.Idle
    }
}

class MealViewModelFactory(
    private val repository: AiRepository,
    private val mealDao: MealDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(repository, mealDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}