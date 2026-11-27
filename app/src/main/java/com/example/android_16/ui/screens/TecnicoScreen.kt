package com.example.android_16.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.android_16.model.OrdenTrabajo
import com.example.android_16.model.Perfil
import com.example.android_16.model.RolUsuario
import com.example.android_16.viewmodel.AuthViewModel
import com.example.android_16.viewmodel.OrdenTrabajoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoScreen(
    perfil: Perfil?,
    authViewModel: AuthViewModel,
    ordenViewModel: OrdenTrabajoViewModel,
    onNuevaOrden: () -> Unit,
    onSeleccionarOrden: (String) -> Unit,
    onRolActualizado: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val ordenes by ordenViewModel.ordenes.collectAsState()
    val uiState by ordenViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val filtros = listOf("Todas", "Pendiente", "En Proceso", "Completada")
    var filtroActivo by remember { mutableStateOf("Todas") }
    var mostrarEditarPerfil by remember { mutableStateOf(false) }

    val rolActual = perfil?.rolEnum ?: RolUsuario.TECNICO
    val tituloPanel = when (rolActual) {
        RolUsuario.ADMIN -> "Panel Administrador"
        RolUsuario.SUPERVISOR -> "Panel Supervisor"
        else -> "Panel Técnico"
    }

    LaunchedEffect(Unit) {
        ordenViewModel.sincronizarOrdenes()
    }

    LaunchedEffect(uiState.error, uiState.mensaje, authState.mensaje, authState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            ordenViewModel.limpiarMensajes()
        }
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            ordenViewModel.limpiarMensajes()
        }
        authState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.limpiarMensajes()
        }
        authState.error?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.limpiarMensajes()
        }
    }

    if (mostrarEditarPerfil && perfil != null) {
        EditarPerfilDialog(
            perfil = perfil,
            authViewModel = authViewModel,
            onDismiss = { mostrarEditarPerfil = false },
            onGuardado = { actualizado ->
                mostrarEditarPerfil = false
                onRolActualizado(actualizado.rol)
            }
        )
    }

    val ordenesFiltradas = remember(ordenes, filtroActivo) {
        if (filtroActivo == "Todas") ordenes
        else ordenes.filter { it.estado.equals(filtroActivo, ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tituloPanel, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${perfil?.nombre?.ifBlank { perfil.email } ?: "Usuario"} · ${rolActual.etiqueta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarEditarPerfil = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                    }
                    IconButton(onClick = { ordenViewModel.sincronizarOrdenes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaOrden) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Orden")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { estado ->
                    FilterChip(
                        selected = filtroActivo == estado,
                        onClick = { filtroActivo = estado },
                        label = { Text(estado) }
                    )
                }
            }

            if (ordenesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (ordenes.isEmpty()) {
                            "No hay órdenes registradas. Presiona + para crear la primera."
                        } else {
                            "No hay órdenes en estado \"$filtroActivo\"."
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
                    items(ordenesFiltradas, key = { it.id }) { orden ->
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

@Composable
fun EditarPerfilDialog(
    perfil: Perfil,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onGuardado: (Perfil) -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    var nombre by remember { mutableStateOf(perfil.nombre) }
    var rolSeleccionado by remember { mutableStateOf(perfil.rolEnum.clave) }

    AlertDialog(
        onDismissRequest = {
            if (!authState.isLoading) {
                authViewModel.limpiarTodosLosErrores()
                onDismiss()
            }
        },
        title = { Text("Editar Perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Correo: ${perfil.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        authViewModel.limpiarErrorCampo("nombre")
                    },
                    label = { Text("Nombre completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    trailingIcon = {
                        if (authState.errorNombre != null) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = authState.errorNombre != null,
                    supportingText = {
                        authState.errorNombre?.let { Text(it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Rol asignado:",
                    style = MaterialTheme.typography.labelLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = rolSeleccionado == RolUsuario.ADMIN.clave,
                        onClick = { rolSeleccionado = RolUsuario.ADMIN.clave },
                        label = { Text(RolUsuario.ADMIN.etiqueta) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = rolSeleccionado == RolUsuario.SUPERVISOR.clave,
                        onClick = { rolSeleccionado = RolUsuario.SUPERVISOR.clave },
                        label = { Text(RolUsuario.SUPERVISOR.etiqueta) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = rolSeleccionado == RolUsuario.TECNICO.clave,
                        onClick = { rolSeleccionado = RolUsuario.TECNICO.clave },
                        label = { Text(RolUsuario.TECNICO.etiqueta) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = rolSeleccionado == RolUsuario.CLIENTE.clave,
                        onClick = { rolSeleccionado = RolUsuario.CLIENTE.clave },
                        label = { Text(RolUsuario.CLIENTE.etiqueta) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    authViewModel.actualizarPerfil(nombre, rolSeleccionado, onSuccess = onGuardado)
                },
                enabled = !authState.isLoading
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    authViewModel.limpiarTodosLosErrores()
                    onDismiss()
                },
                enabled = !authState.isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun OrdenTrabajoCard(
    orden: OrdenTrabajo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = orden.codigo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = onClick,
                    label = { Text(orden.estado) }
                )
            }

            Text(
                text = orden.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Local: ${orden.local}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (orden.tecnicoNombre.isNotBlank()) {
                Text(
                    text = "Técnico: ${orden.tecnicoNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (orden.latitud != null && orden.longitud != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("GPS", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (!orden.fotoUrl.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Evidencia",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Foto", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (orden.firmadoCliente) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Firmada",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Firmada", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}