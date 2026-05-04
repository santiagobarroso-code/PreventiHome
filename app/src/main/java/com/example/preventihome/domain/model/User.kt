package com.example.preventihome.domain.model

/**
 * Modelo de dominio que representa a un usuario dentro de la aplicación.
 *
 * Contiene la información básica necesaria para identificar al usuario,
 * así como su rol dentro del sistema.
 */
data class User(

    /** Identificador único del usuario (proporcionado por Firebase Auth) */
    val uid: String,

    /** Correo electrónico del usuario */
    val email: String,

    /** Nombre del usuario (puede estar vacío inicialmente) */
    val nombre: String = "",

    /**
     * Rol del usuario dentro de la aplicación.
     *
     * Valores esperados:
     * - "paciente": usuario estándar
     * - "fisio": especialista o fisioterapeuta
     * - "admin": administrador del sistema
     */
    val rol: String = "paciente"  // "paciente" | "fisio" | "admin"
)