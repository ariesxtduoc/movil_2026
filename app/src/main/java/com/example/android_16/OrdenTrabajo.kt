package com.example.android_16

enum class EstadoOt {
    PENDIENTE,
    EN_PROCESO,
    COMPLETADO
}

enum class Prioridad {
    ALTA,
    MEDIA,
    BAJA
}

data class OrdenTrabajo(
    val id: String,
    val cliente: String,
    val direccion: String,
    val tipoServicio: String,
    val equipo: String,
    val esEquipoExterior: Boolean = false,
    val alertaClima: String? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val estado: EstadoOt = EstadoOt.PENDIENTE,
    val checkInHora: String? = null,
    val checkOutHora: String? = null,
    val diagnostico: String = "",
    val actividades: String = "",
    val fotoEvidenciaUrl: String? = null,
    val firmaCliente: Boolean = false
)