package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.repository.EjercicioRepository
import com.example.preventihome.domain.model.Ejercicio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EjercicioUiState {
    object Loading : EjercicioUiState()
    data class Success(val ejercicios: List<Ejercicio>) : EjercicioUiState()
    data class Error(val message: String) : EjercicioUiState()
}

@HiltViewModel
class EjercicioViewModel @Inject constructor(
    private val repository: EjercicioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EjercicioUiState>(EjercicioUiState.Loading)
    val uiState: StateFlow<EjercicioUiState> = _uiState.asStateFlow()

    // Lista completa sin filtrar (para restaurar al quitar filtros)
    private var todosLosEjercicios: List<Ejercicio> = emptyList()

    // Zona seleccionada actualmente (null = todas)
    private var zonaSeleccionada: String? = null

    init {
        cargarEjercicios()
    }

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

    fun filtrarPorZona(zona: String?) {
        zonaSeleccionada = zona
        aplicarFiltro()
    }

    fun filtrarRecomendados() {
        _uiState.value = EjercicioUiState.Success(
            todosLosEjercicios.filter { it.chefRecomendado }
        )
    }

    private fun aplicarFiltro() {
        val resultado = if (zonaSeleccionada == null) {
            todosLosEjercicios
        } else {
            todosLosEjercicios.filter { it.zona == zonaSeleccionada }
        }
        _uiState.value = EjercicioUiState.Success(resultado)
    }

    // StateFlow para el detalle de un ejercicio individual
    private val _ejercicioDetalle = MutableStateFlow<Ejercicio?>(null)
    val ejercicioDetalle: StateFlow<Ejercicio?> = _ejercicioDetalle.asStateFlow()

    fun cargarEjercicioById(id: String) {
        viewModelScope.launch {
            repository.getEjercicioById(id)
                .onSuccess { _ejercicioDetalle.value = it }
                .onFailure { _uiState.value = EjercicioUiState.Error(it.message ?: "Error") }
        }
    }
}