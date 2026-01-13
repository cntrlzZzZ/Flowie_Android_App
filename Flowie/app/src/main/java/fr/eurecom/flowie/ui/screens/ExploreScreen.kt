package fr.eurecom.flowie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.eurecom.flowie.R
import fr.eurecom.flowie.ui.components.MapTilerMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen() {

    // Dark theme colours
    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)

    val vienna = LatLng(48.2082, 16.3738)
    var mapboxMap by remember { mutableStateOf<com.mapbox.mapboxsdk.maps.MapboxMap?>(null) }

    var searchText by remember { mutableStateOf("") }

    // Filter chip states
    var selectedVerified by remember { mutableStateOf(false) }
    var selectedCommunity by remember { mutableStateOf(false) }
    var selectedWheelchair by remember { mutableStateOf(false) }
    var selectedDogBowl by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        MapTilerMap(
            modifier = Modifier.fillMaxSize(),
            center = vienna,
            zoom = 14.0,
            onMapReady = { mapboxMap = it }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {

            // Search bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(text = "Search for spots", color = textPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = surfaceColor,
                    focusedContainerColor = surfaceColor,
                    unfocusedTextColor = textPrimary,
                    focusedTextColor = textPrimary,
                    unfocusedPlaceholderColor = textPrimary,
                    focusedPlaceholderColor = textPrimary,
                    unfocusedIndicatorColor = borderColor,
                    focusedIndicatorColor = accentBlue,
                    cursorColor = accentBlue
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Swipeable / scrollable chips row
            val chipScrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipColors = FilterChipDefaults.filterChipColors(
                    containerColor = elevatedSurfaceColor,
                    labelColor = textPrimary,
                    selectedContainerColor = accentBlue,
                    selectedLabelColor = textPrimary
                )
                val chipBorder = BorderStroke(1.dp, borderColor)

                FilterChip(
                    selected = selectedVerified,
                    onClick = { selectedVerified = !selectedVerified },
                    label = { Text("Verified", color = textPrimary) },
                    colors = chipColors,
                    border = chipBorder
                )

                FilterChip(
                    selected = selectedCommunity,
                    onClick = { selectedCommunity = !selectedCommunity },
                    label = { Text("Community", color = textPrimary) },
                    colors = chipColors,
                    border = chipBorder
                )

                FilterChip(
                    selected = selectedWheelchair,
                    onClick = { selectedWheelchair = !selectedWheelchair },
                    label = { Text("Wheelchair Access", color = textPrimary) },
                    colors = chipColors,
                    border = chipBorder
                )

                FilterChip(
                    selected = selectedDogBowl,
                    onClick = { selectedDogBowl = !selectedDogBowl },
                    label = { Text("Dog Bowl", color = textPrimary) },
                    colors = chipColors,
                    border = chipBorder
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
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
            containerColor = surfaceColor, // black button
            contentColor = textPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_my_location_pin),
                contentDescription = "Find my location",
                modifier = Modifier.size(46.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
