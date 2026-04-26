package com.example.preventihome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preventihome.data.remote.FirestoreSource
import com.example.preventihome.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PerfilUiState {
    object Loading : PerfilUiState()
    data class Success(val user: User) : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val firestoreSource: FirestoreSource,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargarPerfil() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching { firestoreSource.getUser(uid) }
                .onSuccess { _uiState.value = PerfilUiState.Success(it) }
                .onFailure {
                    _uiState.value = PerfilUiState.Error(
                        it.message ?: "Error al cargar perfil"
                    )
                }
        }
    }
}