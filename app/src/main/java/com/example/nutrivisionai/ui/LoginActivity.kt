package com.example.nutrivisionai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.nutrivisionai.databinding.ActivityLoginBinding
import com.example.nutrivisionai.model.AppDB
import com.example.nutrivisionai.repository.UserRepository
import com.example.nutrivisionai.viewmodel.AuthViewModel
import com.example.nutrivisionai.viewmodel.UserViewModel
import com.example.nutrivisionai.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var userViewModel: UserViewModel
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUserViewModel()

        binding.r.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginbtn2.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.login(email, password)
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.authState.observe(this) { result ->
            result.onSuccess {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    userViewModel.checkProfileExists(userId)
                }
            }.onFailure {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.profileExists.observe(this) { exists ->
            if (exists) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
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
