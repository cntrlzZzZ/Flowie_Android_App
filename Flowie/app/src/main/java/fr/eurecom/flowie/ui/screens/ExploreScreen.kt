package fr.eurecom.flowie.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.eurecom.flowie.R
import fr.eurecom.flowie.data.model.SpotDto
import fr.eurecom.flowie.data.remote.SourcesRepository
import fr.eurecom.flowie.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen() {
    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)

    val vienna = LatLng(48.2082, 16.3738)

    var mapboxMap by remember { mutableStateOf<com.mapbox.mapboxsdk.maps.MapboxMap?>(null) }
    val repo = remember { SourcesRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Filters
    var filterState by remember { mutableStateOf(SpotFilterState()) }

    // Saved ids (local)
    var savedIds by remember { mutableStateOf(SavedSpotsStore.getIds(context)) }

    // Data states
    var spots by remember { mutableStateOf<List<SpotDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Bottom sheet selected spot
    var selectedSpot by remember { mutableStateOf<SpotDto?>(null) }

    // Markers
    val markers = remember { mutableStateListOf<Marker>() }
    val markerSpotMap = remember { mutableStateMapOf<Long, SpotDto>() }

    fun renderMarkers(newSpots: List<SpotDto>) {
        val map = mapboxMap ?: return

        markers.forEach { it.remove() }
        markers.clear()
        markerSpotMap.clear()

        newSpots.forEach { s ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(s.lat, s.lng))
                    .title(s.address ?: "Water spot")
                    .snippet("${s.typeLabel} • ${s.status} • ${s.origin}")
            )
            if (marker != null) {
                markers.add(marker)
                markerSpotMap[marker.id] = s
            }
        }
    }

    fun loadViennaSpots() {
        scope.launch {
            try {
                isLoading = true
                errorText = null

                val result = repo.fetchInBbox(
                    minLat = 48.1304464434,
                    minLng = 16.2778988626,
                    maxLat = 48.338028,
                    maxLng = 16.5347851295,
                    maxRows = 2000
                )

                Log.d("ExploreScreen", "Loaded spots: ${result.size}")
                spots = result
            } catch (e: Exception) {
                Log.e("ExploreScreen", "Failed loading spots", e)
                errorText = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }

    // Set marker click + load data when map is ready
    LaunchedEffect(mapboxMap) {
        val map = mapboxMap ?: return@LaunchedEffect

        map.setOnMarkerClickListener { marker ->
            markerSpotMap[marker.id]?.let { selectedSpot = it }
            true
        }

        loadViennaSpots()
    }

    val filteredSpots by remember(spots, filterState) {
        derivedStateOf { applySpotFilters(spots, filterState) }
    }

    LaunchedEffect(mapboxMap, filteredSpots) {
        if (mapboxMap != null) renderMarkers(filteredSpots)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapTilerMap(
            modifier = Modifier.fillMaxSize(),
            center = vienna,
            zoom = 14.0,
            onMapReady = { map -> mapboxMap = map }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            SpotFilterBar(
                state = filterState,
                onStateChange = { filterState = it },
                placeholder = "Search for spots",
                surfaceColor = surfaceColor,
                elevatedSurfaceColor = elevatedSurfaceColor,
                borderColor = borderColor,
                accentBlue = accentBlue,
                textPrimary = textPrimary
            )

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Text("Loading spots…", color = textPrimary)
            } else if (errorText != null) {
                Text("Error: $errorText", color = Color.Red)
            } else {
                Text("Showing: ${filteredSpots.size} / ${spots.size}", color = textPrimary)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { loadViennaSpots() },
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue, contentColor = textPrimary)
            ) {
                Text("Refresh spots")
            }
        }

        FloatingActionButton(
            onClick = {
                val map = mapboxMap ?: return@FloatingActionButton
                val location = map.locationComponent.lastKnownLocation ?: return@FloatingActionButton

                map.animateCamera(
                    com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        16.0
                    )
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = surfaceColor,
            contentColor = textPrimary
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_my_location_pin),
                contentDescription = "Find my location",
                modifier = Modifier.size(46.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Bottom sheet
        selectedSpot?.let { spot ->
            SpotDetailsBottomSheet(
                spot = spot,
                isSaved = savedIds.contains(spot.id),
                onToggleSave = {
                    savedIds = SavedSpotsStore.toggle(context, spot.id)
                },
                onDismiss = { selectedSpot = null }
            )
        }
    }
}
