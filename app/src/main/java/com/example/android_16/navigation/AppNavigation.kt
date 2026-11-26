package com.example.android_16.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    ordenViewModel: OrdenTrabajoViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    val startDestination = remember {
        if (authState.isLoggedIn && authState.perfil != null) {
            if (authState.perfil!!.rol.equals("tecnico", ignoreCase = true)) {
                Screen.Tecnico.route
            } else {
                Screen.Cliente.route
            }
        } else {
            Screen.Login.route
        }
    }

    val cerrarSesionYVolverALogin: () -> Unit = {
        ordenViewModel.limpiarCacheAlSalir()
        authViewModel.cerrarSesion()
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
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
                    val destino = if (rol.equals("tecnico", ignoreCase = true)) {
                        Screen.Tecnico.route
                    } else {
                        Screen.Cliente.route
                    }
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
                ordenViewModel = ordenViewModel,
                onNuevaOrden = {
                    navController.navigate(Screen.NuevaOT.route)
                },
                onSeleccionarOrden = { ordenId ->
                    navController.navigate(Screen.DetalleOT.createRoute(ordenId))
                },
                onCerrarSesion = cerrarSesionYVolverALogin
            )
        }

        composable(Screen.Cliente.route) {
            ClienteScreen(
                perfil = authState.perfil,
                ordenViewModel = ordenViewModel,
                onSeleccionarOrden = { ordenId ->
                    navController.navigate(Screen.DetalleOT.createRoute(ordenId))
                },
                onCerrarSesion = cerrarSesionYVolverALogin
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