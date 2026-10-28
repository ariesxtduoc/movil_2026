package com.example.android_16.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioCierreScreen(
    otId: String,
    onFinalizar: () -> Unit
) {
    var diagnostico by remember { mutableStateOf("") }
    var actividades by remember { mutableStateOf("") }
    var fotoTomada by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cierre de Orden - $otId") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = diagnostico,
                onValueChange = { diagnostico = it },
                label = { Text("Diagnóstico Técnico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = actividades,
                onValueChange = { actividades = it },
                label = { Text("Actividades Realizadas") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Evidencia Fotográfica", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // Captura/adjunto de evidencia fotográfica[cite: 2, 3]
            OutlinedButton(
                onClick = { fotoTomada = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (fotoTomada) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = if (fotoTomada) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (fotoTomada) "Foto Adjuntada Correctamente" else "Simular Tomar Foto de Evidencia")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onFinalizar,
                enabled = diagnostico.isNotEmpty() && fotoTomada,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar Orden y Enviar (Check-Out)")
            }
        }
    }
}