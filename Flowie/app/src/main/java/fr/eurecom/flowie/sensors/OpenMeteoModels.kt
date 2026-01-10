package fr.eurecom.flowie.sensors

import kotlinx.serialization.Serializable

/*
 * Root response object returned by the Open-Meteo API.
 */
@Serializable
data class OpenMeteoResponse(
    val current_weather: CurrentWeather
)

/*
 * Model representing current weather conditions.
 */
@Serializable
data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int,
    val windspeed: Double
)