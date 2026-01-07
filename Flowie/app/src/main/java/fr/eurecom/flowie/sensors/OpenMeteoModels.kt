package fr.eurecom.flowie.sensors

import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoResponse(
    val current: CurrentWeather
)

@Serializable
data class CurrentWeather(
    val temperature_2m: Double
)