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

sealed class ProgresoUiState {
    object Idle : ProgresoUiState()
    object Loading : ProgresoUiState()
    object Guardado : ProgresoUiState()
    data class Historial(val lista: List<Progreso>) : ProgresoUiState()
    data class Error(val message: String) : ProgresoUiState()
}

@HiltViewModel
class ProgresoViewModel @Inject constructor(
    private val repository: ProgresoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgresoUiState>(ProgresoUiState.Idle)
    val uiState: StateFlow<ProgresoUiState> = _uiState.asStateFlow()

    fun guardarProgreso(progreso: Progreso) {
        viewModelScope.launch {
            _uiState.value = ProgresoUiState.Loading
            repository.guardarProgreso(progreso)
                .onSuccess { _uiState.value = ProgresoUiState.Guardado }
                .onFailure { _uiState.value = ProgresoUiState.Error(it.message ?: "Error al guardar") }
        }
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            _uiState.value = ProgresoUiState.Loading
            repository.getHistorial()
                .onSuccess { _uiState.value = ProgresoUiState.Historial(it) }
                .onFailure { _uiState.value = ProgresoUiState.Error(it.message ?: "Error al cargar") }
        }
    }

    fun resetState() { _uiState.value = ProgresoUiState.Idle }
}