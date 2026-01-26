@file:OptIn(ExperimentalMaterial3Api::class)

package fr.eurecom.flowie.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.eurecom.flowie.BuildConfig
import fr.eurecom.flowie.data.model.SpotDto

private const val STORAGE_BUCKET = "spots-images"

/**
 * Builds a PUBLIC storage URL from whatever you store in image_path.
 *
 * Supports:
 * - null / blank -> null
 * - full URL (http/https) -> returned as-is
 * - full public path containing "/storage/v1/object/public/" -> normalised
 * - plain object path "vienna/abc.jpg" -> converted to public URL
 * - accidentally stored "spots-images/vienna/abc.jpg" -> bucket prefix stripped
 */
private fun spotPublicImageUrl(imagePathRaw: String?): String? {
    val raw = imagePathRaw?.trim().orEmpty()
    if (raw.isEmpty()) return null

    // Full URL already
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw

    // If someone stored the public path itself
    if (raw.contains("/storage/v1/object/public/")) {
        val base = BuildConfig.SUPABASE_URL.trimEnd('/')
        return if (raw.startsWith("/")) "$base$raw" else raw
    }

    // Otherwise assume it's an object path in the bucket
    var path = raw.removePrefix("/")

    // If stored with bucket prefix, strip it
    if (path.startsWith("$STORAGE_BUCKET/")) path = path.removePrefix("$STORAGE_BUCKET/")

    val base = BuildConfig.SUPABASE_URL.trimEnd('/')
    val safePath = Uri.encode(path, "/")
    return "$base/storage/v1/object/public/$STORAGE_BUCKET/$safePath"
}

private fun openNavigation(context: Context, lat: Double, lng: Double, label: String?) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("google.navigation:q=$lat,$lng")
    ).apply { setPackage("com.google.android.apps.maps") }

    val fallback = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label ?: "Water spot")})")
    )

    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        context.startActivity(fallback)
    }
}

private fun prettyTypeLabel(typeLabel: String): String = when (typeLabel) {
    "outdoor_water_fountain" -> "Outdoor Water Fountain"
    "indoor_water_fountain" -> "Indoor Water Fountain"
    else -> typeLabel.replace('_', ' ')
        .split(' ')
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

private fun prettyOrigin(origin: String): String = when (origin) {
    "verified" -> "Verified"
    "community" -> "Community"
    else -> origin.replaceFirstChar { it.uppercase() }
}

private fun prettyStatus(status: String): String = when (status) {
    "active" -> "Active"
    "inactive" -> "Inactive"
    else -> status.replaceFirstChar { it.uppercase() }
}

@Composable
fun SpotDetailsBottomSheet(
    spot: SpotDto,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB3B3C2)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val subtitle = remember(spot.typeLabel, spot.status, spot.origin) {
        "${prettyTypeLabel(spot.typeLabel)} • ${prettyStatus(spot.status)} • ${prettyOrigin(spot.origin)}"
    }
    val imageUrl = remember(spot.imagePath) { spotPublicImageUrl(spot.imagePath) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = textSecondary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // HEADER: title + Save + X
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spot.address ?: "Water spot",
                        style = MaterialTheme.typography.headlineSmall,
                        color = textPrimary
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = subtitle,
                        color = textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = elevatedSurfaceColor,
                            contentColor = textPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Unsave" else "Save"
                        )
                    }

                    FilledIconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = elevatedSurfaceColor,
                            contentColor = textPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss"
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // IMAGE (only if we can build a URL)
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Spot photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.height(14.dp))
            }

            // CHIPS
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (spot.wheelchairAccess == true) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Wheelchair accessible") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = elevatedSurfaceColor,
                            labelColor = textPrimary
                        ),
                        border = BorderStroke(1.dp, borderColor)
                    )
                }

                if (spot.dogBowl == true) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Dog bowl") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = elevatedSurfaceColor,
                            labelColor = textPrimary
                        ),
                        border = BorderStroke(1.dp, borderColor)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ACTION
            Button(
                onClick = {
                    openNavigation(
                        context = context,
                        lat = spot.lat,
                        lng = spot.lng,
                        label = spot.address
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentBlue,
                    contentColor = textPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Navigate")
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}
