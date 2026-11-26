package com.example.android_16.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current_weather") val currentWeather: CurrentWeather? = null
)

data class CurrentWeather(
    @SerializedName("temperature") val temperature: Double = 0.0,
    @SerializedName("windspeed") val windspeed: Double = 0.0,
    @SerializedName("weathercode") val weathercode: Int = 0
)