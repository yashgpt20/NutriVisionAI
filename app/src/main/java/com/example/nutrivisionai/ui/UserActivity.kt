package com.example.nutrivisionai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.nutrivisionai.databinding.ActivityUserBinding
import com.example.nutrivisionai.model.AppDB
import com.example.nutrivisionai.repository.UserRepository
import com.example.nutrivisionai.viewmodel.UserViewModel
import com.example.nutrivisionai.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserActivity : AppCompatActivity() {
    private lateinit var viewModel: UserViewModel
    private val auth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupViewModel()
        setupClickListener()
        observeViewModel()
    }

    private fun setupViewModel() {
        val database = AppDB.getDatabase(this)
        val firestore = FirebaseFirestore.getInstance()

        val repository = UserRepository(database.userDao(), firestore)
        val factory = UserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.saveStatus.observe(this) { success ->
            if (success) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupClickListener() {
        binding.submit.setOnClickListener {
            val nameStr = binding.name2.text.toString().trim()
            val ageStr = binding.age2.text.toString().trim()
            val weightStr = binding.weight2.text.toString().trim()

            if (nameStr.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageStr.toIntOrNull()
            val weight = weightStr.toDoubleOrNull()

            if (age == null || weight == null) {
                Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUserId = auth.currentUser?.uid ?: "test_user_id"

            binding.submit.isEnabled = false // Prevent double clicks
            Toast.makeText(this, "Saving profile...", Toast.LENGTH_SHORT).show()

            viewModel.saveProfile(
                userId = currentUserId,
                name = nameStr,
                age = age,
                weight = weight
            )
        }
    }
}
