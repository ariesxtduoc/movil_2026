package com.example.android_16.model

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("user") val user: AuthUser? = null
)

data class AuthUser(
    @SerializedName("id") val id: String = "",
    @SerializedName("email") val email: String? = null
)