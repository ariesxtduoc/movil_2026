package com.example.android_16.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android_16.model.OrdenTrabajo

@Entity(tableName = "ordenes_trabajo")
data class OrdenTrabajoEntity(
    @PrimaryKey val id: String,
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val local: String,
    val estado: String,
    val tecnicoId: String?,
    val tecnicoNombre: String,
    val latitud: Double?,
    val longitud: Double?,
    val fotoUrl: String?,
    val firmadoCliente: Boolean,
    val createdAt: String?
)

fun OrdenTrabajoEntity.toModel(): OrdenTrabajo = OrdenTrabajo(
    id = id,
    codigo = codigo,
    titulo = titulo,
    descripcion = descripcion,
    local = local,
    estado = estado,
    tecnicoId = tecnicoId,
    tecnicoNombre = tecnicoNombre,
    latitud = latitud,
    longitud = longitud,
    fotoUrl = fotoUrl,
    firmadoCliente = firmadoCliente,
    createdAt = createdAt
)

fun OrdenTrabajo.toEntity(): OrdenTrabajoEntity = OrdenTrabajoEntity(
    id = id,
    codigo = codigo,
    titulo = titulo,
    descripcion = descripcion,
    local = local,
    estado = estado,
    tecnicoId = tecnicoId,
    tecnicoNombre = tecnicoNombre,
    latitud = latitud,
    longitud = longitud,
    fotoUrl = fotoUrl,
    firmadoCliente = firmadoCliente,
    createdAt = createdAt
)