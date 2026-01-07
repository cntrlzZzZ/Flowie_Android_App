package fr.eurecom.flowie.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.style.TextOverflow
import fr.eurecom.flowie.model.WaterSpot
import fr.eurecom.flowie.ui.components.FilterChipSelectable
import fr.eurecom.flowie.ui.components.SpotBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen() {
    // Fake data for UI
    val savedPlaces = listOf(
        WaterSpot(
            name = "Fresh Water Tap – 3rd District",
            type = "Verified, Wheelchair access",
            isAccessible = true,
            description = "Cold drinking water fountain located near the main square."
        ),
        WaterSpot(
            name = "Park Drinking Fountain – Karlsplatz",
            type = "Community spot",
            isAccessible = false,
            description = "Small fountain, community-confirmed."
        ),
        WaterSpot(
            name = "Public Restroom – MuseumsQuartier",
            type = "Verified",
            isAccessible = false,
            description = "Clean restroom with tap water."
        ),
    )

    var verified by remember { mutableStateOf(false) }
    var community by remember { mutableStateOf(false) }
    var accessible by remember { mutableStateOf(false) }

    var selectedSpot by remember { mutableStateOf<WaterSpot?>(null) }   //celle ci
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar (same as Explore)
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search saved places") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipSelectable("Verified", verified) { verified = it }
            FilterChipSelectable("Community", community) { community = it }
            FilterChipSelectable("Wheelchair Access", accessible) { accessible = it }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Saved list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(savedPlaces) { spot ->
                SavedPlaceCard(spot = spot) {
                    selectedSpot = spot  // celle ci
                    showSheet = true
                }
            }
        }
    }
    // Bottom sheet (reusable component)
    if (showSheet && selectedSpot != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                selectedSpot = null
            },
            sheetState = sheetState
        ) {
            SpotBottomSheet(spot = selectedSpot!!)
        }
    }
}

@Composable
fun SavedPlaceCard(spot: WaterSpot, onClick: () -> Unit) {
    Surface(
        onClick = onClick, // ✅ ICI
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF5F5F5),
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFDDDDDD), RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spot.name,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = spot.type,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
