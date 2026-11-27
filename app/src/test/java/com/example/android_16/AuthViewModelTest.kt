package com.example.android_16

import android.app.Application
import android.content.SharedPreferences
import com.example.android_16.data.repository.AuthRepository
import com.example.android_16.model.Perfil
import com.example.android_16.model.RolUsuario
import com.example.android_16.viewmodel.AuthViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        val prefs = mockk<SharedPreferences>(relaxed = true) {
            every { getString(any(), any()) } returns null
        }
        val app = mockk<Application>(relaxed = true) {
            every { getSharedPreferences(any(), any()) } returns prefs
        }
        mockRepository = mockk(relaxed = true)
        viewModel = AuthViewModel(app).apply {
            repository = mockRepository
        }
    }

    @Test
    fun `validaciones de nombre email y password detectan campos vacios o cortos`() {
        assertNotNull(viewModel.validarEmail(""))
        assertNotNull(viewModel.validarEmail("correo-sin-arroba"))
        assertNull(viewModel.validarEmail("tecnico@acis.cl"))

        assertNotNull(viewModel.validarPassword(""))
        assertNotNull(viewModel.validarPassword("123"))
        assertNull(viewModel.validarPassword("123456"))

        assertNotNull(viewModel.validarNombre("Jo"))
        assertNull(viewModel.validarNombre("José Pérez"))
    }

    @Test
    fun `login con datos invalidos marca errores en uiState sin llamar al repositorio`() = runTest {
        viewModel.login("correo-invalido", "12")

        val state = viewModel.uiState.value
        assertNotNull(state.errorEmail)
        assertNotNull(state.errorPassword)
        assertFalse(state.isLoggedIn)
        coVerify(exactly = 0) { mockRepository.login(any(), any()) }
    }

    @Test
    fun `login exitoso actualiza perfil y isLoggedIn a true`() = runTest {
        val perfil = Perfil(id = "1", email = "admin@acis.cl", nombre = "Admin", rol = "admin")
        coEvery { mockRepository.login("admin@acis.cl", "123456") } returns Result.success(perfil)

        viewModel.login("admin@acis.cl", "123456")

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertEquals(RolUsuario.ADMIN, state.perfil?.rolEnum)
        assertTrue(state.perfil?.puedeEliminarOrdenes == true)
        assertNull(state.error)
    }

    @Test
    fun `privilegios de los 4 roles se evaluan correctamente en Perfil`() {
        val admin = Perfil(rol = "admin")
        val supervisor = Perfil(rol = "supervisor")
        val tecnico = Perfil(rol = "tecnico")
        val cliente = Perfil(rol = "cliente")

        assertTrue(admin.esOperativo && admin.puedeEliminarOrdenes && admin.puedeFirmarOrdenes)
        assertTrue(supervisor.esOperativo && !supervisor.puedeEliminarOrdenes && !supervisor.puedeFirmarOrdenes)
        assertTrue(tecnico.esOperativo && !tecnico.puedeEliminarOrdenes && !tecnico.puedeFirmarOrdenes)
        assertTrue(!cliente.esOperativo && !cliente.puedeEliminarOrdenes && cliente.puedeFirmarOrdenes)
    }

    @Test
    fun `recuperarPassword exitoso limpia errores y muestra mensaje de confirmacion`() = runTest {
        coEvery { mockRepository.recuperarPassword("tec@acis.cl", "clave123") } returns Result.success(Unit)
        var callbackEjecutado = false

        viewModel.recuperarPassword("tec@acis.cl", "clave123") {
            callbackEjecutado = true
        }

        assertTrue(callbackEjecutado)
        assertNotNull(viewModel.uiState.value.mensaje)
        assertNull(viewModel.uiState.value.errorEmail)
    }

    @Test
    fun `cerrarSesion limpia el perfil y deja isLoggedIn en false`() {
        viewModel.cerrarSesion("Sesión cerrada por prueba")

        val state = viewModel.uiState.value
        assertFalse(state.isLoggedIn)
        assertNull(state.perfil)
        assertEquals("Sesión cerrada por prueba", state.error)
    }
}