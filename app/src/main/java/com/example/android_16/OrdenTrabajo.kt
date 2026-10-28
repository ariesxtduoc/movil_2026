package com.example.android_16

import androidx.compose.runtime.mutableStateListOf

data class OrdenTrabajo(
    val id: String,
    val cliente: String,
    val local: String,
    val direccion: String,
    val esEquipoExterior: Boolean, // Equipos en azoteas o patios exteriores[cite: 2]
    val tipo: String,              // Preventivo / Correctivo[cite: 2]
    val prioridad: String,         // Alta, Media, Baja[cite: 2]
    var estado: String,            // Pendiente, En Proceso, Completado
    val alertaClima: String? = null // Alerta por condiciones climáticas adversas[cite: 2, 3]
)

// Datos simulados para la demostración[cite: 1, 2]
val listaOrdenesPrueba = mutableStateListOf(
    OrdenTrabajo(
        id = "OT-201",
        cliente = "Cadenas Retail S.A.",
        local = "Supermercado Lider - Concepción",
        direccion = "Barros Arana 450",
        esEquipoExterior = true,
        tipo = "Preventivo Climatización",
        prioridad = "Alta",
        estado = "Pendiente",
        alertaClima = "Pronóstico de lluvia y vientos de 35 km/h para hoy en la zona."
    ),
    OrdenTrabajo(
        id = "OT-202",
        cliente = "Centros Comerciales Sur",
        local = "Mall Plaza Trébol - Local 102",
        direccion = "Av. Jorge Alessandri 3177",
        esEquipoExterior = false,
        tipo = "Correctivo Refrigeración",
        prioridad = "Media",
        estado = "En Proceso"
    ),
    OrdenTrabajo(
        id = "OT-203",
        cliente = "Retail Falabella",
        local = "Centro Distribución - San Pedro",
        direccion = "Ruta 160 km 8",
        esEquipoExterior = true,
        tipo = "Preventivo Anual",
        prioridad = "Baja",
        estado = "Completado"
    )
)