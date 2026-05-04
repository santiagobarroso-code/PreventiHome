package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.repository.AuthRepository
import com.example.preventihome.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la UI para autenticación.
 */
sealed class AuthUiState {

    /** Estado inicial (sin acción) */
    object Idle : AuthUiState()

    /** Estado de carga */
    object Loading : AuthUiState()

    /** Autenticación exitosa */
    data class Success(val user: User) : AuthUiState()

    /** Error en autenticación */
    data class Error(val message: String) : AuthUiState()
}

/**
 * ViewModel encargado de la lógica de autenticación.
 *
 * Maneja:
 * - Login con email y contraseña
 * - Login con Google
 * - Login con biometría
 * - Registro de usuarios
 * - Manejo de sesión
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Estado interno mutable */
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    /** Estado expuesto a la UI */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Verifica si existe una sesión activa.
     *
     * Si el usuario está autenticado:
     * - Intenta iniciar sesión automáticamente con credenciales guardadas
     * - En caso de error, cierra sesión
     */
    fun checkSession() {
        if (authRepository.isLoggedIn) {
            viewModelScope.launch {

                _uiState.value = AuthUiState.Loading

                authRepository.loginWithSavedCredentials()
                    .onSuccess { _uiState.value = AuthUiState.Success(it) }
                    .onFailure { authRepository.logout() }
            }
        }
    }

    /**
     * Login con correo y contraseña.
     *
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     */
    fun loginWithEmail(email: String, password: String) {

        // Validación básica
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Correo y contraseña son obligatorios")
            return
        }

        viewModelScope.launch {

            _uiState.value = AuthUiState.Loading

            authRepository.loginWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure {
                    _uiState.value = AuthUiState.Error(
                        it.message ?: "Error desconocido"
                    )
                }
        }
    }

    /**
     * Login con Google.
     *
     * @param idToken Token de autenticación de Google
     */
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {

            _uiState.value = AuthUiState.Loading

            authRepository.loginWithGoogle(idToken)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure {
                    _uiState.value = AuthUiState.Error(
                        it.message ?: "Error con Google"
                    )
                }
        }
    }

    /**
     * Login usando biometría.
     *
     * Utiliza credenciales previamente guardadas.
     */
    fun loginWithBiometrics() {
        viewModelScope.launch {

            _uiState.value = AuthUiState.Loading

            authRepository.loginWithSavedCredentials()
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure {
                    _uiState.value = AuthUiState.Error(
                        "Error biométrico: ${it.message}"
                    )
                }
        }
    }

    /**
     * Verifica si existen credenciales guardadas localmente.
     */
    fun hasSavedCredentials(): Boolean =
        authRepository.hasSavedCredentials()

    /**
     * Reinicia el estado de la UI a Idle.
     */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    /**
     * Cierra la sesión del usuario.
     */
    fun logout() {
        authRepository.logout()
    }

    /**
     * Registro de nuevo usuario.
     *
     * @param email Correo
     * @param password Contraseña
     * @param nombre Nombre del usuario
     */
    fun register(email: String, password: String, nombre: String) {

        // Validación básica
        if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
            _uiState.value = AuthUiState.Error("Todos los campos son obligatorios")
            return
        }

        viewModelScope.launch {

            _uiState.value = AuthUiState.Loading

            authRepository.register(email, password, nombre)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure {
                    _uiState.value = AuthUiState.Error(
                        it.message ?: "Error al registrar"
                    )
                }
        }
    }
}