package com.example.exampratice.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("StudentPractice", Context.MODE_PRIVATE)

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("email", email).apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("email", null)
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("name", name).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("name", null)
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}