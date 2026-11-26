package com.example.android_16.data.local

import android.content.Context
import com.example.android_16.model.Perfil

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("acis_session", Context.MODE_PRIVATE)

    fun guardarSesion(token: String, perfil: Perfil) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, perfil.id)
            .putString(KEY_EMAIL, perfil.email)
            .putString(KEY_NOMBRE, perfil.nombre)
            .putString(KEY_ROL, perfil.rol)
            .apply()
    }

    fun obtenerToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun obtenerPerfil(): Perfil? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        return Perfil(
            id = id,
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            nombre = prefs.getString(KEY_NOMBRE, "") ?: "",
            rol = prefs.getString(KEY_ROL, "tecnico") ?: "tecnico"
        )
    }

    fun haySesionActiva(): Boolean = !obtenerToken().isNullOrBlank()

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_ROL = "rol"
    }
}