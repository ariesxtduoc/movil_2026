package com.example.android_16

import com.example.android_16.data.local.SessionManager
import com.example.android_16.data.remote.SupabaseApiService
import com.example.android_16.data.repository.AuthRepository
import com.example.android_16.data.repository.SesionExpiradaException
import com.example.android_16.model.AuthResponse
import com.example.android_16.model.Perfil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest {

    private lateinit var sessionManager: SessionManager
    private lateinit var api: SupabaseApiService
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        sessionManager = mockk(relaxed = true)
        api = mockk(relaxed = true)
        repository = AuthRepository(sessionManager = sessionManager, api = api)
    }

    @Test
    fun `login exitoso guarda token y perfil en SessionManager`() = runTest {
        val perfilEsperado = Perfil(
            id = "user-123",
            email = "admin@acis.cl",
            nombre = "Carlos Admin",
            rol = "admin"
        )
        val authResponse = mockk<AuthResponse> {
            every { accessToken } returns "jwt-token-valido"
            every { user } returns mockk {
                every { id } returns "user-123"
            }
        }

        coEvery { api.login(any()) } returns Response.success(authResponse)
        coEvery {
            api.obtenerPerfil(bearerToken = "Bearer jwt-token-valido", idFilter = "eq.user-123", select = any())
        } returns Response.success(listOf(perfilEsperado))

        val resultado = repository.login("admin@acis.cl", "123456")

        assertTrue(resultado.isSuccess)
        assertEquals(perfilEsperado, resultado.getOrNull())
        verify(exactly = 1) { sessionManager.guardarSesion("jwt-token-valido", perfilEsperado) }
    }

    @Test
    fun `login con credenciales invalidas retorna Result failure`() = runTest {
        val errorBody = """{"error":"invalid_grant"}""".toResponseBody("application/json".toMediaType())
        coEvery { api.login(any()) } returns Response.error(400, errorBody)

        val resultado = repository.login("malo@acis.cl", "wrongpass")

        assertTrue(resultado.isFailure)
        verify(exactly = 0) { sessionManager.guardarSesion(any(), any()) }
    }

    @Test
    fun `registrar crea perfil con rol supervisor y guarda sesion`() = runTest {
        val perfilCreado = Perfil(
            id = "sup-01",
            email = "sup@acis.cl",
            nombre = "Ana Supervisor",
            rol = "supervisor"
        )
        val authResponse = mockk<AuthResponse> {
            every { accessToken } returns "jwt-sup"
            every { user } returns mockk {
                every { id } returns "sup-01"
            }
        }

        coEvery { api.signUp(any()) } returns Response.success(authResponse)
        coEvery {
            api.crearPerfil(bearerToken = "Bearer jwt-sup", perfil = any(), prefer = any())
        } returns Response.success(listOf(perfilCreado))

        val resultado = repository.registrar("Ana Supervisor", "sup@acis.cl", "123456", "supervisor")

        assertTrue(resultado.isSuccess)
        assertEquals("supervisor", resultado.getOrNull()?.rol)
        verify(exactly = 1) { sessionManager.guardarSesion("jwt-sup", perfilCreado) }
    }

    @Test
    fun `recuperarPassword retorna exito cuando RPC encuentra el correo`() = runTest {
        coEvery { api.restablecerClaveRpc(any()) } returns Response.success(true)

        val resultado = repository.recuperarPassword("tecnico@acis.cl", "nueva123")

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) {
            api.restablecerClaveRpc(
                match { it["p_email"] == "tecnico@acis.cl" && it["p_nueva_clave"] == "nueva123" }
            )
        }
    }

    @Test
    fun `recuperarPassword retorna failure cuando el correo no existe`() = runTest {
        coEvery { api.restablecerClaveRpc(any()) } returns Response.success(false)

        val resultado = repository.recuperarPassword("noexiste@acis.cl", "nueva123")

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("No existe") == true)
    }

    @Test
    fun `actualizarPerfil con respuesta 401 retorna SesionExpiradaException`() = runTest {
        every { sessionManager.obtenerToken() } returns "token-expirado"
        every { sessionManager.obtenerPerfil() } returns Perfil(id = "u1", email = "u@acis.cl")

        val errorBody = """{"message":"JWT expired"}""".toResponseBody("application/json".toMediaType())
        coEvery {
            api.actualizarPerfil(bearerToken = any(), idFilter = any(), cambios = any(), prefer = any())
        } returns Response.error(401, errorBody)

        val resultado = repository.actualizarPerfil("Nuevo Nombre", "admin")

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is SesionExpiradaException)
    }
}