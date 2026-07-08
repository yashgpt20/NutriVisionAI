package com.example.nutrivisionai.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealLog)

    @Query("SELECT * FROM meal_logs WHERE userId = :currentUserId ORDER BY timestamp DESC")
    fun getMealsForUser(currentUserId: String): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE userId = :uid AND timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp DESC")
    fun getMealsForToday(uid: String, startOfDay: Long, endOfDay: Long): Flow<List<MealLog>>

    @Query("DELETE FROM meal_logs WHERE id = :mealId")
    suspend fun deleteMeal(mealId: Int)
}