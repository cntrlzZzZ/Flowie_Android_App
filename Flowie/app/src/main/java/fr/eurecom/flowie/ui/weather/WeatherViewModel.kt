package fr.eurecom.flowie.ui.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.eurecom.flowie.sensors.OpenMeteoApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/*
 * ViewModel responsible for loading weather data and exposing UI state.
 */
class WeatherViewModel : ViewModel() {

    private val TAG = "WeatherVM"

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState

    /*
     * Loads weather data for the given coordinates and updates UI state.
     */
    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading weather...")

                val weather = OpenMeteoApi.getWeather(lat, lon)

                Log.d(
                    TAG,
                    "Parsed weather → temp=${weather.temperature}, " +
                            "code=${weather.weathercode}, wind=${weather.windspeed}"
                )

                _uiState.value = WeatherUiState(
                    temperature = "${weather.temperature.toInt()}°C",
                    icon = weatherCodeToIcon(weather.weathercode),
                    wind = "${weather.windspeed} m/s"
                )

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Failed to load weather", e)
                Log.e(TAG, "Weather error", e)
                _uiState.value = WeatherUiState(
                    temperature = "N/A",
                    icon = "❌",
                    wind = "--"
                )
            }
        }
    }
}
