package fr.eurecom.flowie.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * Bottom navigation bar used for main app navigation.
 */
@Composable
fun FilterChipSelectable(
    text: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onSelectedChange(!selected) },
        color = if (selected) Color(0xFF4CAF50) else Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}