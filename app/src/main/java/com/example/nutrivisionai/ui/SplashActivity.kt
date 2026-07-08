package com.example.nutrivisionai.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.nutrivisionai.R
import com.example.nutrivisionai.model.AppDB
import com.example.nutrivisionai.repository.UserRepository
import com.example.nutrivisionai.viewmodel.UserViewModel
import com.example.nutrivisionai.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.splash)
        
        val punch = findViewById<TextView>(R.id.punch)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        punch.startAnimation(fadeIn)

        setupUserViewModel()

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                userViewModel.checkProfileExists(currentUser.uid)
            } else {
                startActivity(Intent(this, SplashActivity2::class.java))
                finish()
            }
        }, 2400)

        userViewModel.profileExists.observe(this) { exists ->
            if (exists) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, UserActivity::class.java))
            }
            finish()
        }
    }

    private fun setupUserViewModel() {
        val database = AppDB.getDatabase(this)
        val firestore = FirebaseFirestore.getInstance()
        val repository = UserRepository(database.userDao(), firestore)
        val factory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }
}
