package com.example.android_16.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_16.model.Perfil
import com.example.android_16.viewmodel.OrdenTrabajoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(
    perfil: Perfil?,
    ordenViewModel: OrdenTrabajoViewModel,
    onSeleccionarOrden: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val ordenes by ordenViewModel.ordenes.collectAsState()
    val uiState by ordenViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var soloPendientesFirma by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ordenViewModel.sincronizarOrdenes()
    }

    LaunchedEffect(uiState.error, uiState.mensaje) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            ordenViewModel.limpiarMensajes()
        }
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            ordenViewModel.limpiarMensajes()
        }
    }

    val ordenesVisibles = remember(ordenes, soloPendientesFirma) {
        if (soloPendientesFirma) ordenes.filter { !it.firmadoCliente }
        else ordenes
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Portal Cliente", fontWeight = FontWeight.Bold)
                        Text(
                            text = perfil?.nombre?.ifBlank { perfil.email } ?: "Cliente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { ordenViewModel.sincronizarOrdenes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !soloPendientesFirma,
                    onClick = { soloPendientesFirma = false },
                    label = { Text("Todas (${ordenes.size})") }
                )
                FilterChip(
                    selected = soloPendientesFirma,
                    onClick = { soloPendientesFirma = true },
                    label = { Text("Por firmar (${ordenes.count { !it.firmadoCliente }})") }
                )
            }

            if (ordenesVisibles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (soloPendientesFirma) {
                            "Todas las órdenes ya cuentan con firma de conformidad."
                        } else {
                            "Aún no hay órdenes de trabajo disponibles."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ordenesVisibles, key = { it.id }) { orden ->
                        OrdenTrabajoCard(
                            orden = orden,
                            onClick = { onSeleccionarOrden(orden.id) }
                        )
                    }
                }
            }
        }
    }
}