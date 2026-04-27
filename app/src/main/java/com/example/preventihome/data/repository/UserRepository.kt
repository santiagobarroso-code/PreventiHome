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
 * lectura, cambio de roles y creación desde el panel de admin.
 *
 * Patrón: Repository — abstrae las fuentes de datos (Firestore + Firebase Auth)
 * del resto de la aplicación.
 */
@Singleton
class UserRepository @Inject constructor(
    private val firestoreSource: FirestoreSource,
    private val auth: FirebaseAuth
) {
    /**
     * Obtiene todos los usuarios con rol "paciente".
     * Usado por el panel del fisioterapeuta.
     */
    suspend fun getPacientes(): Result<List<User>> = runCatching {
        firestoreSource.getUsersByRol("paciente")
    }

    /**
     * Obtiene todos los usuarios de la plataforma.
     * Usado por el panel del administrador.
     */
    suspend fun getAllUsers(): Result<List<User>> = runCatching {
        firestoreSource.getAllUsers()
    }

    /**
     * Promueve a un usuario al rol de fisioterapeuta.
     * @param uid UID del usuario a promover
     */
    suspend fun promoverAFisio(uid: String): Result<Unit> = runCatching {
        firestoreSource.updateUserRol(uid, "fisio")
    }

    /**
     * Revoca el rol de fisioterapeuta y regresa a paciente.
     * @param uid UID del usuario
     */
    suspend fun revocarFisio(uid: String): Result<Unit> = runCatching {
        firestoreSource.updateUserRol(uid, "paciente")
    }

    /**
     * Crea un nuevo usuario desde el panel de administrador.
     * Usa Firebase Auth para crear la cuenta y Firestore para el perfil.
     * El rol se asigna automáticamente según el dominio del correo,
     * pero puede ser sobreescrito por el parámetro rolExplicito.
     *
     * @param email Correo del nuevo usuario
     * @param password Contraseña temporal
     * @param nombre Nombre completo
     * @param rol Rol a asignar: "paciente", "fisio" o "admin"
     */
    suspend fun crearUsuario(
        email: String,
        password: String,
        nombre: String,
        rol: String
    ): Result<Unit> = runCatching {
        // Crear cuenta en Firebase Auth
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Error al crear usuario en Auth")

        // Guardar perfil en Firestore con el rol especificado
        val nuevoUsuario = User(
            uid = uid,
            email = email,
            nombre = nombre,
            rol = rol
        )
        firestoreSource.createUser(nuevoUsuario)
    }
}