package com.example.preventihome.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.example.preventihome.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuente de datos encargada de interactuar con Firebase Firestore
 * para la gestión de usuarios.
 *
 * Todas las operaciones son suspend (coroutines) para trabajar de forma asíncrona.
 */
@Singleton
class FirestoreSource @Inject constructor(private val db: FirebaseFirestore) {

    /**
     * Obtiene un usuario desde Firestore por su UID.
     *
     * Flujo:
     * 1. Consulta el documento en la colección "users"
     * 2. Verifica si existe
     * 3. Mapea los datos al modelo User
     *
     * @param uid Identificador único del usuario
     * @return Usuario encontrado
     * @throws Exception si el usuario no existe
     */
    suspend fun getUser(uid: String): User {
        val doc = db.collection("users").document(uid).get().await()
        if (!doc.exists()) throw Exception("Usuario no encontrado")

        return User(
            uid = uid,
            email = doc.getString("email") ?: "",
            nombre = doc.getString("nombre") ?: "",
            rol = doc.getString("rol") ?: "paciente" // Valor por defecto
        )
    }

    /**
     * Crea un nuevo usuario en Firestore.
     *
     * Flujo:
     * 1. Convierte el objeto User a un mapa clave-valor
     * 2. Guarda el documento en la colección "users"
     *
     * @param user Usuario a almacenar
     */
    suspend fun createUser(user: User) {
        val data = mapOf(
            "email" to user.email,
            "nombre" to user.nombre,
            "rol" to user.rol
        )

        db.collection("users").document(user.uid).set(data).await()
    }

    /**
     * Actualiza el rol de un usuario específico.
     *
     * @param uid UID del usuario
     * @param nuevoRol Nuevo rol asignado (admin, fisio, paciente, etc.)
     */
    suspend fun updateUserRol(uid: String, nuevoRol: String) {
        db.collection("users").document(uid)
            .update("rol", nuevoRol)
            .await()
    }

    /**
     * Obtiene todos los usuarios almacenados en Firestore.
     *
     * Flujo:
     * 1. Recupera todos los documentos de la colección "users"
     * 2. Mapea cada documento a un objeto User
     *
     * @return Lista de usuarios
     */
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

    /**
     * Obtiene usuarios filtrados por rol.
     *
     * Flujo:
     * 1. Realiza una consulta con filtro (whereEqualTo)
     * 2. Mapea los resultados a objetos User
     *
     * @param rol Rol a filtrar (admin, fisio, paciente)
     * @return Lista de usuarios con el rol especificado
     */
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
     *
     * Nota:
     * - Solo elimina el perfil en Firestore
     * - NO elimina la cuenta en Firebase Authentication
     * - El usuario quedará sin acceso funcional en la app
     *
     * @param uid UID del usuario a eliminar
     */
    suspend fun deleteUser(uid: String) {
        db.collection("users").document(uid).delete().await()
    }

    /**
     * Actualiza el nombre de un usuario en Firestore.
     *
     * @param uid UID del usuario
     * @param nuevoNombre Nuevo nombre del usuario
     */
    suspend fun updateUserNombre(uid: String, nuevoNombre: String) {
        db.collection("users").document(uid)
            .update("nombre", nuevoNombre)
            .await()
    }
}