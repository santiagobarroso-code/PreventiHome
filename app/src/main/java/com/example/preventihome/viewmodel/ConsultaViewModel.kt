package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.repository.ConsultaRepository
import com.example.preventihome.domain.model.Consulta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la UI de consultas.
 * Compartido entre las pantallas del fisioterapeuta y del paciente.
 */
sealed class ConsultaUiState {
    /** Estado inicial antes de cualquier operación */
    object Idle : ConsultaUiState()
    /** Cargando datos de Firestore */
    object Loading : ConsultaUiState()
    /** Lista de consultas cargada correctamente */
    data class Success(val consultas: List<Consulta>) : ConsultaUiState()
    /** Operación de escritura completada (crear, actualizar, cerrar) */
    object OperacionExitosa : ConsultaUiState()
    /** Error con mensaje descriptivo */
    data class Error(val message: String) : ConsultaUiState()
}

/**
 * ViewModel compartido para la gestión de consultas.
 *
 * Usado tanto por el fisioterapeuta (crear/editar consultas)
 * como por el paciente (ver sus consultas y diagnósticos).
 *
 * Arquitectura: ViewModel → ConsultaRepository → Firestore
 *
 * @param repository Repositorio inyectado por Hilt
 */
@HiltViewModel
class ConsultaViewModel @Inject constructor(
    private val repository: ConsultaRepository
) : ViewModel() {

    /** Estado observable de la UI */
    private val _uiState = MutableStateFlow<ConsultaUiState>(ConsultaUiState.Idle)
    val uiState: StateFlow<ConsultaUiState> = _uiState.asStateFlow()

    /**
     * Carga las consultas de un paciente específico.
     * Usado por el fisioterapeuta en el panel de historial del paciente.
     *
     * @param pacienteId UID del paciente seleccionado
     */
    fun cargarConsultasPaciente(pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = ConsultaUiState.Loading
            repository.getConsultasPorPaciente(pacienteId)
                .onSuccess { _uiState.value = ConsultaUiState.Success(it) }
                .onFailure {
                    _uiState.value = ConsultaUiState.Error(
                        it.message ?: "Error al cargar consultas"
                    )
                }
        }
    }

    /**
     * Carga las consultas del paciente autenticado.
     * Usado en el home del paciente para ver su historial clínico.
     */
    fun cargarMisConsultas() {
        viewModelScope.launch {
            _uiState.value = ConsultaUiState.Loading
            repository.getMisConsultas()
                .onSuccess { _uiState.value = ConsultaUiState.Success(it) }
                .onFailure {
                    _uiState.value = ConsultaUiState.Error(
                        it.message ?: "Error al cargar tus consultas"
                    )
                }
        }
    }

    /**
     * Crea una nueva consulta para un paciente.
     * Valida que los campos obligatorios no estén vacíos antes de guardar.
     *
     * @param consulta Objeto con todos los datos de la consulta
     */
    fun crearConsulta(consulta: Consulta) {
        if (consulta.diagnostico.isBlank() || consulta.patologia.isBlank()) {
            _uiState.value = ConsultaUiState.Error(
                "El diagnóstico y la patología son obligatorios"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = ConsultaUiState.Loading
            repository.crearConsulta(consulta)
                .onSuccess { _uiState.value = ConsultaUiState.OperacionExitosa }
                .onFailure {
                    _uiState.value = ConsultaUiState.Error(
                        it.message ?: "Error al crear consulta"
                    )
                }
        }
    }

    /**
     * Actualiza las notas de seguimiento de una consulta existente.
     *
     * @param consultaId ID de la consulta en Firestore
     * @param notas      Nuevas notas del fisioterapeuta
     */
    fun actualizarNotas(consultaId: String, notas: String) {
        viewModelScope.launch {
            _uiState.value = ConsultaUiState.Loading
            repository.actualizarNotas(consultaId, notas)
                .onSuccess { _uiState.value = ConsultaUiState.OperacionExitosa }
                .onFailure {
                    _uiState.value = ConsultaUiState.Error(
                        it.message ?: "Error al actualizar notas"
                    )
                }
        }
    }

    /**
     * Cierra una consulta marcándola como finalizada.
     * Una consulta cerrada sigue visible pero ya no se puede editar.
     *
     * @param consultaId ID de la consulta a cerrar
     */
    fun cerrarConsulta(consultaId: String) {
        viewModelScope.launch {
            _uiState.value = ConsultaUiState.Loading
            repository.cerrarConsulta(consultaId)
                .onSuccess { _uiState.value = ConsultaUiState.OperacionExitosa }
                .onFailure {
                    _uiState.value = ConsultaUiState.Error(
                        it.message ?: "Error al cerrar consulta"
                    )
                }
        }
    }

    /** Resetea el estado a Idle para evitar reacciones duplicadas */
    fun resetState() { _uiState.value = ConsultaUiState.Idle }
}