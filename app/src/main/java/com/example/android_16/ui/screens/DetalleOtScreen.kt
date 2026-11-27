package com.example.android_16.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.android_16.model.Perfil
import com.example.android_16.viewmodel.OrdenTrabajoViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOTScreen(
    ordenId: String,
    perfil: Perfil?,
    ordenViewModel: OrdenTrabajoViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by ordenViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val esOperativo = perfil?.esOperativo == true
    val puedeEliminar = perfil?.puedeEliminarOrdenes == true
    val puedeFirmar = perfil?.puedeFirmarOrdenes == true

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var fotoUriTemporal by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(ordenId) {
        ordenViewModel.cargarOrdenPorId(ordenId)
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

    val orden = uiState.ordenSeleccionada

    val capturarYActualizarGps: () -> Unit = {
        val ordenActual = orden
        if (ordenActual != null) {
            scope.launch {
                val coords = HardwareHelper.obtenerUbicacionActual(context)
                if (coords != null) {
                    ordenViewModel.actualizarUbicacion(ordenActual, coords.first, coords.second)
                } else {
                    snackbarHostState.showSnackbar("No se pudo obtener la ubicación GPS")
                }
            }
        }
    }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            capturarYActualizarGps()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permiso de ubicación denegado") }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val uri = fotoUriTemporal
        val ordenActual = orden
        if (exito && uri != null && ordenActual != null) {
            scope.launch {
                val bytes = HardwareHelper.uriABytesComprimidos(context, uri)
                if (bytes != null) {
                    ordenViewModel.subirYGuardarEvidencia(ordenActual, bytes)
                } else {
                    snackbarHostState.showSnackbar("No se pudo procesar la fotografía")
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val uri = HardwareHelper.crearUriFotoTemporal(context)
            fotoUriTemporal = uri
            cameraLauncher.launch(uri)
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permiso de cámara denegado") }
        }
    }

    if (mostrarDialogoEliminar && orden != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar orden") },
            text = { Text("¿Seguro que deseas eliminar la orden ${orden.codigo}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                        ordenViewModel.eliminarOrden(orden.id, onSuccess = onVolver)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(orden?.codigo ?: "Detalle de Orden", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (puedeEliminar && orden != null) {
                        IconButton(onClick = { mostrarDialogoEliminar = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar orden",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (orden == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedVisibility(visible = uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = orden.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Local: ${orden.local}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Técnico: ${orden.tecnicoNombre.ifBlank { "No asignado" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (orden.descripcion.isNotBlank()) {
                        Text(
                            text = orden.descripcion,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = if (orden.firmadoCliente) {
                            "Estado de firma: Firmada conforme por el cliente"
                        } else {
                            "Estado de firma: Pendiente de firma del cliente"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (orden.firmadoCliente) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (esOperativo) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Estado de la Orden (${perfil?.rolEnum?.etiqueta ?: "Operativo"})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Pendiente", "En Proceso", "Completada").forEach { estado ->
                                FilterChip(
                                    selected = orden.estado.equals(estado, ignoreCase = true),
                                    onClick = { ordenViewModel.actualizarEstado(orden.id, estado) },
                                    label = { Text(estado) },
                                    enabled = !uiState.isLoading
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Geolocalización y Clima en Terreno",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (orden.latitud != null && orden.longitud != null) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "Coordenadas: %.5f, %.5f",
                                orden.latitud,
                                orden.longitud
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        when {
                            uiState.cargandoClima -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Consultando Open-Meteo...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            uiState.climaActual != null -> {
                                val clima = uiState.climaActual!!
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${clima.temperature}°C · ${HardwareHelper.interpretarClima(clima.weathercode)} · Viento ${clima.windspeed} km/h",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Sin ubicación GPS registrada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (esOperativo) {
                        OutlinedButton(
                            onClick = {
                                val tieneFine = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                val tieneCoarse = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (tieneFine || tieneCoarse) {
                                    capturarYActualizarGps()
                                } else {
                                    gpsPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (orden.latitud != null) "Actualizar GPS" else "Registrar GPS actual")
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Evidencia Fotográfica",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!orden.fotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = orden.fotoUrl,
                            contentDescription = "Evidencia de la orden",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Text(
                            text = "Aún no se ha subido fotografía de evidencia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (esOperativo) {
                        OutlinedButton(
                            onClick = {
                                val tieneCamara = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (tieneCamara) {
                                    val uri = HardwareHelper.crearUriFotoTemporal(context)
                                    fotoUriTemporal = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (orden.fotoUrl.isNullOrBlank()) "Subir fotografía" else "Reemplazar fotografía")
                        }
                    }
                }
            }

            if (puedeFirmar && !orden.firmadoCliente) {
                Button(
                    onClick = { ordenViewModel.firmarOrdenComoCliente(orden) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Firmar Conformidad")
                }
            }
        }
    }
}