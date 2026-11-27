package com.example.android_16.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
fun NuevaOTScreen(
    perfil: Perfil?,
    ordenViewModel: OrdenTrabajoViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by ordenViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val codigoInicial = remember { "OT-${(1000..9999).random()}" }
    var codigo by remember { mutableStateOf(codigoInicial) }
    var titulo by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var obteniendoGps by remember { mutableStateOf(false) }

    var fotoUriTemporal by remember { mutableStateOf<Uri?>(null) }
    var fotoCapturadaUri by remember { mutableStateOf<Uri?>(null) }
    var fotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var procesandoFoto by remember { mutableStateOf(false) }

    val ocupado = uiState.isLoading || procesandoFoto

    LaunchedEffect(Unit) {
        ordenViewModel.limpiarErroresFormulario()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            ordenViewModel.limpiarMensajes()
        }
    }

    val capturarUbicacion: () -> Unit = {
        scope.launch {
            obteniendoGps = true
            val coords = HardwareHelper.obtenerUbicacionActual(context)
            obteniendoGps = false
            if (coords != null) {
                latitud = coords.first
                longitud = coords.second
            } else {
                snackbarHostState.showSnackbar("No se pudo obtener la ubicación. Activa el GPS.")
            }
        }
    }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            capturarUbicacion()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permiso de ubicación denegado")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val uri = fotoUriTemporal
        if (exito && uri != null) {
            fotoCapturadaUri = uri
            fotoBytes = null
            scope.launch {
                procesandoFoto = true
                try {
                    fotoBytes = HardwareHelper.uriABytesComprimidos(context, uri)
                } finally {
                    procesandoFoto = false
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
            scope.launch {
                snackbarHostState.showSnackbar("Permiso de cámara denegado")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nueva Orden de Trabajo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = codigo,
                onValueChange = {
                    codigo = it
                    ordenViewModel.limpiarErrorCampo("codigo")
                },
                label = { Text("Código OT *") },
                trailingIcon = {
                    if (uiState.errorCodigo != null) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = uiState.errorCodigo != null,
                supportingText = {
                    uiState.errorCodigo?.let { Text(it) }
                },
                enabled = !uiState.isLoading,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    ordenViewModel.limpiarErrorCampo("titulo")
                },
                label = { Text("Título del trabajo *") },
                trailingIcon = {
                    if (uiState.errorTitulo != null) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = uiState.errorTitulo != null,
                supportingText = {
                    uiState.errorTitulo?.let { Text(it) }
                },
                enabled = !uiState.isLoading,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = local,
                onValueChange = {
                    local = it
                    ordenViewModel.limpiarErrorCampo("local")
                },
                label = { Text("Local / Sucursal *") },
                trailingIcon = {
                    if (uiState.errorLocal != null) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = uiState.errorLocal != null,
                supportingText = {
                    uiState.errorLocal?.let { Text(it) }
                },
                enabled = !uiState.isLoading,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción de labores") },
                enabled = !uiState.isLoading,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ubicación GPS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (latitud != null && longitud != null) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "Lat: %.5f  |  Lng: %.5f",
                                latitud,
                                longitud
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Sin coordenadas registradas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                                capturarUbicacion()
                            } else {
                                gpsPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !obteniendoGps && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (obteniendoGps) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Obteniendo GPS...")
                        } else {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (latitud != null) "Actualizar GPS" else "Capturar GPS actual")
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
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Evidencia Fotográfica (Opcional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    AnimatedVisibility(visible = fotoCapturadaUri != null) {
                        if (fotoCapturadaUri != null) {
                            AsyncImage(
                                model = fotoCapturadaUri,
                                contentDescription = "Vista previa evidencia",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }

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
                        enabled = !ocupado,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (procesandoFoto) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Comprimiendo foto...")
                        } else {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (fotoCapturadaUri != null) "Tomar otra foto" else "Tomar fotografía")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    ordenViewModel.crearOrden(
                        codigo = codigo,
                        titulo = titulo,
                        descripcion = descripcion,
                        local = local,
                        tecnicoId = perfil?.id,
                        tecnicoNombre = perfil?.nombre?.ifBlank { perfil.email } ?: "Técnico",
                        latitud = latitud,
                        longitud = longitud,
                        fotoBytes = fotoBytes,
                        onSuccess = onVolver
                    )
                },
                enabled = !ocupado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (ocupado) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(if (procesandoFoto) "Procesando foto..." else "Guardando...")
                    }
                } else {
                    Text("Guardar Orden de Trabajo")
                }
            }
        }
    }
}