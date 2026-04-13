package com.example.preventihome.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.example.preventihome.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSource @Inject constructor(private val db: FirebaseFirestore) {

    suspend fun getUser(uid: String): User {
        val doc = db.collection("users").document(uid).get().await()
        return User(
            uid = uid,
            email = doc.getString("email") ?: "",
            nombre = doc.getString("nombre") ?: "",
            rol = doc.getString("rol") ?: "paciente"
        )
    }

    suspend fun createUser(user: User) {
        val data = mapOf(
            "email" to user.email,
            "nombre" to user.nombre,
            "rol" to user.rol
        )
        db.collection("users").document(user.uid).set(data).await()
    }
}