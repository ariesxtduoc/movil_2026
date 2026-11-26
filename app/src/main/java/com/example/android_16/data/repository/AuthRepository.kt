package com.example.android_16.data.repository

import com.example.android_16.data.local.SessionManager
import com.example.android_16.data.remote.RetrofitClient
import com.example.android_16.model.AuthRequest
import com.example.android_16.model.Perfil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val sessionManager: SessionManager
) {
    private val api = RetrofitClient.supabaseApi

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

    fun obtenerPerfilActual(): Perfil? = sessionManager.obtenerPerfil()

    fun haySesionActiva(): Boolean = sessionManager.haySesionActiva()

    fun cerrarSesion() {
        sessionManager.cerrarSesion()
    }
}