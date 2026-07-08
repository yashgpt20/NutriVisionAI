package com.example.nutrivisionai.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "",
    val mealName: String,
    val calories: Int,
    val protein: Int,
    val fats: Int,
    val carbs: Int,
    val timestamp: Long = System.currentTimeMillis()
)
