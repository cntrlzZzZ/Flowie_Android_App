package fr.eurecom.flowie.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.eurecom.flowie.sensors.OpenMeteoApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WeatherViewModel : ViewModel() {

    private val _temperature = MutableStateFlow<String>("--°C")
    val temperature: StateFlow<String> = _temperature

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val temp = OpenMeteoApi.getTemperature(lat, lon)
                _temperature.value = "${temp.toInt()}°C"
            } catch (e: Exception) {
                _temperature.value = "N/A"
            }
        }
    }
}