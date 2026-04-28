package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.repository.CitaRepository
import com.example.preventihome.domain.model.Cita
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la UI de citas.
 */
sealed class CitaUiState {
    object Idle : CitaUiState()
    object Loading : CitaUiState()
    data class Success(val citas: List<Cita>) : CitaUiState()
    object OperacionExitosa : CitaUiState()
    data class Error(val message: String) : CitaUiState()
}

/**
 * ViewModel compartido para la gestión de citas.
 * Usado tanto por el paciente (agendar/ver) como
 * por el fisioterapeuta (ver pendientes/atender).
 *
 * @param repository Repositorio inyectado por Hilt
 */
@HiltViewModel
class CitaViewModel @Inject constructor(
    private val repository: CitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CitaUiState>(CitaUiState.Idle)
    val uiState: StateFlow<CitaUiState> = _uiState.asStateFlow()

    /**
     * Agenda una nueva cita para el paciente autenticado.
     * Valida que el motivo no esté vacío antes de guardar.
     *
     * @param cita Objeto con los datos de la cita
     */
    fun agendarCita(cita: Cita) {
        if (cita.motivo.isBlank()) {
            _uiState.value = CitaUiState.Error("El motivo de la cita es obligatorio")
            return
        }
        viewModelScope.launch {
            _uiState.value = CitaUiState.Loading
            repository.agendarCita(cita)
                .onSuccess { _uiState.value = CitaUiState.OperacionExitosa }
                .onFailure {
                    _uiState.value = CitaUiState.Error(
                        it.message ?: "Error al agendar cita"
                    )
                }
        }
    }

    /**
     * Carga las citas del paciente autenticado.
     */
    fun cargarMisCitas() {
        viewModelScope.launch {
            _uiState.value = CitaUiState.Loading
            repository.getMisCitas()
                .onSuccess { _uiState.value = CitaUiState.Success(it) }
                .onFailure {
                    _uiState.value = CitaUiState.Error(
                        it.message ?: "Error al cargar citas"
                    )
                }
        }
    }

    /**
     * Carga todas las citas pendientes para el fisioterapeuta.
     */
    fun cargarCitasPendientes() {
        viewModelScope.launch {
            _uiState.value = CitaUiState.Loading
            repository.getCitasPendientes()
                .onSuccess { _uiState.value = CitaUiState.Success(it) }
                .onFailure {
                    _uiState.value = CitaUiState.Error(
                        it.message ?: "Error al cargar citas pendientes"
                    )
                }
        }
    }

    /**
     * Marca una cita como atendida vinculándola a una consulta.
     *
     * @param citaId     ID de la cita atendida
     * @param consultaId ID de la consulta creada
     */
    fun marcarComoAtendida(citaId: String, consultaId: String) {
        viewModelScope.launch {
            repository.marcarComoAtendida(citaId, consultaId)
                .onSuccess { _uiState.value = CitaUiState.OperacionExitosa }
                .onFailure {
                    _uiState.value = CitaUiState.Error(
                        it.message ?: "Error al actualizar cita"
                    )
                }
        }
    }

    /**
     * Cancela una cita.
     *
     * @param citaId ID de la cita a cancelar
     */
    fun cancelarCita(citaId: String) {
        viewModelScope.launch {
            _uiState.value = CitaUiState.Loading
            repository.cancelarCita(citaId)
                .onSuccess { _uiState.value = CitaUiState.OperacionExitosa }
                .onFailure {
                    _uiState.value = CitaUiState.Error(
                        it.message ?: "Error al cancelar cita"
                    )
                }
        }
    }

    fun resetState() { _uiState.value = CitaUiState.Idle }
}