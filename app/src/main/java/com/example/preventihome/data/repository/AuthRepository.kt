package com.example.preventihome.data.repository

import com.example.preventihome.data.remote.FirebaseAuthSource
import com.example.preventihome.domain.model.User
import com.example.preventihome.utils.Prefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authSource: FirebaseAuthSource,
    private val prefs: Prefs
) {
    val isLoggedIn: Boolean get() = authSource.currentUser != null

    suspend fun loginWithEmail(email: String, password: String): Result<User> = runCatching {
        val user = authSource.signInWithEmail(email, password)
        prefs.saveCredentials(email, password)
        user
    }

    suspend fun loginWithGoogle(idToken: String): Result<User> = runCatching {
        authSource.signInWithGoogle(idToken)
    }

    suspend fun loginWithSavedCredentials(): Result<User> = runCatching {
        val email = prefs.getEmail()
        val password = prefs.getPassword()
        if (email.isEmpty() || password.isEmpty()) throw Exception("Sin credenciales guardadas")
        authSource.signInWithEmail(email, password)
    }

    fun logout() {
        authSource.signOut()
        prefs.clearCredentials()
    }

    fun hasSavedCredentials(): Boolean = prefs.hasCredentials()
}