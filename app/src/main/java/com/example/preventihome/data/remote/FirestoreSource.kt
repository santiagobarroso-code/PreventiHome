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
        if (!doc.exists()) throw Exception("Usuario no encontrado")
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

    suspend fun updateUserRol(uid: String, nuevoRol: String) {
        db.collection("users").document(uid)
            .update("rol", nuevoRol)
            .await()
    }

    suspend fun getAllUsers(): List<User> {
        val snapshot = db.collection("users").get().await()
        return snapshot.documents.map { doc ->
            User(
                uid = doc.id,
                email = doc.getString("email") ?: "",
                nombre = doc.getString("nombre") ?: "",
                rol = doc.getString("rol") ?: "paciente"
            )
        }
    }

    suspend fun getUsersByRol(rol: String): List<User> {
        val snapshot = db.collection("users")
            .whereEqualTo("rol", rol)
            .get()
            .await()
        return snapshot.documents.map { doc ->
            User(
                uid = doc.id,
                email = doc.getString("email") ?: "",
                nombre = doc.getString("nombre") ?: "",
                rol = doc.getString("rol") ?: "paciente"
            )
        }
    }


    /**
     * Elimina el documento de un usuario en Firestore.
     * La cuenta de Firebase Auth permanece pero sin perfil
     * el usuario no puede operar en la plataforma.
     *
     * @param uid UID del usuario a eliminar
     */
    suspend fun deleteUser(uid: String) {
        db.collection("users").document(uid).delete().await()
    }

    /**
     * Actualiza el nombre de un usuario en Firestore.
     *
     * @param uid         UID del usuario
     * @param nuevoNombre Nombre actualizado
     */
    suspend fun updateUserNombre(uid: String, nuevoNombre: String) {
        db.collection("users").document(uid)
            .update("nombre", nuevoNombre)
            .await()
    }
}