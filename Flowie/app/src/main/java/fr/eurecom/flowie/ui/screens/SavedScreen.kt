package fr.eurecom.flowie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.eurecom.flowie.data.model.SpotDto
import fr.eurecom.flowie.data.remote.SourcesRepository
import fr.eurecom.flowie.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen() {
    val backgroundColor = Color(0xFF0B0B0F)
    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)

    val context = LocalContext.current
    val repo = remember { SourcesRepository() }

    // Saved ids (local)
    var savedIds by remember { mutableStateOf(SavedSpotsStore.getIds(context)) }

    // Loaded saved spots
    var spots by remember { mutableStateOf<List<SpotDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Filters
    var filterState by remember { mutableStateOf(SpotFilterState()) }

    // Sheet spot
    var selectedSpot by remember { mutableStateOf<SpotDto?>(null) }

    // Reload from server whenever savedIds change
    LaunchedEffect(savedIds) {
        isLoading = true
        spots = try {
            repo.fetchByIds(savedIds.toList())
        } catch (_: Exception) {
            emptyList()
        }
        isLoading = false
    }

    val filteredSpots by remember(spots, filterState) {
        derivedStateOf { applySpotFilters(spots, filterState) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SpotFilterBar(
                state = filterState,
                onStateChange = { filterState = it },
                placeholder = "Search saved places",
                surfaceColor = surfaceColor,
                elevatedSurfaceColor = elevatedSurfaceColor,
                borderColor = borderColor,
                accentBlue = accentBlue,
                textPrimary = textPrimary
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Text("Loading saved spots…", color = textPrimary)
                return@Column
            }

            if (filteredSpots.isEmpty()) {
                Text("No saved spots yet.", color = textPrimary)
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredSpots, key = { it.id }) { spot ->
                    SavedSpotCard(
                        spot = spot,
                        surfaceColor = surfaceColor,
                        borderColor = borderColor,
                        textPrimary = textPrimary,
                        onClick = { selectedSpot = spot }
                    )
                }
            }
        }

        // Bottom sheet
        selectedSpot?.let { spot ->
            SpotDetailsBottomSheet(
                spot = spot,
                isSaved = savedIds.contains(spot.id),
                onToggleSave = {
                    val newIds = SavedSpotsStore.toggle(context, spot.id)
                    savedIds = newIds
                    if (!newIds.contains(spot.id)) selectedSpot = null // if you unsave, close
                },
                onDismiss = { selectedSpot = null }
            )
        }
    }
}

@Composable
private fun SavedSpotCard(
    spot: SpotDto,
    surfaceColor: Color,
    borderColor: Color,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = spot.address ?: "Water spot",
                color = textPrimary,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = prettySubtitle(spot),
                color = textPrimary.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun prettySubtitle(spot: SpotDto): String {
    val bits = mutableListOf<String>()
    bits += if (spot.origin == "verified") "Verified" else "Community"
    if (spot.wheelchairAccess == true) bits += "Wheelchair"
    if (spot.dogBowl == true) bits += "Dog bowl"
    return bits.joinToString(", ")
}
