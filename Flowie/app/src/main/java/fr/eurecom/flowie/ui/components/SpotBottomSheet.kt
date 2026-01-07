package fr.eurecom.flowie.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.eurecom.flowie.model.WaterSpot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotBottomSheet(spot: WaterSpot) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = spot.name,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black
        )

        Spacer(Modifier.height(8.dp))

        Text(spot.type, color = Color.Black)

        Spacer(Modifier.height(12.dp))

        if (spot.isAccessible) {
            AssistChip(
                onClick = {},
                label = { Text("Wheelchair accessible") }
            )
            Spacer(Modifier.height(12.dp))
        }

        Text(
            text = spot.description,
            color = Color.Black
        )

        Spacer(Modifier.height(30.dp))
    }
}