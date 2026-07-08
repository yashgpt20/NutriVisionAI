package com.example.nutrivisionai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivisionai.adapter.MealAdapter
import com.example.nutrivisionai.databinding.FragmentAnalyticsBinding
import com.example.nutrivisionai.model.AppDB
import com.google.firebase.auth.FirebaseAuth // <-- 1. Added Firebase Import
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mealAdapter: MealAdapter

    // Daily Goals
    private val GOAL_CALORIES = 2000
    private val GOAL_PROTEIN = 150
    private val GOAL_CARBS = 250
    private val GOAL_FATS = 70

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mealAdapter = MealAdapter()
        binding.rvMealHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mealAdapter
        }

        observeDailyAnalytics()
    }

    private fun observeDailyAnalytics() {
        val database = AppDB.getDatabase(requireContext())
        val mealDao = database.mealDao()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUid = currentUser?.uid ?: "guest_user"

        viewLifecycleOwner.lifecycleScope.launch {
            mealDao.getMealsForToday(currentUid, startOfDay, endOfDay).collectLatest { meals ->

                val totalCalories = meals.sumOf { it.calories }
                val totalProtein = meals.sumOf { it.protein }
                val totalCarbs = meals.sumOf { it.carbs }
                val totalFats = meals.sumOf { it.fats }

                binding.tvTotalCalories.text = totalCalories.toString()
                binding.tvTotalProtein.text = "Protein: ${totalProtein}g"
                binding.tvTotalCarbs.text = "Carbs: ${totalCarbs}g"
                binding.tvTotalFats.text = "Fats: ${totalFats}g"

                binding.progressCalories.max = GOAL_CALORIES
                binding.progressCalories.progress = if (totalCalories > GOAL_CALORIES) GOAL_CALORIES else totalCalories

                binding.progressProtein.max = GOAL_PROTEIN
                binding.progressProtein.progress = if (totalProtein > GOAL_PROTEIN) GOAL_PROTEIN else totalProtein

                binding.progressCarbs.max = GOAL_CARBS
                binding.progressCarbs.progress = if (totalCarbs > GOAL_CARBS) GOAL_CARBS else totalCarbs

                binding.progressFats.max = GOAL_FATS
                binding.progressFats.progress = if (totalFats > GOAL_FATS) GOAL_FATS else totalFats

                mealAdapter.submitList(meals)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}