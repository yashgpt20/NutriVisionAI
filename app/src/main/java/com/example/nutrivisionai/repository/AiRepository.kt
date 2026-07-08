package com.example.nutrivisionai.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.nutrivisionai.model.MealLog
import com.example.nutrivisionai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import org.json.JSONObject

class AiRepository {
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH)
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = safetySettings
    )

    suspend fun analyzeMeal(bitmap: Bitmap): MealLog {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 640, (bitmap.height * (640.0 / bitmap.width)).toInt(), true)
        
        val prompt = """
            You are a nutrition expert. Analyze this image.
            If there is NO food/drink, return: NOT_FOOD
            If there IS food, estimate the nutritional values and return ONLY a JSON object:
            {
              "mealName": "Name",
              "calories": 0,
              "protein": 0,
              "fats": 0,
              "carbs": 0
            }
        """.trimIndent()

        val inputContent = content {
            image(scaledBitmap)
            text(prompt)
        }

        try {
            val response = generativeModel.generateContent(inputContent)

            val responseText = response.text?.trim()
                ?: throw Exception("AI blocked the response or returned empty. Try a different angle.")

            Log.d("AiRepository", "AI Output: $responseText")

            if (responseText.contains("NOT_FOOD", ignoreCase = true)) {
                throw Exception("NOT_FOOD_ERROR")
            }

            val start = responseText.indexOf('{')
            val end = responseText.lastIndexOf('}')
            if (start == -1 || end == -1) throw Exception("Could not find nutrition data in AI response")

            val json = responseText.substring(start, end + 1)
            val obj = JSONObject(json)
            
            return MealLog(
                mealName = obj.getString("mealName"),
                calories = obj.getInt("calories"),
                protein = obj.getInt("protein"),
                fats = obj.getInt("fats"),
                carbs = obj.getInt("carbs"),
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("AiRepository", "Error during analysis", e)
            throw e
        }
    }
}
