package com.example.android_16.uii

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_16.EstadoOt
import com.example.android_16.OrdenTrabajo

@Composable
fun ListaOtScreen(
    ordenes: List<OrdenTrabajo>,
    onOrdenSelect: (String) -> Unit
) {
    var tabSeleccionada by remember { mutableStateOf(0) }
    val tabs = listOf("Activas / En Proceso", "Registradas / Completadas")

    val ordenesFiltradas = when (tabSeleccionada) {
        0 -> ordenes.filter { it.estado != EstadoOt.COMPLETADO }
        else -> ordenes.filter { it.estado == EstadoOt.COMPLETADO }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestión de Órdenes ACIS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TabRow(selectedTabIndex = tabSeleccionada) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabSeleccionada == index,
                    onClick = { tabSeleccionada = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (ordenesFiltradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (tabSeleccionada == 0) "No hay órdenes pendientes." else "No hay órdenes registradas aún.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(ordenesFiltradas) { orden ->
                    TarjetaOrdenItem(
                        orden = orden,
                        onClick = { onOrdenSelect(orden.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaOrdenItem(orden: OrdenTrabajo, onClick: () -> Unit) {
    val colorChip = when (orden.estado) {
        EstadoOt.COMPLETADO -> Color(0xFFC8E6C9)
        EstadoOt.EN_PROCESO -> Color(0xFFBBDEFB)
        else -> Color(0xFFFFFECB)
    }

    val textoEstado = when (orden.estado) {
        EstadoOt.COMPLETADO -> "Completado"
        EstadoOt.EN_PROCESO -> "En Proceso"
        EstadoOt.PENDIENTE -> "Pendiente"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (orden.estado == EstadoOt.COMPLETADO) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = orden.id, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                SuggestionChip(
                    onClick = { },
                    label = { Text(textoEstado) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = colorChip)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = orden.cliente, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = orden.direccion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (orden.esEquipoExterior) Icons.Default.Warning else Icons.Default.Build,
                    contentDescription = null,
                    tint = if (orden.esEquipoExterior) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Equipo: ${orden.equipo}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}