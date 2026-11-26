package com.example.android_16.data.remote

import com.example.android_16.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): Response<WeatherResponse>
}