package com.example.android_16.data.remote

import com.example.android_16.model.AuthRequest
import com.example.android_16.model.AuthResponse
import com.example.android_16.model.OrdenTrabajo
import com.example.android_16.model.OrdenTrabajoRequest
import com.example.android_16.model.Perfil
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseApiService {

    // --- AUTENTICACIÓN ---
    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("auth/v1/recover")
    suspend fun recuperarPassword(
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    // --- PERFILES ---
    @GET("rest/v1/perfiles")
    suspend fun obtenerPerfil(
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<Perfil>>

    @POST("rest/v1/perfiles")
    suspend fun crearPerfil(
        @Header("Authorization") bearerToken: String,
        @Body perfil: Perfil,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<Perfil>>

    @PATCH("rest/v1/perfiles")
    suspend fun actualizarPerfil(
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String,
        @Body cambios: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<Perfil>>

    // --- ÓRDENES DE TRABAJO (CRUD) ---
    @GET("rest/v1/ordenes_trabajo")
    suspend fun obtenerOrdenes(
        @Header("Authorization") bearerToken: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<OrdenTrabajo>>

    @POST("rest/v1/ordenes_trabajo")
    suspend fun crearOrden(
        @Header("Authorization") bearerToken: String,
        @Body orden: OrdenTrabajoRequest,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<OrdenTrabajo>>

    @PATCH("rest/v1/ordenes_trabajo")
    suspend fun actualizarOrden(
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String,
        @Body cambios: Map<String, @JvmSuppressWildcards Any?>,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<OrdenTrabajo>>

    @DELETE("rest/v1/ordenes_trabajo")
    suspend fun eliminarOrden(
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String
    ): Response<Unit>

    // --- STORAGE (FOTOS DE EVIDENCIA) ---
    @POST("storage/v1/object/${SupabaseConfig.STORAGE_BUCKET}/{fileName}")
    suspend fun subirEvidencia(
        @Header("Authorization") bearerToken: String,
        @Path("fileName") fileName: String,
        @Body imageBody: RequestBody,
        @Header("Content-Type") contentType: String = "image/jpeg",
        @Header("x-upsert") upsert: String = "true"
    ): Response<ResponseBody>
}