package com.example.nutrivisionai.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.nutrivisionai.databinding.FragmentProfileBinding
import com.example.nutrivisionai.model.AppDB
import com.example.nutrivisionai.repository.UserRepository
import com.example.nutrivisionai.viewmodel.UserViewModel
import com.example.nutrivisionai.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserViewModel
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        observeUserData()

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), SplashActivity2::class.java)
            // This clears the backstack so the user can't press the back button to return to the profile
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupViewModel() {
        val database = AppDB.getDatabase(requireContext())
        val firestore = FirebaseFirestore.getInstance()
        val repository = UserRepository(database.userDao(), firestore)
        val factory = UserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun observeUserData() {
        val userId = auth.currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getUserProfile(userId).collect { userDetails ->

                if (userDetails != null) {
                    binding.profileName.setText(userDetails.name)
                    binding.profileAge.setText(userDetails.age.toString())
                    binding.profileWeight.setText(userDetails.weight.toString())

                } else {
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val name = document.getString("name") ?: ""
                                val age = document.getLong("age")?.toInt()?.toString() ?: ""
                                val weight = document.getLong("weight")?.toInt()?.toString() ?: ""
                                binding.profileName.setText(name)
                                binding.profileAge.setText(age)
                                binding.profileWeight.setText(weight)
                            }
                        }
                        .addOnFailureListener {
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}