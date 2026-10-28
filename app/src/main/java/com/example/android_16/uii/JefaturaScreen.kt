package com.example.android_16.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JefaturaScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Vista Jefatura - Monitoreo SLA") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Control de Operaciones", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            // Panel para verificar cumplimiento de SLA contratados[cite: 1, 3]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cumplimiento Global de SLA", style = MaterialTheme.typography.titleMedium)
                    Text("92% de órdenes atendidas a tiempo", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF1565C0))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Resumen de Terreno", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Técnicos desplegados: 5")
            Text("• Órdenes en proceso: 2")
            Text("• Órdenes postergadas por clima: 1")
        }
    }
}