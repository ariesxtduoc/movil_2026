package com.example.android_16.model

import com.google.gson.annotations.SerializedName

data class OrdenTrabajo(
    @SerializedName("id") val id: String = "",
    @SerializedName("codigo") val codigo: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("local") val local: String = "",
    @SerializedName("estado") val estado: String = "Pendiente",
    @SerializedName("tecnico_id") val tecnicoId: String? = null,
    @SerializedName("tecnico_nombre") val tecnicoNombre: String = "",
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("foto_url") val fotoUrl: String? = null,
    @SerializedName("firmado_cliente") val firmadoCliente: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

data class OrdenTrabajoRequest(
    @SerializedName("codigo") val codigo: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("local") val local: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("tecnico_id") val tecnicoId: String? = null,
    @SerializedName("tecnico_nombre") val tecnicoNombre: String = "",
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("foto_url") val fotoUrl: String? = null,
    @SerializedName("firmado_cliente") val firmadoCliente: Boolean = false
)