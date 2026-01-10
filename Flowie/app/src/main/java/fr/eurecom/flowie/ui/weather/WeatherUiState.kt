package fr.eurecom.flowie.ui.weather

/*
 * UI state representing weather information displayed on screen.
 */
data class WeatherUiState(
    val temperature: String = "--°C",
    val icon: String = "❓",
    val wind: String = "-- m/s"
)

/*
 * Maps Open-Meteo weather codes to emoji icons.
 */
fun weatherCodeToIcon(code: Int): String {
    return when (code) {
        0 -> "☀️"        // Clear
        1, 2 -> "🌤️"     // Mainly clear
        3 -> "☁️"        // Cloudy
        45, 48 -> "🌫️"  // Fog
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        95 -> "⛈️"
        else -> "❓"
    }
}