package com.example.android_16.model

import com.google.gson.annotations.SerializedName

enum class RolUsuario(val clave: String, val etiqueta: String) {
    ADMIN("admin", "Administrador"),
    SUPERVISOR("supervisor", "Supervisor"),
    TECNICO("tecnico", "Técnico"),
    CLIENTE("cliente", "Cliente");

    companion object {
        fun fromClave(rol: String?): RolUsuario {
            return entries.find { it.clave.equals(rol?.trim(), ignoreCase = true) } ?: TECNICO
        }
    }
}

data class Perfil(
    @SerializedName("id") val id: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("rol") val rol: String = RolUsuario.TECNICO.clave
) {
    val rolEnum: RolUsuario
        get() = RolUsuario.fromClave(rol)

    val esOperativo: Boolean
        get() = rolEnum in listOf(RolUsuario.ADMIN, RolUsuario.SUPERVISOR, RolUsuario.TECNICO)

    val puedeEliminarOrdenes: Boolean
        get() = rolEnum == RolUsuario.ADMIN

    val puedeFirmarOrdenes: Boolean
        get() = rolEnum == RolUsuario.CLIENTE || rolEnum == RolUsuario.ADMIN
}