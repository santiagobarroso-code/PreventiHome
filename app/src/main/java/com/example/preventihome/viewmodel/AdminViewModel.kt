package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.repository.UserRepository
import com.example.preventihome.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la UI del panel de administrador.
 * Sigue el patrón sealed class para manejo exhaustivo en el Fragment.
 */
sealed class AdminUiState {
    /** Estado inicial o después de resetear */
    object Idle : AdminUiState()
    /** Cargando datos de Firestore */
    object Loading : AdminUiState()
    /** Lista de usuarios cargada correctamente */
    data class Success(val usuarios: List<User>) : AdminUiState()
    /** Operación completada exitosamente (crear/promover/revocar) */
    object OperacionExitosa : AdminUiState()
    /** Error con mensaje descriptivo */
    data class Error(val message: String) : AdminUiState()
}

/**
 * ViewModel del panel de administrador.
 * Gestiona la lista de usuarios y las operaciones de gestión de roles.
 *
 * Responsabilidades:
 * - Cargar todos los usuarios de la plataforma
 * - Promover pacientes a fisioterapeutas
 * - Revocar rol de fisioterapeuta
 * - Crear nuevos usuarios (fisio o paciente)
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    /** Estado observable de la UI */
    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarios()
    }

    /**
     * Carga todos los usuarios registrados en la plataforma.
     * Actualiza el estado con la lista completa ordenada por rol.
     */
    fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            userRepository.getAllUsers()
                .onSuccess { lista ->
                    // Ordenar: admins primero, luego fisios, luego pacientes
                    val ordenados = lista.sortedWith(
                        compareBy { when (it.rol) {
                            "admin"  -> 0
                            "fisio"  -> 1
                            else     -> 2
                        }}
                    )
                    _uiState.value = AdminUiState.Success(ordenados)
                }
                .onFailure {
                    _uiState.value = AdminUiState.Error(
                        it.message ?: "Error al cargar usuarios"
                    )
                }
        }
    }

    /**
     * Promueve a un paciente al rol de fisioterapeuta.
     * @param uid UID del usuario a promover
     */
    fun promoverAFisio(uid: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            userRepository.promoverAFisio(uid)
                .onSuccess {
                    _uiState.value = AdminUiState.OperacionExitosa
                    cargarUsuarios() // Recargar lista actualizada
                }
                .onFailure {
                    _uiState.value = AdminUiState.Error(
                        it.message ?: "Error al promover usuario"
                    )
                }
        }
    }

    /**
     * Revoca el rol de fisioterapeuta y regresa al usuario a paciente.
     * @param uid UID del usuario a revocar
     */
    fun revocarFisio(uid: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            userRepository.revocarFisio(uid)
                .onSuccess {
                    _uiState.value = AdminUiState.OperacionExitosa
                    cargarUsuarios()
                }
                .onFailure {
                    _uiState.value = AdminUiState.Error(
                        it.message ?: "Error al revocar rol"
                    )
                }
        }
    }


    /**
     * Crea un nuevo usuario desde el panel de administrador.
     * Después de crear exitosamente recarga la lista de usuarios.
     *
     * @param nombre   Nombre completo del usuario
     * @param email    Correo electrónico
     * @param password Contraseña temporal
     * @param rol      Rol a asignar: "paciente" o "fisio"
     */
    fun crearUsuario(nombre: String, email: String, password: String, rol: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            userRepository.crearUsuario(email, password, nombre, rol)
                .onSuccess {
                    _uiState.value = AdminUiState.OperacionExitosa
                    cargarUsuarios()
                }
                .onFailure {
                    _uiState.value = AdminUiState.Error(
                        it.message ?: "Error al crear usuario"
                    )
                }
        }
    }

    /** Resetea el estado a Idle después de manejar una operación */
    fun resetState() { _uiState.value = AdminUiState.Idle }
}