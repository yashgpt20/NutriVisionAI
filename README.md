# NutriVision AI 🤖

> An intelligent, offline-first Android application that leverages the Gemini 2.5 AI model to track daily nutrition and macronutrients from food images.

## 📖 Overview
NutriVision AI simplifies calorie tracking by eliminating manual entry. Users simply snap a photo of their meal, and the app utilizes Google's Generative AI to estimate the meal's name, calories, protein, carbohydrates, and fats. Built with a robust offline-first architecture, it ensures lightning-fast UI updates while securely isolating data for multiple users on the same device.

## ✨ Key Features
* **AI Food Analysis:** Integrates the Gemini 2.5 Flash API to analyze food imagery, utilizing strict safety settings and robust JSON parsing to extract accurate nutritional data.
* **Offline-First Caching:** Uses Room Database as the single source of truth for daily meals, allowing the Daily Analytics Dashboard to update instantly via Kotlin Flows.
* **Multi-User Data Isolation:** Securely stamps every local database entry with a Firebase Auth UID, ensuring users only ever see their own daily data.
* **Cloud Profile Sync:** Implements a fallback caching strategy where user profile details (Name, Age, Weight) are fetched from Firebase Firestore if local data is wiped, allowing profiles to survive app reinstalls.
* **Daily Goal Tracking:** Real-time progress indicators tracking daily caloric and macronutrient consumption against personalized user goals.

## 🔄 Application Flow
1. **Authentication:** User logs in via Firebase Auth. The app retrieves their UID and checks the local Room database for profile details. If missing, it fetches them from Firestore and caches them locally.
2. **Dashboard Observation:** The `AnalyticsFragment` subscribes to a Kotlin Flow from Room, requesting only meals that match the current user's UID and today's timestamp.
3. **AI Processing:** The user captures an image. The image is downscaled and sent to the Gemini 2.5 API with a strict system prompt.
4. **Data Synchronization:** The AI returns a JSON string, which the `AiRepository` parses into a `MealLog` object. The `MealViewModel` stamps the object with the user's UID and saves it to Room.
5. **Reactive UI:** Room detects the new database row and automatically pushes the updated meal list through the Flow, instantly recalculating the user's daily macros and animating the dashboard progress bars.

## 🛠️ Tech Stack
* **Language:** Kotlin
* **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Local Storage:** Room (SQLite)
* **Cloud Services:** Firebase Authentication, Firebase Firestore
* **Artificial Intelligence:** Google Generative AI SDK (Gemini 2.5)
* **Asynchronous Programming:** Kotlin Coroutines & StateFlow / Flow
* **UI Components:** ViewBinding, RecyclerView, custom XML drawables

## 🔒 Security Practices
This repository is strictly structured to prevent API key leaks:
* `google-services.json` is excluded via `.gitignore`.
* The Gemini API key is stored locally in `local.properties` and dynamically injected into the app at compile-time using Gradle's `BuildConfig` feature.

## 👨‍💻 Author
* **Yash Gupta**
* **Github:** https://github.com/yashgpt20
