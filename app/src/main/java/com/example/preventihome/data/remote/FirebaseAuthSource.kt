package com.example.preventihome.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.preventihome.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuente de datos encargada de manejar la autenticación con Firebase.
 *
 * Se comunica con:
 * - Firebase Authentication (para login/registro)
 * - FirestoreSource (para almacenar/obtener datos del usuario)
 */
@Singleton
class FirebaseAuthSource @Inject constructor(
    private val auth: FirebaseAuth, // Instancia de FirebaseAuth inyectada
    private val firestoreSource: FirestoreSource // Fuente de datos para Firestore
) {

    /**
     * Obtiene el usuario actualmente autenticado en Firebase.
     */
    val currentUser get() = auth.currentUser

    /**
     * Determina el rol del usuario basado en el dominio de su correo electrónico.
     *
     * Reglas:
     * - @admin.preventihome.com -> admin
     * - @fisio.preventihome.com -> fisio
     * - cualquier otro -> paciente
     */
    private fun rolPorDominio(email: String): String = when {
        email.endsWith("@admin.preventihome.com") -> "admin"
        email.endsWith("@fisio.preventihome.com") -> "fisio"
        else -> "paciente"
    }

    /**
     * Inicia sesión con correo y contraseña.
     *
     * Flujo:
     * 1. Autentica con FirebaseAuth
     * 2. Intenta obtener el usuario desde Firestore
     * 3. Si no existe, lo crea automáticamente
     *
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     * @return Usuario del dominio de la app
     */
    suspend fun signInWithEmail(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val fbUser = result.user ?: throw Exception("Error al autenticar")

        // Intentar leer perfil existente, si no existe crearlo
        return try {
            firestoreSource.getUser(fbUser.uid)
        } catch (e: Exception) {

            // Determinar rol automáticamente según el dominio
            val rol = rolPorDominio(fbUser.email ?: "")

            // Crear nuevo usuario en Firestore
            val newUser = User(
                uid = fbUser.uid,
                email = fbUser.email ?: "",
                nombre = "", // Nombre vacío inicialmente
                rol = rol
            )

            firestoreSource.createUser(newUser)
            newUser
        }
    }

    /**
     * Inicia sesión usando Google.
     *
     * Flujo:
     * 1. Convierte el idToken en credenciales de Firebase
     * 2. Autentica con Firebase
     * 3. Busca el usuario en Firestore
     * 4. Si no existe, lo crea
     *
     * @param idToken Token de autenticación de Google
     * @return Usuario del dominio de la app
     */
    suspend fun signInWithGoogle(idToken: String): User {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val fbUser = result.user ?: throw Exception("Error con Google")

        return try {
            firestoreSource.getUser(fbUser.uid)
        } catch (e: Exception) {

            // Crear usuario nuevo con datos de Google
            val newUser = User(
                uid = fbUser.uid,
                email = fbUser.email ?: "",
                nombre = fbUser.displayName ?: "", // Nombre proporcionado por Google
                rol = "paciente" // Por defecto todos los de Google son pacientes
            )

            firestoreSource.createUser(newUser)
            newUser
        }
    }

    /**
     * Registra un nuevo usuario con correo y contraseña.
     *
     * Flujo:
     * 1. Crea el usuario en FirebaseAuth
     * 2. Asigna rol basado en el dominio del correo
     * 3. Guarda el usuario en Firestore
     *
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     * @param nombre Nombre del usuario
     * @return Usuario creado
     */
    suspend fun register(email: String, password: String, nombre: String): User {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val fbUser = result.user ?: throw Exception("Error al registrar")

        // Determinar rol automáticamente
        val rol = rolPorDominio(email)

        // Crear objeto de usuario
        val newUser = User(
            uid = fbUser.uid,
            email = email,
            nombre = nombre,
            rol = rol
        )

        // Guardar en Firestore
        firestoreSource.createUser(newUser)

        return newUser
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() = auth.signOut()
}