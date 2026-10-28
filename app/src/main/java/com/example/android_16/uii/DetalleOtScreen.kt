package com.example.android_16.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android_16.OrdenTrabajo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOtScreen(
    orden: OrdenTrabajo?,
    onIniciarCheckIn: () -> Unit,
    onIrAFormulario: () -> Unit
) {
    if (orden == null) return

    var estadoLocal by remember { mutableStateOf(orden.estado) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle - ${orden.id}") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Alerta preventiva de clima para equipos en azoteas[cite: 2, 3]
            if (orden.esEquipoExterior && orden.alertaClima != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Alerta Meteorológica Preventiva", style = MaterialTheme.typography.titleSmall, color = Color.Red)
                            Text(orden.alertaClima, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Text(text = orden.local, style = MaterialTheme.typography.headlineSmall)
            Text(text = orden.direccion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Tipo de Servicio: ${orden.tipo}")
            Text("Prioridad: ${orden.prioridad}")
            Text("Estado Actual: $estadoLocal")

            Spacer(modifier = Modifier.weight(1f))

            if (estadoLocal == "Pendiente") {
                Button(
                    onClick = {
                        estadoLocal = "En Proceso"
                        orden.estado = "En Proceso"
                        onIniciarCheckIn()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Check-In GPS")
                }
            } else {
                Button(
                    onClick = onIrAFormulario,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ir a Formulario de Cierre / Evidencia")
                }
            }
        }
    }
}