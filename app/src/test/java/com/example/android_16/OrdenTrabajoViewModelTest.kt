package com.example.android_16

import android.app.Application
import android.content.SharedPreferences
import com.example.android_16.data.repository.OrdenTrabajoRepository
import com.example.android_16.data.repository.SesionExpiradaException
import com.example.android_16.model.CurrentWeather
import com.example.android_16.model.OrdenTrabajo
import com.example.android_16.viewmodel.OrdenTrabajoViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdenTrabajoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: OrdenTrabajoRepository
    private lateinit var viewModel: OrdenTrabajoViewModel

    @Before
    fun setup() {
        val prefs = mockk<SharedPreferences>(relaxed = true) {
            every { getString(any(), any()) } returns null
        }
        val app = mockk<Application>(relaxed = true) {
            every { getSharedPreferences(any(), any()) } returns prefs
        }
        mockRepository = mockk(relaxed = true)
        every { mockRepository.observarOrdenes() } returns flowOf(emptyList())

        viewModel = OrdenTrabajoViewModel(app).apply {
            repository = mockRepository
        }
    }

    @Test
    fun `crearOrden con campos obligatorios vacios marca errores y no llama al repositorio`() = runTest {
        viewModel.crearOrden(
            codigo = "",
            titulo = "ab",
            descripcion = "",
            local = "",
            tecnicoId = "t1",
            tecnicoNombre = "Técnico",
            latitud = null,
            longitud = null,
            fotoBytes = null,
            onSuccess = {}
        )

        val state = viewModel.uiState.value
        assertNotNull(state.errorCodigo)
        assertNotNull(state.errorTitulo)
        assertNotNull(state.errorLocal)
        coVerify(exactly = 0) { mockRepository.crearOrden(any()) }
    }

    @Test
    fun `crearOrden valida sube foto crea orden y ejecuta onSuccess`() = runTest {
        val bytesFoto = byteArrayOf(1, 2, 3)
        val ordenCreada = mockk<OrdenTrabajo>(relaxed = true)
        var exitoLlamado = false

        coEvery { mockRepository.subirEvidencia(bytesFoto, "OT-1001") } returns Result.success("https://img.test/ot.jpg")
        coEvery { mockRepository.crearOrden(any()) } returns Result.success(ordenCreada)

        viewModel.crearOrden(
            codigo = "OT-1001",
            titulo = "Mantención aire acondicionado",
            descripcion = "Revisión de filtros",
            local = "Sucursal Arauco",
            tecnicoId = "t1",
            tecnicoNombre = "Técnico 1",
            latitud = -37.246,
            longitud = -73.317,
            fotoBytes = bytesFoto,
            onSuccess = { exitoLlamado = true }
        )

        assertTrue(exitoLlamado)
        assertNull(viewModel.uiState.value.errorCodigo)
        assertEquals("Orden creada correctamente", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `actualizarEstado actualiza ordenSeleccionada y mensaje`() = runTest {
        val actualizada = mockk<OrdenTrabajo>(relaxed = true) {
            every { id } returns "ot-10"
            every { estado } returns "En Proceso"
        }
        coEvery {
            mockRepository.actualizarOrden("ot-10", match { it["estado"] == "En Proceso" })
        } returns Result.success(actualizada)

        viewModel.actualizarEstado("ot-10", "En Proceso")

        assertEquals(actualizada, viewModel.uiState.value.ordenSeleccionada)
        assertEquals("Estado actualizado a En Proceso", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `actualizarUbicacion guarda coordenadas y consulta clima automaticamente`() = runTest {
        val orden = mockk<OrdenTrabajo>(relaxed = true) { every { id } returns "ot-1" }
        val ordenActualizada = mockk<OrdenTrabajo>(relaxed = true) {
            every { id } returns "ot-1"
            every { latitud } returns -37.25
            every { longitud } returns -73.32
        }
        val climaMock = mockk<CurrentWeather>(relaxed = true) {
            every { temperature } returns 18.5
        }

        coEvery { mockRepository.actualizarOrden("ot-1", any()) } returns Result.success(ordenActualizada)
        coEvery { mockRepository.obtenerClima(-37.25, -73.32) } returns Result.success(climaMock)

        viewModel.actualizarUbicacion(orden, -37.25, -73.32)

        val state = viewModel.uiState.value
        assertEquals(ordenActualizada, state.ordenSeleccionada)
        assertEquals(climaMock, state.climaActual)
    }

    @Test
    fun `firmarOrdenComoCliente marca conformidad y estado Completada`() = runTest {
        val orden = mockk<OrdenTrabajo>(relaxed = true) { every { id } returns "ot-2" }
        val firmada = mockk<OrdenTrabajo>(relaxed = true) {
            every { id } returns "ot-2"
            every { firmadoCliente } returns true
            every { estado } returns "Completada"
        }

        coEvery {
            mockRepository.actualizarOrden(
                "ot-2",
                match { it["firmado_cliente"] == true && it["estado"] == "Completada" }
            )
        } returns Result.success(firmada)

        viewModel.firmarOrdenComoCliente(orden)

        assertEquals(firmada, viewModel.uiState.value.ordenSeleccionada)
        assertEquals("Conformidad firmada por el cliente", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `eliminarOrden exitoso limpia ordenSeleccionada y ejecuta onSuccess`() = runTest {
        var callbackEjecutado = false
        coEvery { mockRepository.eliminarOrden("ot-99") } returns Result.success(Unit)

        viewModel.eliminarOrden("ot-99") {
            callbackEjecutado = true
        }

        assertTrue(callbackEjecutado)
        assertNull(viewModel.uiState.value.ordenSeleccionada)
        assertEquals("Orden eliminada", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `sincronizarOrdenes ante SesionExpiradaException activa bandera sesionExpirada`() = runTest {
        coEvery { mockRepository.sincronizarOrdenes() } returns Result.failure(SesionExpiradaException())

        viewModel.sincronizarOrdenes()

        assertTrue(viewModel.uiState.value.sesionExpirada)
    }
}