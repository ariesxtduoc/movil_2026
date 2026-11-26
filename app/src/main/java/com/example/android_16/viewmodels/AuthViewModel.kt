package com.example.android_16.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_16.data.local.SessionManager
import com.example.android_16.data.repository.AuthRepository
import com.example.android_16.model.Perfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val perfil: Perfil? = null,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = AuthRepository(sessionManager)

    private val _uiState = MutableStateFlow(
        AuthUiState(
            perfil = repository.obtenerPerfilActual(),
            isLoggedIn = repository.haySesionActiva()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa correo y contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { perfil ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            perfil = perfil,
                            isLoggedIn = true,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al iniciar sesión"
                        )
                    }
                }
            )
        }
    }

    fun registrar(nombre: String, email: String, password: String, rol: String) {
        if (nombre.isBlank() || email.isBlank() || password.length < 6) {
            _uiState.update {
                it.copy(error = "Completa todos los campos (contraseña mínimo 6 caracteres)")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.registrar(nombre, email, password, rol)
            result.fold(
                onSuccess = { perfil ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            perfil = perfil,
                            isLoggedIn = true,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al registrar usuario"
                        )
                    }
                }
            )
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _uiState.value = AuthUiState(
            isLoading = false,
            perfil = null,
            isLoggedIn = false,
            error = null
        )
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}