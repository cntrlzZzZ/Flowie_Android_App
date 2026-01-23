package fr.eurecom.flowie.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.eurecom.flowie.data.model.SpotDto
import java.util.Locale

data class SpotFilterState(
    val searchText: String = "",
    val verified: Boolean = false,
    val community: Boolean = false,
    val wheelchair: Boolean = false,
    val dogBowl: Boolean = false,
)

fun applySpotFilters(spots: List<SpotDto>, state: SpotFilterState): List<SpotDto> {
    val q = state.searchText.trim().lowercase(Locale.getDefault())

    return spots
        .asSequence()
        .filter { s ->
            if (q.isEmpty()) true
            else {
                val hay = buildString {
                    append(s.address ?: "")
                    append(" ")
                    append(s.typeLabel)
                }.lowercase(Locale.getDefault())
                hay.contains(q)
            }
        }
        .filter { s ->
            val anyOriginFilter = state.verified || state.community
            if (!anyOriginFilter) return@filter true
            if (state.verified && state.community) return@filter true

            if (state.verified) s.origin == "verified" else s.origin == "community"
        }
        .filter { s ->
            if (!state.wheelchair) true else (s.wheelchairAccess == true)
        }
        .filter { s ->
            if (!state.dogBowl) true else (s.dogBowl == true)
        }
        .toList()
}

@Composable
fun SpotFilterBar(
    state: SpotFilterState,
    onStateChange: (SpotFilterState) -> Unit,
    placeholder: String,
    surfaceColor: Color,
    elevatedSurfaceColor: Color,
    borderColor: Color,
    accentBlue: Color,
    textPrimary: Color
) {
    OutlinedTextField(
        value = state.searchText,
        onValueChange = { onStateChange(state.copy(searchText = it)) },
        placeholder = { Text(text = placeholder, color = textPrimary) },
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .horizontalScroll(rememberScrollState()),
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
            selected = state.verified,
            onClick = { onStateChange(state.copy(verified = !state.verified)) },
            label = { Text("Verified", color = textPrimary) },
            colors = chipColors,
            border = chipBorder
        )

        FilterChip(
            selected = state.community,
            onClick = { onStateChange(state.copy(community = !state.community)) },
            label = { Text("Community", color = textPrimary) },
            colors = chipColors,
            border = chipBorder
        )

        FilterChip(
            selected = state.wheelchair,
            onClick = { onStateChange(state.copy(wheelchair = !state.wheelchair)) },
            label = { Text("Wheelchair Access", color = textPrimary) },
            colors = chipColors,
            border = chipBorder
        )

        FilterChip(
            selected = state.dogBowl,
            onClick = { onStateChange(state.copy(dogBowl = !state.dogBowl)) },
            label = { Text("Dog Bowl", color = textPrimary) },
            colors = chipColors,
            border = chipBorder
        )
    }
}
