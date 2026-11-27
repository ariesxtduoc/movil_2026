package com.example.android_16

import com.example.android_16.data.local.OrdenTrabajoDao
import com.example.android_16.data.local.SessionManager
import com.example.android_16.data.remote.SupabaseApiService
import com.example.android_16.data.repository.OrdenTrabajoRepository
import com.example.android_16.data.repository.SesionExpiradaException
import com.example.android_16.model.OrdenTrabajo
import com.example.android_16.model.OrdenTrabajoRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class OrdenTrabajoRepositoryTest {

    private lateinit var dao: OrdenTrabajoDao
    private lateinit var sessionManager: SessionManager
    private lateinit var supabaseApi: SupabaseApiService
    private lateinit var repository: OrdenTrabajoRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        supabaseApi = mockk(relaxed = true)
        every { sessionManager.obtenerToken() } returns "token-activo"

        repository = OrdenTrabajoRepository(
            dao = dao,
            sessionManager = sessionManager,
            supabaseApi = supabaseApi
        )
    }

    @Test
    fun `sincronizarOrdenes exitoso reemplaza cache local en Room`() = runTest {
        val ordenesRemotas = listOf(
            mockk<OrdenTrabajo>(relaxed = true) { every { id } returns "ot-1" },
            mockk<OrdenTrabajo>(relaxed = true) { every { id } returns "ot-2" }
        )
        coEvery {
            supabaseApi.obtenerOrdenes(bearerToken = "Bearer token-activo", select = any(), order = any())
        } returns Response.success(ordenesRemotas)

        val resultado = repository.sincronizarOrdenes()

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { dao.limpiarTodo() }
        coVerify(exactly = 1) { dao.insertarTodas(match { it.size == 2 }) }
    }

    @Test
    fun `sincronizarOrdenes con error 401 retorna SesionExpiradaException`() = runTest {
        val errorBody = """{"message":"JWT expired"}""".toResponseBody("application/json".toMediaType())
        coEvery {
            supabaseApi.obtenerOrdenes(bearerToken = any(), select = any(), order = any())
        } returns Response.error(401, errorBody)

        val resultado = repository.sincronizarOrdenes()

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is SesionExpiradaException)
    }

    @Test
    fun `sincronizarOrdenes sin conexion retorna fallback offline`() = runTest {
        coEvery {
            supabaseApi.obtenerOrdenes(bearerToken = any(), select = any(), order = any())
        } throws RuntimeException("Unable to resolve host")

        val resultado = repository.sincronizarOrdenes()

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("Sin conexión") == true)
    }

    @Test
    fun `crearOrden exitoso guarda en DAO y retorna orden creada`() = runTest {
        val request = mockk<OrdenTrabajoRequest>(relaxed = true) {
            every { tecnicoId } returns "tec-1"
        }
        val ordenCreada = mockk<OrdenTrabajo>(relaxed = true) {
            every { id } returns "ot-nueva"
            every { codigo } returns "OT-9999"
        }

        coEvery {
            supabaseApi.crearOrden(bearerToken = "Bearer token-activo", orden = any(), prefer = any())
        } returns Response.success(listOf(ordenCreada))

        val resultado = repository.crearOrden(request)

        assertTrue(resultado.isSuccess)
        assertEquals("OT-9999", resultado.getOrNull()?.codigo)
        coVerify(exactly = 1) { dao.insertarUna(any()) }
    }

    @Test
    fun `eliminarOrden exitoso borra registro del DAO local`() = runTest {
        coEvery {
            supabaseApi.eliminarOrden(bearerToken = "Bearer token-activo", idFilter = "eq.ot-55")
        } returns Response.success(Unit)

        val resultado = repository.eliminarOrden("ot-55")

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { dao.eliminarPorId("ot-55") }
    }

    @Test
    fun `subirEvidencia exitoso retorna URL publica de Storage`() = runTest {
        val bodyVacio = "".toResponseBody("application/json".toMediaType())
        coEvery {
            supabaseApi.subirEvidencia(
                bearerToken = "Bearer token-activo",
                fileName = any(),
                imageBody = any(),
                contentType = any(),
                upsert = any()
            )
        } returns Response.success(bodyVacio)

        val resultado = repository.subirEvidencia(byteArrayOf(10, 20, 30), "OT-1234")

        assertTrue(resultado.isSuccess)
        assertTrue(resultado.getOrNull()?.contains("ot_OT-1234_") == true)
    }
}