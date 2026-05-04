package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.repository.ProgresoRepository
import com.example.preventihome.domain.model.Progreso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la UI relacionados con el progreso del usuario.
 */
sealed class ProgresoUiState {

    /** Estado inicial */
    object Idle : ProgresoUiState()

    /** Estado de carga */
    object Loading : ProgresoUiState()

    /** Progreso guardado exitosamente */
    object Guardado : ProgresoUiState()

    /** Historial cargado */
    data class Historial(val lista: List<Progreso>) : ProgresoUiState()

    /** Error en cualquier operación */
    data class Error(val message: String) : ProgresoUiState()
}

/**
 * ViewModel encargado de la gestión del progreso del usuario.
 *
 * Maneja:
 * - Guardado de ejercicios realizados
 * - Obtención del historial de progreso
 * - Exposición de estados a la UI mediante StateFlow
 */
@HiltViewModel
class ProgresoViewModel @Inject constructor(
    private val repository: ProgresoRepository
) : ViewModel() {

    /** Estado interno mutable */
    private val _uiState = MutableStateFlow<ProgresoUiState>(ProgresoUiState.Idle)

    /** Estado expuesto a la UI */
    val uiState: StateFlow<ProgresoUiState> = _uiState.asStateFlow()

    /**
     * Guarda el progreso de un ejercicio realizado.
     *
     * @param progreso Objeto Progreso con la información del ejercicio
     */
    fun guardarProgreso(progreso: Progreso) {
        viewModelScope.launch {

            _uiState.value = ProgresoUiState.Loading

            repository.guardarProgreso(progreso)
                .onSuccess {
                    _uiState.value = ProgresoUiState.Guardado
                }
                .onFailure {
                    _uiState.value = ProgresoUiState.Error(
                        it.message ?: "Error al guardar"
                    )
                }
        }
    }

    /**
     * Obtiene el historial de progreso del usuario.
     */
    fun cargarHistorial() {
        viewModelScope.launch {

            _uiState.value = ProgresoUiState.Loading

            repository.getHistorial()
                .onSuccess {
                    _uiState.value = ProgresoUiState.Historial(it)
                }
                .onFailure {
                    _uiState.value = ProgresoUiState.Error(
                        it.message ?: "Error al cargar"
                    )
                }
        }
    }

    /**
     * Reinicia el estado de la UI a Idle.
     *
     * Útil después de completar acciones como guardar progreso
     * para evitar reacciones duplicadas en la UI.
     */
    fun resetState() {
        _uiState.value = ProgresoUiState.Idle
    }
}