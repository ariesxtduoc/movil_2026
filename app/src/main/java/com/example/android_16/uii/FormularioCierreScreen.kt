package com.example.android_16.uii

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_16.OrdenTrabajo

@Composable
fun FormularioCierreScreen(
    ot: OrdenTrabajo,
    onGuardarOActualizar: (diagnostico: String, actividades: String, fotoUrl: String?, horaCierre: String) -> Unit,
    onVolver: () -> Unit
) {
    var diagnostico by remember { mutableStateOf(ot.diagnostico) }
    var actividades by remember { mutableStateOf(ot.actividades) }
    var fotoCargada by remember { mutableStateOf(ot.fotoEvidenciaUrl != null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (ot.diagnostico.isEmpty()) "Registrar Cierre: ${ot.id}" else "Editar Registro: ${ot.id}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Cliente: ${ot.cliente}", fontWeight = FontWeight.SemiBold)
                Text(text = "Equipo: ${ot.equipo}")
                Text(text = "Hora de Check-In: ${ot.checkInHora ?: "No registrada"}")
            }
        }

        OutlinedTextField(
            value = diagnostico,
            onValueChange = { diagnostico = it },
            label = { Text("Diagnóstico Técnico") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = actividades,
            onValueChange = { actividades = it },
            label = { Text("Actividades Realizadas y Repuestos") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Evidencia Fotográfica", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { fotoCargada = true }) {
                    Text(text = if (fotoCargada) "✓ Evidencia Adjuntada" else "Capturar / Adjuntar Foto")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onVolver, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    onGuardarOActualizar(
                        diagnostico,
                        actividades,
                        if (fotoCargada) "foto_${ot.id}.jpg" else null,
                        "11:30 AM"
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = diagnostico.isNotEmpty() && actividades.isNotEmpty()
            ) {
                Text(if (ot.diagnostico.isEmpty()) "Guardar Cierre" else "Actualizar Registro")
            }
        }
    }
}