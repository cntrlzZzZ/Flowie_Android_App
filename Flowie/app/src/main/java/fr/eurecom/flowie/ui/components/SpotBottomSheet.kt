package fr.eurecom.flowie.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.eurecom.flowie.model.WaterSpot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotBottomSheet(
    spot: WaterSpot,
    onClose: () -> Unit
) {
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB3B3C2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = spot.name,
            style = MaterialTheme.typography.headlineSmall,
            color = textPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = spot.type,
            color = textSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(12.dp))

        if (spot.isAccessible) {
            AssistChip(
                onClick = {},
                label = { Text("Wheelchair accessible") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = elevatedSurfaceColor,
                    labelColor = textPrimary
                ),
                border = BorderStroke(1.dp, borderColor)
            )
            Spacer(Modifier.height(12.dp))
        }

        Text(
            text = spot.description,
            color = textPrimary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = onClose, // ✅ now actually closes
            colors = ButtonDefaults.buttonColors(
                containerColor = accentBlue,
                contentColor = textPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close")
        }

        Spacer(Modifier.height(10.dp))
    }
}
