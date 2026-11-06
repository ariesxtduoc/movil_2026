package com.example.android_16.navigaton

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.android_16.ui.theme.OtViewModel
import com.example.android_16.uii.*

sealed class Screen(val route: String) {
    object ListaOT : Screen("lista_ot")
    object DetalleOT : Screen("detalle_ot/{otId}") {
        fun createRoute(otId: String) = "detalle_ot/$otId"
    }
    object FormularioCierre : Screen("formulario_cierre/{otId}") {
        fun createRoute(otId: String) = "formulario_cierre/$otId"
    }
    object Jefatura : Screen("jefatura")
    object Cliente : Screen("cliente")
}

@Composable
fun AppNavigation(otViewModel: OtViewModel = viewModel()) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                    label = { Text("Técnico") },
                    selected = false,
                    onClick = { navController.navigate(Screen.ListaOT.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) },
                    label = { Text("Jefatura") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Jefatura.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Cliente") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Cliente.route) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ListaOT.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ListaOT.route) {
                ListaOtScreen(
                    ordenes = otViewModel.ordenes,
                    onOrdenSelect = { id ->
                        navController.navigate(Screen.DetalleOT.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.DetalleOT.route,
                arguments = listOf(navArgument("otId") { type = NavType.StringType })
            ) { backStackEntry ->
                val otId = backStackEntry.arguments?.getString("otId")
                val orden = otViewModel.ordenes.find { it.id == otId }

                if (orden != null) {
                    DetalleOtScreen(
                        orden = orden,
                        onIniciarCheckIn = {
                            otViewModel.registrarCheckIn(orden.id, "10:00 AM")
                        },
                        onIrAFormulario = {
                            navController.navigate(Screen.FormularioCierre.createRoute(orden.id))
                        }
                    )
                }
            }

            composable(
                route = Screen.FormularioCierre.route,
                arguments = listOf(navArgument("otId") { type = NavType.StringType })
            ) { backStackEntry ->
                val otId = backStackEntry.arguments?.getString("otId") ?: ""
                val orden = otViewModel.ordenes.find { it.id == otId }

                if (orden != null) {
                    FormularioCierreScreen(
                        ot = orden,
                        onGuardarOActualizar = { diagnostico, actividades, fotoUrl, horaCierre ->
                            otViewModel.guardarOActualizarOrden(
                                id = otId,
                                diagnostico = diagnostico,
                                actividades = actividades,
                                fotoUrl = fotoUrl,
                                checkOutHora = horaCierre
                            )
                            navController.popBackStack(Screen.ListaOT.route, inclusive = false)
                        },
                        onVolver = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Jefatura.route) { JefaturaScreen() }
            composable(Screen.Cliente.route) { ClienteScreen() }
        }
    }
}