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

sealed class FisioUiState {
    object Loading : FisioUiState()
    data class Success(val pacientes: List<User>) : FisioUiState()
    data class Error(val message: String) : FisioUiState()
}

@HiltViewModel
class FisioViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FisioUiState>(FisioUiState.Loading)
    val uiState: StateFlow<FisioUiState> = _uiState.asStateFlow()

    init {
        cargarPacientes()
    }

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