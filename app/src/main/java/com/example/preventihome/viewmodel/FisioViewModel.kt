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
 * Estados posibles de la UI para el módulo de fisioterapeuta.
 */
sealed class FisioUiState {

    /** Estado de carga */
    object Loading : FisioUiState()

    /** Lista de pacientes cargada exitosamente */
    data class Success(val pacientes: List<User>) : FisioUiState()

    /** Error al obtener los datos */
    data class Error(val message: String) : FisioUiState()
}

/**
 * ViewModel encargado de la lógica del fisioterapeuta.
 *
 * Maneja:
 * - Obtención de pacientes desde el repositorio
 * - Exposición de estados a la UI mediante StateFlow
 */
@HiltViewModel
class FisioViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    /** Estado interno mutable */
    private val _uiState = MutableStateFlow<FisioUiState>(FisioUiState.Loading)

    /** Estado expuesto a la UI */
    val uiState: StateFlow<FisioUiState> = _uiState.asStateFlow()

    /**
     * Bloque de inicialización.
     *
     * Se ejecuta automáticamente al crear el ViewModel
     * y carga la lista de pacientes.
     */
    init {
        cargarPacientes()
    }

    /**
     * Obtiene la lista de pacientes desde el repositorio.
     *
     * Actualiza el estado de la UI:
     * - Loading
     * - Success con datos
     * - Error en caso de fallo
     */
    fun cargarPacientes() {
        viewModelScope.launch {

            _uiState.value = FisioUiState.Loading

            userRepository.getPacientes()
                .onSuccess { _uiState.value = FisioUiState.Success(it) }
                .onFailure {
                    _uiState.value = FisioUiState.Error(
                        it.message ?: "Error al cargar pacientes"
                    )
                }
        }
    }
}