package com.example.nutrivisionai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutrivisionai.databinding.ActivityRegisterBinding
import com.example.nutrivisionai.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.l.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.registerbtn2.setOnClickListener {
            val email = binding.email1.text.toString()
            val password = binding.password2.text.toString()
            val cnfpassword = binding.cnfpassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && cnfpassword.isNotEmpty()) {
                if (password == cnfpassword) {
                    viewModel.register(email, password)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.authState.observe(this) { result ->
            result.onSuccess {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                Toast.makeText(this, "Successfully Registered", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "Registration Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
