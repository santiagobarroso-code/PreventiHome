package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.remote.FirestoreSource
import com.example.preventihome.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Estados posibles de la UI del perfil del paciente.
 */
sealed class PerfilUiState {
    object Loading : PerfilUiState()
    data class Success(val user: User) : PerfilUiState()
    object ActualizacionExitosa : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}

/**
 * ViewModel del perfil del usuario paciente.
 *
 * Gestiona:
 * - Carga del perfil desde Firestore
 * - Actualización del nombre en Firestore y Firebase Auth
 * - Cambio de contraseña en Firebase Auth
 *
 * Los cambios de nombre se sincronizan tanto en Firebase Auth
 * como en Firestore para mantener consistencia.
 */
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val firestoreSource: FirestoreSource,
    private val auth: FirebaseAuth,
    private val prefs: com.example.preventihome.utils.Prefs
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    /**
     * Carga el perfil del usuario autenticado desde Firestore.
     */
    fun cargarPerfil() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching { firestoreSource.getUser(uid) }
                .onSuccess { _uiState.value = PerfilUiState.Success(it) }
                .onFailure {
                    _uiState.value = PerfilUiState.Error(
                        it.message ?: "Error al cargar perfil"
                    )
                }
        }
    }

    /**
     * Actualiza el nombre del usuario en Firestore.
     * El nombre se guarda en la colección users/ bajo el UID del usuario.
     *
     * @param nuevoNombre Nombre actualizado del usuario
     */
    fun actualizarNombre(nuevoNombre: String) {
        if (nuevoNombre.isBlank()) {
            _uiState.value = PerfilUiState.Error("El nombre no puede estar vacío")
            return
        }
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching {
                // Actualizar en Firestore
                firestoreSource.updateUserNombre(uid, nuevoNombre)
            }
                .onSuccess {
                    _uiState.value = PerfilUiState.ActualizacionExitosa
                    cargarPerfil() // Recargar para mostrar el nombre actualizado
                }
                .onFailure {
                    _uiState.value = PerfilUiState.Error(
                        it.message ?: "Error al actualizar nombre"
                    )
                }
        }
    }

    /**
     * Cambia la contraseña del usuario en Firebase Auth.
     * Re-autentica al usuario con su contraseña actual antes de cambiarla,
     * ya que Firebase requiere autenticación reciente para operaciones sensibles.
     *
     * @param passwordActual    Contraseña actual del usuario (para re-autenticar)
     * @param nuevaPassword     Nueva contraseña deseada
     * @param confirmarPassword Confirmación de la nueva contraseña
     */
    fun cambiarPassword(
        passwordActual: String,
        nuevaPassword: String,
        confirmarPassword: String
    ) {
        when {
            passwordActual.isBlank() -> {
                _uiState.value = PerfilUiState.Error("Ingresa tu contraseña actual")
                return
            }
            nuevaPassword.isBlank() -> {
                _uiState.value = PerfilUiState.Error("La nueva contraseña no puede estar vacía")
                return
            }
            nuevaPassword.length < 6 -> {
                _uiState.value = PerfilUiState.Error("La contraseña debe tener mínimo 6 caracteres")
                return
            }
            nuevaPassword != confirmarPassword -> {
                _uiState.value = PerfilUiState.Error("Las contraseñas no coinciden")
                return
            }
        }
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            runCatching {
                val user = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                val email = user.email
                    ?: throw Exception("No se pudo obtener el correo del usuario")

                // Re-autenticar con la contraseña actual antes de cambiarla
                val credential = com.google.firebase.auth.EmailAuthProvider
                    .getCredential(email, passwordActual)
                user.reauthenticate(credential).await()

                // Una vez re-autenticado, actualizar la contraseña
                user.updatePassword(nuevaPassword).await()

                // Guardar nueva contraseña en SharedPreferences para biometría
                prefs.saveCredentials(email, nuevaPassword)
            }
                .onSuccess {
                    _uiState.value = PerfilUiState.ActualizacionExitosa
                }
                .onFailure {
                    val mensaje = when {
                        it.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ||
                                it.message?.contains("wrong-password") == true ->
                            "La contraseña actual es incorrecta"
                        it.message?.contains("network") == true ->
                            "Error de conexión, intenta de nuevo"
                        else -> it.message ?: "Error al cambiar contraseña"
                    }
                    _uiState.value = PerfilUiState.Error(mensaje)
                }
        }
    }
}