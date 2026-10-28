package com.example.android_16.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android_16.OrdenTrabajo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaOtScreen(
    ordenes: List<OrdenTrabajo>,
    onOrdenSelect: (String) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("ACIS - Órdenes de Terreno") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(ordenes) { orden ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onOrdenSelect(orden.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = orden.id, style = MaterialTheme.typography.titleMedium)

                            val colorChip = when (orden.estado) {
                                "Completado" -> Color(0xFFC8E6C9)
                                "En Proceso" -> Color(0xFFBBDEFB)
                                else -> Color(0xFFFFECB3)
                            }

                            SuggestionChip(
                                onClick = {},
                                label = { Text(orden.estado) },
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = colorChip)
                            )
                        }

                        Text(text = orden.cliente, style = MaterialTheme.typography.bodyLarge)
                        Text(text = orden.local, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (orden.esEquipoExterior) Icons.Default.Warning else Icons.Default.Build,
                                contentDescription = null,
                                tint = if (orden.esEquipoExterior) Color(0xFFD84315) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (orden.esEquipoExterior) "Unidad Exterior (Azotea/Patio)" else "Unidad Interior",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}