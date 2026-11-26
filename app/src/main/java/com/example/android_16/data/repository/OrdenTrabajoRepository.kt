package com.example.android_16.data.repository

import com.example.android_16.data.local.OrdenTrabajoDao
import com.example.android_16.data.local.SessionManager
import com.example.android_16.data.local.toEntity
import com.example.android_16.data.local.toModel
import com.example.android_16.data.remote.RetrofitClient
import com.example.android_16.data.remote.SupabaseConfig
import com.example.android_16.model.CurrentWeather
import com.example.android_16.model.OrdenTrabajo
import com.example.android_16.model.OrdenTrabajoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class OrdenTrabajoRepository(
    private val dao: OrdenTrabajoDao,
    private val sessionManager: SessionManager
) {
    private val supabaseApi = RetrofitClient.supabaseApi
    private val weatherApi = RetrofitClient.weatherApi

    private fun obtenerBearer(): String {
        val token = sessionManager.obtenerToken() ?: SupabaseConfig.SUPABASE_KEY
        return "Bearer $token"
    }

    fun observarOrdenes(): Flow<List<OrdenTrabajo>> {
        return dao.observarOrdenes().map { lista ->
            lista.map { it.toModel() }
        }
    }

    suspend fun obtenerOrdenLocal(id: String): OrdenTrabajo? = withContext(Dispatchers.IO) {
        dao.obtenerPorId(id)?.toModel()
    }

    suspend fun sincronizarOrdenes(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseApi.obtenerOrdenes(bearerToken = obtenerBearer())
            if (response.isSuccessful) {
                val remotas = response.body().orEmpty()
                dao.limpiarTodo()
                dao.insertarTodas(remotas.map { it.toEntity() })
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudieron sincronizar las órdenes (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión: mostrando datos guardados localmente"))
        }
    }

    suspend fun crearOrden(request: OrdenTrabajoRequest): Result<OrdenTrabajo> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseApi.crearOrden(
                bearerToken = obtenerBearer(),
                orden = request
            )
            if (response.isSuccessful) {
                val creada = response.body()?.firstOrNull()
                    ?: return@withContext Result.failure(Exception("Supabase no devolvió la orden creada"))
                dao.insertarUna(creada.toEntity())
                Result.success(creada)
            } else {
                Result.failure(Exception("Error al crear la orden (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red al crear orden: ${e.localizedMessage}"))
        }
    }

    suspend fun actualizarOrden(
        id: String,
        cambios: Map<String, Any?>
    ): Result<OrdenTrabajo> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseApi.actualizarOrden(
                bearerToken = obtenerBearer(),
                idFilter = "eq.$id",
                cambios = cambios
            )
            if (response.isSuccessful) {
                val actualizada = response.body()?.firstOrNull()
                    ?: return@withContext Result.failure(Exception("No se recibió la orden actualizada"))
                dao.insertarUna(actualizada.toEntity())
                Result.success(actualizada)
            } else {
                Result.failure(Exception("Error al actualizar la orden (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red al actualizar: ${e.localizedMessage}"))
        }
    }

    suspend fun eliminarOrden(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseApi.eliminarOrden(
                bearerToken = obtenerBearer(),
                idFilter = "eq.$id"
            )
            if (response.isSuccessful) {
                dao.eliminarPorId(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar la orden (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red al eliminar: ${e.localizedMessage}"))
        }
    }

    suspend fun subirEvidencia(
        imageBytes: ByteArray,
        ordenCodigo: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val codigoLimpio = ordenCodigo.replace(Regex("[^A-Za-z0-9_-]"), "_")
            val fileName = "ot_${codigoLimpio}_${System.currentTimeMillis()}.jpg"
            val body = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            val response = supabaseApi.subirEvidencia(
                bearerToken = obtenerBearer(),
                fileName = fileName,
                imageBody = body
            )

            if (response.isSuccessful) {
                Result.success(SupabaseConfig.buildPublicFotoUrl(fileName))
            } else {
                Result.failure(Exception("Error al subir evidencia (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red al subir la foto: ${e.localizedMessage}"))
        }
    }

    suspend fun obtenerClima(
        latitud: Double,
        longitud: Double
    ): Result<CurrentWeather> = withContext(Dispatchers.IO) {
        try {
            val response = weatherApi.obtenerClimaActual(latitud, longitud)
            val clima = response.body()?.currentWeather
            if (response.isSuccessful && clima != null) {
                Result.success(clima)
            } else {
                Result.failure(Exception("No se pudo obtener el clima actual"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión para consultar clima"))
        }
    }

    suspend fun limpiarCacheLocal() = withContext(Dispatchers.IO) {
        dao.limpiarTodo()
    }
}