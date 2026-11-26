package com.example.android_16.viewmodels

data class OrdenTrabajo(
    val id: String,
    val clienteNombre: String,
    val descripcion: String,
    val estado: String = "Pendiente", // Pendiente, En Proceso, Cerrada
    val checkInHora: String? = null,
    val diagnostico: String? = null,
    val actividades: String? = null,
    val fotoUrl: String? = null,
    val checkOutHora: String? = null
)