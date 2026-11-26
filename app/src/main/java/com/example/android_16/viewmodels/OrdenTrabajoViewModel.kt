package com.example.android_16.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_16.data.local.AppDatabase
import com.example.android_16.data.local.SessionManager
import com.example.android_16.data.repository.OrdenTrabajoRepository
import com.example.android_16.data.repository.SesionExpiradaException
import com.example.android_16.model.CurrentWeather
import com.example.android_16.model.OrdenTrabajo
import com.example.android_16.model.OrdenTrabajoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdenUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val ordenSeleccionada: OrdenTrabajo? = null,
    val climaActual: CurrentWeather? = null,
    val cargandoClima: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
    val sesionExpirada: Boolean = false
)

class OrdenTrabajoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(application)
    private val repository = OrdenTrabajoRepository(database.ordenTrabajoDao(), sessionManager)

    val ordenes: StateFlow<List<OrdenTrabajo>> = repository.observarOrdenes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(OrdenUiState())
    val uiState: StateFlow<OrdenUiState> = _uiState.asStateFlow()

    private fun manejarFallo(e: Throwable, fallback: String) {
        if (e is SesionExpiradaException) {
            _uiState.update {
                it.copy(
                    sesionExpirada = true,
                    error = e.message
                )
            }
        } else {
            _uiState.update {
                it.copy(error = e.message ?: fallback)
            }
        }
    }

    fun sincronizarOrdenes() {
        if (_uiState.value.isSyncing) return
        _uiState.update { it.copy(isSyncing = true, error = null) }

        viewModelScope.launch {
            try {
                val result = repository.sincronizarOrdenes()
                result.onFailure { e ->
                    manejarFallo(e, "No se pudo sincronizar")
                }
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun cargarOrdenPorId(id: String) {
        viewModelScope.launch {
            val orden = repository.obtenerOrdenLocal(id)
                ?: ordenes.value.find { it.id == id }
            _uiState.update {
                it.copy(
                    ordenSeleccionada = orden,
                    climaActual = null
                )
            }
            if (orden?.latitud != null && orden.longitud != null) {
                consultarClima(orden.latitud, orden.longitud)
            }
        }
    }

    fun crearOrden(
        codigo: String,
        titulo: String,
        descripcion: String,
        local: String,
        tecnicoId: String?,
        tecnicoNombre: String,
        latitud: Double?,
        longitud: Double?,
        fotoBytes: ByteArray?,
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.isLoading) return

        if (codigo.isBlank() || titulo.isBlank() || local.isBlank()) {
            _uiState.update { it.copy(error = "Completa código, título y local") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, mensaje = null) }

        viewModelScope.launch {
            var creadaConExito = false
            try {
                var fotoUrl: String? = null
                if (fotoBytes != null) {
                    val uploadResult = repository.subirEvidencia(fotoBytes, codigo)
                    uploadResult.fold(
                        onSuccess = { url -> fotoUrl = url },
                        onFailure = { e ->
                            manejarFallo(e, "No se pudo subir la foto")
                            return@launch
                        }
                    )
                }

                val request = OrdenTrabajoRequest(
                    codigo = codigo.trim(),
                    titulo = titulo.trim(),
                    descripcion = descripcion.trim(),
                    local = local.trim(),
                    estado = "Pendiente",
                    tecnicoId = tecnicoId,
                    tecnicoNombre = tecnicoNombre,
                    latitud = latitud,
                    longitud = longitud,
                    fotoUrl = fotoUrl,
                    firmadoCliente = false
                )

                val result = repository.crearOrden(request)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(mensaje = "Orden creada correctamente")
                        }
                        creadaConExito = true
                    },
                    onFailure = { e ->
                        manejarFallo(e, "Error al crear la orden")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }

            if (creadaConExito) {
                onSuccess()
            }
        }
    }

    fun actualizarEstado(ordenId: String, nuevoEstado: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val result = repository.actualizarOrden(
                    id = ordenId,
                    cambios = mapOf("estado" to nuevoEstado)
                )
                result.fold(
                    onSuccess = { actualizada ->
                        _uiState.update {
                            it.copy(
                                ordenSeleccionada = actualizada,
                                mensaje = "Estado actualizado a $nuevoEstado"
                            )
                        }
                    },
                    onFailure = { e ->
                        manejarFallo(e, "Error al actualizar estado")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun subirYGuardarEvidencia(orden: OrdenTrabajo, imageBytes: ByteArray) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val uploadResult = repository.subirEvidencia(imageBytes, orden.codigo)
                uploadResult.fold(
                    onSuccess = { url ->
                        val updateResult = repository.actualizarOrden(
                            id = orden.id,
                            cambios = mapOf("foto_url" to url)
                        )
                        updateResult.fold(
                            onSuccess = { actualizada ->
                                _uiState.update {
                                    it.copy(
                                        ordenSeleccionada = actualizada,
                                        mensaje = "Evidencia fotográfica guardada"
                                    )
                                }
                            },
                            onFailure = { e ->
                                manejarFallo(e, "Foto subida, pero falló guardar enlace")
                            }
                        )
                    },
                    onFailure = { e ->
                        manejarFallo(e, "Error al subir la evidencia")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun actualizarUbicacion(orden: OrdenTrabajo, latitud: Double, longitud: Double) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val result = repository.actualizarOrden(
                    id = orden.id,
                    cambios = mapOf(
                        "latitud" to latitud,
                        "longitud" to longitud
                    )
                )
                result.fold(
                    onSuccess = { actualizada ->
                        _uiState.update {
                            it.copy(
                                ordenSeleccionada = actualizada,
                                mensaje = "Ubicación GPS registrada"
                            )
                        }
                        consultarClima(latitud, longitud)
                    },
                    onFailure = { e ->
                        manejarFallo(e, "Error al guardar ubicación")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun firmarOrdenComoCliente(orden: OrdenTrabajo) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val result = repository.actualizarOrden(
                    id = orden.id,
                    cambios = mapOf(
                        "firmado_cliente" to true,
                        "estado" to "Completada"
                    )
                )
                result.fold(
                    onSuccess = { actualizada ->
                        _uiState.update {
                            it.copy(
                                ordenSeleccionada = actualizada,
                                mensaje = "Conformidad firmada por el cliente"
                            )
                        }
                    },
                    onFailure = { e ->
                        manejarFallo(e, "Error al firmar la orden")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun eliminarOrden(ordenId: String, onSuccess: () -> Unit) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            var eliminadaConExito = false
            try {
                val result = repository.eliminarOrden(ordenId)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                ordenSeleccionada = null,
                                mensaje = "Orden eliminada"
                            )
                        }
                        eliminadaConExito = true
                    },
                    onFailure = { e ->
                        manejarFallo(e, "Error al eliminar la orden")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }

            if (eliminadaConExito) {
                onSuccess()
            }
        }
    }

    fun consultarClima(latitud: Double, longitud: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoClima = true) }
            val result = repository.obtenerClima(latitud, longitud)
            result.fold(
                onSuccess = { clima ->
                    _uiState.update {
                        it.copy(
                            cargandoClima = false,
                            climaActual = clima
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(cargandoClima = false) }
                }
            )
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensaje = null, error = null) }
    }

    fun limpiarCacheAlSalir() {
        viewModelScope.launch {
            repository.limpiarCacheLocal()
            _uiState.value = OrdenUiState()
        }
    }
}