package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.repository.ConsultaRepository
import com.example.preventihome.data.repository.EjercicioRepository
import com.example.preventihome.domain.model.Ejercicio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la UI de ejercicios.
 */
sealed class EjercicioUiState {
    object Loading : EjercicioUiState()
    data class Success(val ejercicios: List<Ejercicio>) : EjercicioUiState()
    data class Error(val message: String) : EjercicioUiState()
}

/**
 * ViewModel para la gestión de ejercicios.
 *
 * Responsabilidades:
 * - Cargar ejercicios desde Firestore
 * - Aplicar filtros por zona corporal o por patología
 * - Detectar la patología activa del paciente desde su consulta más reciente
 * - Gestionar el estado de carga para el detalle de un ejercicio individual
 *
 * @param repository         Repositorio de ejercicios
 * @param consultaRepository Repositorio de consultas (para obtener patología activa)
 */
@HiltViewModel
class EjercicioViewModel @Inject constructor(
    private val repository: EjercicioRepository,
    private val consultaRepository: ConsultaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EjercicioUiState>(EjercicioUiState.Loading)
    val uiState: StateFlow<EjercicioUiState> = _uiState.asStateFlow()

    /** Lista completa sin filtrar para restaurar al quitar filtros */
    private var todosLosEjercicios: List<Ejercicio> = emptyList()

    /** Zona seleccionada actualmente (null = todas) */
    private var zonaSeleccionada: String? = null

    /** Patología activa del paciente (null = no tiene consulta activa) */
    private var _patologiaActiva = MutableStateFlow<String?>(null)
    val patologiaActiva: StateFlow<String?> = _patologiaActiva.asStateFlow()

    /** StateFlow para el detalle de un ejercicio individual */
    private val _ejercicioDetalle = MutableStateFlow<Ejercicio?>(null)
    val ejercicioDetalle: StateFlow<Ejercicio?> = _ejercicioDetalle.asStateFlow()

    init {
        cargarEjercicios()
        cargarPatologiaActiva()
    }

    /**
     * Carga todos los ejercicios activos desde Firestore.
     * También detecta la patología activa del paciente para
     * mostrar el chip de recomendación por patología.
     */
    fun cargarEjercicios() {
        viewModelScope.launch {
            _uiState.value = EjercicioUiState.Loading
            repository.getEjercicios()
                .onSuccess { lista ->
                    todosLosEjercicios = lista
                    aplicarFiltro()
                }
                .onFailure {
                    _uiState.value = EjercicioUiState.Error(
                        it.message ?: "Error al cargar ejercicios"
                    )
                }
        }
    }

    /**
     * Detecta la patología activa del paciente desde su consulta más reciente.
     * Si tiene una consulta activa, expone la patología para mostrar
     * el chip de recomendación personalizada en la UI.
     */
    private fun cargarPatologiaActiva() {
        viewModelScope.launch {
            consultaRepository.getConsultaActivaActual()
                .onSuccess { consulta ->
                    _patologiaActiva.value = consulta?.patologia
                }
                .onFailure {
                    // Si no se puede cargar, simplemente no hay patología activa
                    _patologiaActiva.value = null
                }
        }
    }

    /**
     * Filtra ejercicios por zona corporal.
     * @param zona Zona a filtrar, o null para mostrar todos
     */
    fun filtrarPorZona(zona: String?) {
        zonaSeleccionada = zona
        aplicarFiltro()
    }

    /**
     * Filtra ejercicios por la patología activa del paciente.
     * Llama a Firestore usando whereArrayContains para encontrar
     * ejercicios que incluyan la patología en su array.
     *
     * @param patologia Patología de la consulta activa
     */
    fun filtrarPorPatologia(patologia: String) {
        viewModelScope.launch {
            _uiState.value = EjercicioUiState.Loading
            repository.getEjerciciosPorPatologia(patologia)
                .onSuccess { lista ->
                    _uiState.value = EjercicioUiState.Success(lista)
                }
                .onFailure {
                    _uiState.value = EjercicioUiState.Error(
                        it.message ?: "Error al filtrar por patología"
                    )
                }
        }
    }

    /** Filtra los ejercicios marcados como recomendados por el chef */
    fun filtrarRecomendados() {
        _uiState.value = EjercicioUiState.Success(
            todosLosEjercicios.filter { it.chefRecomendado }
        )
    }

    /** Aplica el filtro de zona actual sobre la lista completa */
    private fun aplicarFiltro() {
        val resultado = if (zonaSeleccionada == null) {
            todosLosEjercicios
        } else {
            todosLosEjercicios.filter { it.zona == zonaSeleccionada }
        }
        _uiState.value = EjercicioUiState.Success(resultado)
    }

    /**
     * Carga un ejercicio específico por su ID para la pantalla de detalle.
     * @param id ID del documento en Firestore
     */
    fun cargarEjercicioById(id: String) {
        viewModelScope.launch {
            repository.getEjercicioById(id)
                .onSuccess { _ejercicioDetalle.value = it }
                .onFailure {
                    _uiState.value = EjercicioUiState.Error(
                        it.message ?: "Error al cargar ejercicio"
                    )
                }
        }
    }
}