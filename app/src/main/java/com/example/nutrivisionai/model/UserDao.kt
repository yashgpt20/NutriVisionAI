package com.example.nutrivisionai.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface userdetailsDao {
    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    fun getUserProfile(userId: String): Flow<Userdetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: Userdetails)

}