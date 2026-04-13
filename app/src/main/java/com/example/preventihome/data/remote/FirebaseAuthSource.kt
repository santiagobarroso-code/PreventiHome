package com.example.preventihome.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.preventihome.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreSource: FirestoreSource
) {
    val currentUser get() = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val fbUser = result.user ?: throw Exception("Error al autenticar")
        return firestoreSource.getUser(fbUser.uid)
    }

    suspend fun signInWithGoogle(idToken: String): User {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val fbUser = result.user ?: throw Exception("Error con Google")
        return try {
            firestoreSource.getUser(fbUser.uid)
        } catch (e: Exception) {
            val newUser = User(uid = fbUser.uid, email = fbUser.email ?: "")
            firestoreSource.createUser(newUser)
            newUser
        }
    }

    suspend fun register(email: String, password: String, nombre: String): User {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val fbUser = result.user ?: throw Exception("Error al registrar")
        val newUser = User(uid = fbUser.uid, email = email, nombre = nombre)
        firestoreSource.createUser(newUser)
        return newUser
    }

    fun signOut() = auth.signOut()
}