package com.example.android_16.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen() {
    var firmado by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Portal Cliente - Validaciones") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Órdenes Pendientes de Conformidad", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            // Validación de conformidad del cliente
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("OT-203: Preventivo Anual", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Local: Centro Distribución San Pedro", style = MaterialTheme.typography.bodyMedium)
                    Text("Técnico: Juan Pérez", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (firmado) {
                        Text("✓ Conformidad Firmada Satisfactoriamente", color = Color(0xFF2E7D32))
                    } else {
                        Button(onClick = { firmado = true }) {
                            Text("Validar y Firmar Conformidad")
                        }
                    }
                }
            }
        }
    }
}