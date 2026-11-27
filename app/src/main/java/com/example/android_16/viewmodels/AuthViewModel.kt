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
    val isRecovering: Boolean = false,
    val perfil: Perfil? = null,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val errorNombre: String? = null,
    val errorEmail: String? = null,
    val errorPassword: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    internal var repository: AuthRepository = AuthRepository(sessionManager)

    private val _uiState = MutableStateFlow(
        AuthUiState(
            perfil = repository.obtenerPerfilActual(),
            isLoggedIn = repository.haySesionActiva()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun validarNombre(nombre: String): String? {
        return when {
            nombre.isBlank() -> "El nombre completo es obligatorio"
            nombre.trim().length < 3 -> "Ingresa al menos 3 caracteres"
            else -> null
        }
    }

    fun validarEmail(email: String): String? {
        val limpio = email.trim()
        return when {
            limpio.isBlank() -> "El correo electrónico es obligatorio"
            !limpio.contains("@") || !limpio.substringAfter("@").contains(".") ->
                "Ingresa un correo electrónico válido"
            else -> null
        }
    }

    fun validarPassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }
    }

    fun limpiarErrorCampo(campo: String) {
        _uiState.update {
            when (campo) {
                "nombre" -> it.copy(errorNombre = null, error = null)
                "email" -> it.copy(errorEmail = null, error = null)
                "password" -> it.copy(errorPassword = null, error = null)
                else -> it.copy(error = null)
            }
        }
    }

    fun limpiarTodosLosErrores() {
        _uiState.update {
            it.copy(
                error = null,
                mensaje = null,
                errorNombre = null,
                errorEmail = null,
                errorPassword = null
            )
        }
    }

    fun login(email: String, password: String) {
        val errEmail = validarEmail(email)
        val errPass = validarPassword(password)

        if (errEmail != null || errPass != null) {
            _uiState.update {
                it.copy(
                    errorEmail = errEmail,
                    errorPassword = errPass
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mensaje = null) }
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { perfil ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            perfil = perfil,
                            isLoggedIn = true,
                            error = null,
                            errorEmail = null,
                            errorPassword = null
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
        val errNombre = validarNombre(nombre)
        val errEmail = validarEmail(email)
        val errPass = validarPassword(password)

        if (errNombre != null || errEmail != null || errPass != null) {
            _uiState.update {
                it.copy(
                    errorNombre = errNombre,
                    errorEmail = errEmail,
                    errorPassword = errPass
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mensaje = null) }
            val result = repository.registrar(nombre, email, password, rol)
            result.fold(
                onSuccess = { perfil ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            perfil = perfil,
                            isLoggedIn = true,
                            error = null,
                            errorNombre = null,
                            errorEmail = null,
                            errorPassword = null
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

    fun recuperarPassword(email: String, onSuccess: () -> Unit) {
        val errEmail = validarEmail(email)
        if (errEmail != null) {
            _uiState.update { it.copy(errorEmail = errEmail) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRecovering = true, error = null, mensaje = null) }
            val result = repository.recuperarPassword(email)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isRecovering = false,
                            mensaje = "Si el correo existe, recibirás un enlace de recuperación."
                        )
                    }
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isRecovering = false,
                            error = e.message ?: "No se pudo procesar la recuperación"
                        )
                    }
                }
            )
        }
    }

    fun actualizarPerfil(nombre: String, rol: String, onSuccess: (Perfil) -> Unit) {
        val errNombre = validarNombre(nombre)
        if (errNombre != null) {
            _uiState.update { it.copy(errorNombre = errNombre) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mensaje = null) }
            val result = repository.actualizarPerfil(nombre, rol)
            result.fold(
                onSuccess = { actualizado ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            perfil = actualizado,
                            errorNombre = null,
                            mensaje = "Perfil actualizado correctamente"
                        )
                    }
                    onSuccess(actualizado)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "No se pudo actualizar el perfil"
                        )
                    }
                }
            )
        }
    }

    fun cerrarSesion(motivo: String? = null) {
        repository.cerrarSesion()
        _uiState.value = AuthUiState(
            isLoading = false,
            perfil = null,
            isLoggedIn = false,
            error = motivo
        )
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }
}