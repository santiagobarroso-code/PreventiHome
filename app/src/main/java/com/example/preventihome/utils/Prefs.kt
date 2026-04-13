package com.example.preventihome.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefs @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("physio_prefs", Context.MODE_PRIVATE)

    fun saveCredentials(email: String, password: String) {
        prefs.edit()
            .putString("email", email)
            .putString("password", password)
            .apply()
    }

    fun getEmail(): String = prefs.getString("email", "") ?: ""
    fun getPassword(): String = prefs.getString("password", "") ?: ""
    fun hasCredentials(): Boolean = getEmail().isNotEmpty()
    fun clearCredentials() = prefs.edit().clear().apply()
}