package com.example.android_16.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_16.model.RolUsuario
import com.example.android_16.ui.screens.ClienteScreen
import com.example.android_16.ui.screens.DetalleOTScreen
import com.example.android_16.ui.screens.LoginScreen
import com.example.android_16.ui.screens.NuevaOTScreen
import com.example.android_16.ui.screens.TecnicoScreen
import com.example.android_16.viewmodel.AuthViewModel
import com.example.android_16.viewmodel.OrdenTrabajoViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Tecnico : Screen("tecnico")
    data object Cliente : Screen("cliente")
    data object NuevaOT : Screen("nueva_ot")
    data object DetalleOT : Screen("detalle_ot/{ordenId}") {
        fun createRoute(ordenId: String) = "detalle_ot/$ordenId"
    }
}

private fun destinoPorRol(rol: String?): String {
    return if (RolUsuario.fromClave(rol) == RolUsuario.CLIENTE) {
        Screen.Cliente.route
    } else {
        Screen.Tecnico.route
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    ordenViewModel: OrdenTrabajoViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()
    val ordenState by ordenViewModel.uiState.collectAsState()

    val startDestination = remember {
        if (authState.isLoggedIn && authState.perfil != null) {
            destinoPorRol(authState.perfil!!.rol)
        } else {
            Screen.Login.route
        }
    }

    val cerrarSesionYVolverALogin: (String?) -> Unit = { motivo ->
        ordenViewModel.limpiarCacheAlSalir()
        authViewModel.cerrarSesion(motivo)
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    LaunchedEffect(ordenState.sesionExpirada) {
        if (ordenState.sesionExpirada) {
            cerrarSesionYVolverALogin("Tu sesión ha expirado. Inicia sesión nuevamente.")
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { rol ->
                    val destino = destinoPorRol(rol)
                    navController.navigate(destino) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Tecnico.route) {
            TecnicoScreen(
                perfil = authState.perfil,
                authViewModel = authViewModel,
                ordenViewModel = ordenViewModel,
                onNuevaOrden = {
                    navController.navigate(Screen.NuevaOT.route)
                },
                onSeleccionarOrden = { ordenId ->
                    navController.navigate(Screen.DetalleOT.createRoute(ordenId))
                },
                onRolActualizado = { nuevoRol ->
                    val destino = destinoPorRol(nuevoRol)
                    if (destino != Screen.Tecnico.route) {
                        navController.navigate(destino) {
                            popUpTo(Screen.Tecnico.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onCerrarSesion = { cerrarSesionYVolverALogin(null) }
            )
        }

        composable(Screen.Cliente.route) {
            ClienteScreen(
                perfil = authState.perfil,
                authViewModel = authViewModel,
                ordenViewModel = ordenViewModel,
                onSeleccionarOrden = { ordenId ->
                    navController.navigate(Screen.DetalleOT.createRoute(ordenId))
                },
                onRolActualizado = { nuevoRol ->
                    val destino = destinoPorRol(nuevoRol)
                    if (destino != Screen.Cliente.route) {
                        navController.navigate(destino) {
                            popUpTo(Screen.Cliente.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onCerrarSesion = { cerrarSesionYVolverALogin(null) }
            )
        }

        composable(Screen.NuevaOT.route) {
            NuevaOTScreen(
                perfil = authState.perfil,
                ordenViewModel = ordenViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.DetalleOT.route,
            arguments = listOf(navArgument("ordenId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ordenId = backStackEntry.arguments?.getString("ordenId") ?: return@composable
            DetalleOTScreen(
                ordenId = ordenId,
                perfil = authState.perfil,
                ordenViewModel = ordenViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}