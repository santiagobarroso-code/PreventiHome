package com.example.preventihome.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase encargada de gestionar el almacenamiento local de credenciales
 * utilizando SharedPreferences.
 *
 * Se utiliza principalmente para:
 * - Guardar email y contraseña
 * - Recuperar credenciales para login automático (biometría)
 * - Verificar si existen credenciales almacenadas
 * - Limpiar datos guardados
 */
@Singleton
class Prefs @Inject constructor(@ApplicationContext context: Context) {

    /**
     * Instancia de SharedPreferences privada de la app.
     */
    private val prefs: SharedPreferences =
        context.getSharedPreferences("physio_prefs", Context.MODE_PRIVATE)

    /**
     * Guarda las credenciales del usuario.
     *
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     */
    fun saveCredentials(email: String, password: String) {
        prefs.edit()
            .putString("email", email)
            .putString("password", password)
            .apply()
    }

    /**
     * Obtiene el email almacenado.
     *
     * @return Email o cadena vacía si no existe
     */
    fun getEmail(): String = prefs.getString("email", "") ?: ""

    /**
     * Obtiene la contraseña almacenada.
     *
     * @return Contraseña o cadena vacía si no existe
     */
    fun getPassword(): String = prefs.getString("password", "") ?: ""

    /**
     * Verifica si existen credenciales guardadas.
     *
     * @return true si hay email almacenado, false en caso contrario
     */
    fun hasCredentials(): Boolean = getEmail().isNotEmpty()

    /**
     * Elimina todas las credenciales almacenadas.
     */
    fun clearCredentials() = prefs.edit().clear().apply()
}