package com.example.android_16.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.android_16.listaOrdenesPrueba
import com.example.android_16.ui.*

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
fun AppNavigation() {
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
                    ordenes = listaOrdenesPrueba,
                    onOrdenSelect = { id -> navController.navigate(Screen.DetalleOT.createRoute(id)) }
                )
            }

            composable(
                route = Screen.DetalleOT.route,
                arguments = listOf(navArgument("otId") { type = NavType.StringType })
            ) { backStackEntry ->
                val otId = backStackEntry.arguments?.getString("otId")
                val orden = listaOrdenesPrueba.find { it.id == otId }
                DetalleOtScreen(
                    orden = orden,
                    onIniciarCheckIn = { },
                    onIrAFormulario = {
                        if (otId != null) {
                            navController.navigate(Screen.FormularioCierre.createRoute(otId))
                        }
                    }
                )
            }

            composable(
                route = Screen.FormularioCierre.route,
                arguments = listOf(navArgument("otId") { type = NavType.StringType })
            ) { backStackEntry ->
                val otId = backStackEntry.arguments?.getString("otId") ?: ""
                FormularioCierreScreen(
                    otId = otId,
                    onFinalizar = {
                        listaOrdenesPrueba.find { it.id == otId }?.estado = "Completado"
                        navController.popBackStack(Screen.ListaOT.route, inclusive = false)
                    }
                )
            }

            composable(Screen.Jefatura.route) { JefaturaScreen() }
            composable(Screen.Cliente.route) { ClienteScreen() }
        }
    }
}