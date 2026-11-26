package com.example.android_16.model

import com.google.gson.annotations.SerializedName

data class Perfil(
    @SerializedName("id") val id: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("rol") val rol: String = "tecnico"
)