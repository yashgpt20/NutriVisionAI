package com.example.nutrivisionai.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "user_profile")
data class Userdetails(
    @PrimaryKey val userId: String = "",
    val name: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    @get:Exclude var isSyncedWithCloud: Boolean = true)