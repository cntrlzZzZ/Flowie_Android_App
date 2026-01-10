package fr.eurecom.flowie.sensors

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/*
 * API client responsible for fetching weather data from Open-Meteo.
 * Uses Ktor and Kotlinx Serialization.
 */
object OpenMeteoApi {

    private const val TAG = "OpenMeteo"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    /*
     * Fetches current weather data for the given latitude and longitude.
     */
    suspend fun getWeather(
        latitude: Double,
        longitude: Double
    ): CurrentWeather {

        Log.d(TAG, "Requesting weather for lat=$latitude lon=$longitude")

        val response: OpenMeteoResponse = client.get(
            "https://api.open-meteo.com/v1/forecast"
        ) {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current_weather", "true")
        }.body()

        Log.d(TAG, "Weather received: $response")

        return response.current_weather
    }
}