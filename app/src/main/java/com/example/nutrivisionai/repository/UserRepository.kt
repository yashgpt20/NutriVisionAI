package com.example.nutrivisionai.repository

import com.example.nutrivisionai.model.Userdetails
import com.example.nutrivisionai.model.userdetailsDao
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: userdetailsDao,
                     private val firestore: FirebaseFirestore)
{
    fun getUserProfile(userId: String): Flow<Userdetails?> {
        return userDao.getUserProfile(userId)
    }
    
    suspend fun saveUserProfile(profile: Userdetails) {
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users")
                    .document(profile.userId)
                    .set(profile)
                    .await()
                userDao.insertUserProfile(profile.copy(isSyncedWithCloud = true))
                Log.d("UserRepository", "Successfully synced with cloud")
            } catch (e: Exception) {
                Log.e("UserRepository", "Cloud sync failed: ${e.message}")
                // If network fails, cache it locally anyway but flag it as unsynced
                userDao.insertUserProfile(profile.copy(isSyncedWithCloud = false))
            }
        }
    }

    suspend fun isProfileComplete(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                document.exists()
            } catch (e: Exception) {
                false
            }
        }
    }
}
