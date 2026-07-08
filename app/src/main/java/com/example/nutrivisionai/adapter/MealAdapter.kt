package com.example.nutrivisionai.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivisionai.R
import com.example.nutrivisionai.model.MealLog

class MealAdapter : ListAdapter<MealLog, MealAdapter.MealViewHolder>(MealDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealName: TextView = itemView.findViewById(R.id.tvItemMealName)
        private val macros: TextView = itemView.findViewById(R.id.tvItemMacros)
        private val calories: TextView = itemView.findViewById(R.id.tvItemCalories)

        fun bind(meal: MealLog) {
            mealName.text = meal.mealName
            macros.text = "P: ${meal.protein}g • C: ${meal.carbs}g • F: ${meal.fats}g"
            calories.text = "${meal.calories} kcal"
        }
    }

    class MealDiffCallback : DiffUtil.ItemCallback<MealLog>() {
        override fun areItemsTheSame(oldItem: MealLog, newItem: MealLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MealLog, newItem: MealLog): Boolean {
            return oldItem == newItem
        }
    }
}
