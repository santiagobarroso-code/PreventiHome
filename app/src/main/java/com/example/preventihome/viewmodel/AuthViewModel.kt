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

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Correo y contraseña son obligatorios")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error desconocido") }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginWithGoogle(idToken)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error con Google") }
        }
    }

    fun loginWithBiometrics() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginWithSavedCredentials()
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error("Error biométrico: ${it.message}") }
        }
    }

    fun hasSavedCredentials(): Boolean = authRepository.hasSavedCredentials()
    fun resetState() { _uiState.value = AuthUiState.Idle }
    fun logout() { authRepository.logout() }


    fun register(email: String, password: String, nombre: String) {
        if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
            _uiState.value = AuthUiState.Error("Todos los campos son obligatorios")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email, password, nombre)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error al registrar") }
        }
    }
}