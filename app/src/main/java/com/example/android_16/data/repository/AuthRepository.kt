package com.example.android_16.data.repository

import com.example.android_16.data.local.SessionManager
import com.example.android_16.data.remote.RetrofitClient
import com.example.android_16.data.remote.SupabaseApiService
import com.example.android_16.model.AuthRequest
import com.example.android_16.model.Perfil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val sessionManager: SessionManager,
    private val api: SupabaseApiService = RetrofitClient.supabaseApi
) {
    suspend fun login(email: String, password: String): Result<Perfil> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(AuthRequest(email.trim(), password))
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Credenciales incorrectas o cuenta no encontrada (${response.code()})")
                )
            }

            val authBody = response.body()
            val token = authBody?.accessToken
            val userId = authBody?.user?.id

            if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Respuesta de autenticación incompleta"))
            }

            val bearer = "Bearer $token"
            val perfilResponse = api.obtenerPerfil(bearerToken = bearer, idFilter = "eq.$userId")
            val perfil = perfilResponse.body()?.firstOrNull()
                ?: Perfil(
                    id = userId,
                    email = email.trim(),
                    nombre = email.substringBefore("@"),
                    rol = "tecnico"
                )

            sessionManager.guardarSesion(token, perfil)
            Result.success(perfil)
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al iniciar sesión: ${e.localizedMessage}"))
        }
    }

    suspend fun registrar(
        nombre: String,
        email: String,
        password: String,
        rol: String
    ): Result<Perfil> = withContext(Dispatchers.IO) {
        try {
            val response = api.signUp(AuthRequest(email.trim(), password))
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("No se pudo registrar el usuario (${response.code()})")
                )
            }

            val authBody = response.body()
            val token = authBody?.accessToken
            val userId = authBody?.user?.id

            if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                return@withContext Result.failure(
                    Exception("Registro incompleto. Verifica que la confirmación de correo esté desactivada.")
                )
            }

            val bearer = "Bearer $token"
            val nuevoPerfil = Perfil(
                id = userId,
                email = email.trim(),
                nombre = nombre.trim(),
                rol = rol
            )

            val crearResponse = api.crearPerfil(bearerToken = bearer, perfil = nuevoPerfil)
            val perfilGuardado = if (crearResponse.isSuccessful) {
                crearResponse.body()?.firstOrNull() ?: nuevoPerfil
            } else {
                api.obtenerPerfil(bearerToken = bearer, idFilter = "eq.$userId")
                    .body()?.firstOrNull() ?: nuevoPerfil
            }

            sessionManager.guardarSesion(token, perfilGuardado)
            Result.success(perfilGuardado)
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al registrar: ${e.localizedMessage}"))
        }
    }

    suspend fun recuperarPassword(email: String, nuevaPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.restablecerClaveRpc(
                mapOf(
                    "p_email" to email.trim(),
                    "p_nueva_clave" to nuevaPassword
                )
            )
            if (response.isSuccessful) {
                if (response.body() == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("No existe una cuenta registrada con ese correo"))
                }
            } else {
                Result.failure(Exception("No se pudo restablecer la contraseña (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al restablecer contraseña: ${e.localizedMessage}"))
        }
    }

    suspend fun actualizarPerfil(
        nombre: String,
        rol: String,
        nuevaPassword: String = ""
    ): Result<Perfil> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.obtenerToken()?.takeIf { it.isNotBlank() }
                ?: return@withContext Result.failure(SesionExpiradaException())
            val perfilActual = sessionManager.obtenerPerfil()
                ?: return@withContext Result.failure(SesionExpiradaException())

            val response = api.actualizarPerfil(
                bearerToken = "Bearer $token",
                idFilter = "eq.${perfilActual.id}",
                cambios = mapOf(
                    "nombre" to nombre.trim(),
                    "rol" to rol
                )
            )

            if (!response.isSuccessful) {
                return@withContext if (response.code() == 401) {
                    Result.failure(SesionExpiradaException())
                } else {
                    Result.failure(Exception("Error al actualizar perfil (${response.code()})"))
                }
            }

            if (nuevaPassword.isNotBlank()) {
                val passResponse = api.actualizarPasswordUsuario(
                    bearerToken = "Bearer $token",
                    body = mapOf("password" to nuevaPassword)
                )
                if (!passResponse.isSuccessful) {
                    return@withContext if (passResponse.code() == 401) {
                        Result.failure(SesionExpiradaException())
                    } else {
                        Result.failure(Exception("Perfil guardado, pero falló el cambio de clave (${passResponse.code()})"))
                    }
                }
            }

            val actualizado = response.body()?.firstOrNull()
                ?: perfilActual.copy(nombre = nombre.trim(), rol = rol)
            sessionManager.guardarSesion(token, actualizado)
            Result.success(actualizado)
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión al actualizar perfil: ${e.localizedMessage}"))
        }
    }

    fun obtenerPerfilActual(): Perfil? = sessionManager.obtenerPerfil()

    fun haySesionActiva(): Boolean = sessionManager.haySesionActiva()

    fun cerrarSesion() {
        sessionManager.cerrarSesion()
    }
}