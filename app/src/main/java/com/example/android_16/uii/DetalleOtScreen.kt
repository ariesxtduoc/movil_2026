package com.example.android_16.uii

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_16.EstadoOt
import com.example.android_16.OrdenTrabajo

@Composable
fun DetalleOtScreen(
    orden: OrdenTrabajo,
    onIniciarCheckIn: () -> Unit,
    onIrAFormulario: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (orden.esEquipoExterior && orden.alertaClima != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Alerta Meteorológica Preventiva",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = orden.alertaClima,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Text(text = orden.cliente, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = orden.direccion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(text = "Tipo de Servicio: ${orden.tipoServicio}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Prioridad: ${orden.prioridad}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(6.dp))

        val textoEstado = when (orden.estado) {
            EstadoOt.PENDIENTE -> "Pendiente"
            EstadoOt.EN_PROCESO -> "En Proceso"
            EstadoOt.COMPLETADO -> "Completado"
        }
        Text(
            text = "Estado Actual: $textoEstado",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        if (orden.checkInHora != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Hora de Check-In: ${orden.checkInHora}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (orden.estado) {
            EstadoOt.PENDIENTE -> {
                Button(
                    onClick = onIniciarCheckIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Trabajos (Check-In)")
                }
            }
            EstadoOt.EN_PROCESO -> {
                Button(
                    onClick = onIrAFormulario,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ir a Formulario de Cierre")
                }
            }
            EstadoOt.COMPLETADO -> {
                Button(
                    onClick = onIrAFormulario,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Ver / Editar Registro de Cierre")
                }
            }
        }
    }
}