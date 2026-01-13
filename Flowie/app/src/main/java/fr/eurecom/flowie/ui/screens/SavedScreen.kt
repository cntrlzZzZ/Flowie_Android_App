package fr.eurecom.flowie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.eurecom.flowie.model.WaterSpot
import fr.eurecom.flowie.ui.components.SpotBottomSheet
import kotlinx.coroutines.launch

/*
 * Screen displaying the list of saved water spots.
 * Includes filters and a bottom sheet with detailed information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen() {

    // Dark theme colours (same as Explore)
    val backgroundColor = Color(0xFF0B0B0F)
    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB3B3C2)

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

    // Filter states
    var verified by remember { mutableStateOf(false) }
    var community by remember { mutableStateOf(false) }
    var accessible by remember { mutableStateOf(false) }

    // Search
    var searchText by remember { mutableStateOf("") }

    // Bottom sheet state
    var selectedSpot by remember { mutableStateOf<WaterSpot?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Search bar (dark + white text)
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search saved places", color = textPrimary) },
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

        // Filters (dark, consistent, swipeable)
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
                selected = verified,
                onClick = { verified = !verified },
                label = { Text("Verified", color = textPrimary) },
                colors = chipColors,
                border = chipBorder
            )

            FilterChip(
                selected = community,
                onClick = { community = !community },
                label = { Text("Community", color = textPrimary) },
                colors = chipColors,
                border = chipBorder
            )

            FilterChip(
                selected = accessible,
                onClick = { accessible = !accessible },
                label = { Text("Wheelchair Access", color = textPrimary) },
                colors = chipColors,
                border = chipBorder
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // (Optional) quick filtering based on toggles + search text
        val filteredPlaces = remember(savedPlaces, searchText, verified, community, accessible) {
            savedPlaces.filter { spot ->
                val matchesSearch = searchText.isBlank() ||
                        spot.name.contains(searchText, ignoreCase = true) ||
                        spot.type.contains(searchText, ignoreCase = true)

                val matchesVerified = !verified || spot.type.contains("Verified", ignoreCase = true)
                val matchesCommunity = !community || spot.type.contains("Community", ignoreCase = true)
                val matchesAccessible = !accessible || spot.isAccessible

                matchesSearch && matchesVerified && matchesCommunity && matchesAccessible
            }
        }

        // Saved list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPlaces) { spot ->
                SavedPlaceCard(
                    spot = spot,
                    surfaceColor = surfaceColor,
                    borderColor = borderColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                ) {
                    selectedSpot = spot
                    showSheet = true
                }
            }
        }
    }

    // Bottom sheet
    if (showSheet && selectedSpot != null) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    showSheet = false
                    selectedSpot = null
                }
            },
            sheetState = sheetState,
            containerColor = surfaceColor
        ) {
            SpotBottomSheet(
                spot = selectedSpot!!,
                onClose = {
                    scope.launch {
                        sheetState.hide()
                        showSheet = false
                        selectedSpot = null
                    }
                }
            )
        }
    }
}

/*
 * Card representing a saved water spot in the list.
 * Clicking the card opens the bottom sheet.
 */
@Composable
fun SavedPlaceCard(
    spot: WaterSpot,
    surfaceColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
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
                    .background(Color(0xFF1A1A24), RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spot.name,
                    color = textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = spot.type,
                    color = textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
