package com.example.preventihome.data.repository

import com.example.preventihome.data.remote.FirestoreSource
import com.example.preventihome.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de gestión de usuarios.
 * Centraliza todas las operaciones relacionadas con usuarios:
 * lectura, cambio de roles, creación y eliminación.
 *
 * Nota sobre eliminación: Firebase Auth no permite eliminar otros usuarios
 * desde el cliente. Solo se elimina el documento de Firestore desde aquí.
 * Para eliminar la cuenta de Auth completamente se requeriría Cloud Functions.
 * Para efectos del proyecto, eliminamos el documento de Firestore y el usuario
 * queda inhabilitado funcionalmente (sin perfil no puede usar la app).
 */
@Singleton
class UserRepository @Inject constructor(
    private val firestoreSource: FirestoreSource,
    private val auth: FirebaseAuth
) {
    /**
     * Obtiene todos los usuarios con rol "paciente".
     */
    suspend fun getPacientes(): Result<List<User>> = runCatching {
        firestoreSource.getUsersByRol("paciente")
    }

    /**
     * Obtiene todos los usuarios de la plataforma.
     */
    suspend fun getAllUsers(): Result<List<User>> = runCatching {
        firestoreSource.getAllUsers()
    }

    /**
     * Promueve a un usuario al rol de fisioterapeuta.
     */
    suspend fun promoverAFisio(uid: String): Result<Unit> = runCatching {
        firestoreSource.updateUserRol(uid, "fisio")
    }

    /**
     * Revoca el rol de fisioterapeuta y regresa a paciente.
     */
    suspend fun revocarFisio(uid: String): Result<Unit> = runCatching {
        firestoreSource.updateUserRol(uid, "paciente")
    }

    /**
     * Crea un nuevo usuario desde el panel de administrador.
     * Crea la cuenta en Firebase Auth y el perfil en Firestore.
     *
     * @param email    Correo — ya debe incluir el dominio correcto
     * @param password Contraseña temporal
     * @param nombre   Nombre completo
     * @param rol      Rol: "paciente", "fisio" o "admin"
     */
    suspend fun crearUsuario(
        email: String,
        password: String,
        nombre: String,
        rol: String
    ): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Error al crear usuario en Auth")
        val nuevoUsuario = User(uid = uid, email = email, nombre = nombre, rol = rol)
        firestoreSource.createUser(nuevoUsuario)
    }

    /**
     * Elimina el documento del usuario en Firestore.
     * Nota: la cuenta de Firebase Auth permanece pero sin perfil
     * el usuario no puede acceder a ninguna funcionalidad de la app.
     *
     * @param uid UID del usuario a eliminar
     */
    suspend fun eliminarUsuario(uid: String): Result<Unit> = runCatching {
        firestoreSource.deleteUser(uid)
    }
}