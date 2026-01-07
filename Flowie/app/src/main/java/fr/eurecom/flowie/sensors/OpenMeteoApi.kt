package fr.eurecom.flowie.sensors

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object OpenMeteoApi {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    suspend fun getTemperature(
        latitude: Double,
        longitude: Double
    ): Double {
        val response: OpenMeteoResponse = client.get(
            "https://api.open-meteo.com/v1/forecast"
        ) {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current", "temperature_2m")
        }.body()

        return response.current.temperature_2m
    }
}