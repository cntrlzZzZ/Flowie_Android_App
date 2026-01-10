package fr.eurecom.flowie.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import fr.eurecom.flowie.ui.components.FilterChipSelectable
import fr.eurecom.flowie.ui.components.MapTilerMap
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.eurecom.flowie.ui.weather.WeatherViewModel

/*
 * Main exploration screen displaying the interactive map,
 * filters, weather information, and user location.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen() {

    val vienna = LatLng(48.2082, 16.3738)
    var mapboxMap by remember { mutableStateOf<com.mapbox.mapboxsdk.maps.MapboxMap?>(null) }
    val weatherViewModel = androidx.lifecycle.viewmodel.compose.viewModel<WeatherViewModel>()
    val weatherState by weatherViewModel.uiState.collectAsState()
    val location = mapboxMap
        ?.locationComponent
        ?.lastKnownLocation

    Box(modifier = Modifier.fillMaxSize()) {

        MapTilerMap(
            modifier = Modifier.fillMaxSize(),
            center = vienna,
            zoom = 14.0 ,
            onMapReady = { mapboxMap = it }
        )

        LaunchedEffect(location) {
            location ?: return@LaunchedEffect

            while (true) {
                weatherViewModel.loadWeather(
                    lat = location.latitude,
                    lon = location.longitude
                )
                kotlinx.coroutines.delay(2 * 60 * 1000L) // 2 minutes
            }
        }

        // UI par-dessus la carte
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search for spots") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipSelectable("Verified", false) {}
                FilterChipSelectable("Community", false) {}
                FilterChipSelectable("Wheelchair Access", false) {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            WeatherCard(
                icon = weatherState.icon,
                temperature = weatherState.temperature
            )
        }
        FloatingActionButton(
            onClick = {
                val map = mapboxMap ?: return@FloatingActionButton

                val location = map.locationComponent.lastKnownLocation ?: return@FloatingActionButton

                map.animateCamera(
                    com.mapbox.mapboxsdk.camera.CameraUpdateFactory
                        .newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            16.0
                        )
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text("📍")
        }
    }
}

/*
 * Small card displaying current weather icon and temperature.
 */
@Composable
fun WeatherCard(
    icon: String,
    temperature: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.7f),
        modifier = Modifier.size(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = MaterialTheme.typography.headlineMedium.fontSize)
            Spacer(modifier = Modifier.height(4.dp))
            Text(temperature, color = Color.White)
        }
    }
}

