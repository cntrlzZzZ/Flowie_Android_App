package fr.eurecom.flowie.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.eurecom.flowie.ui.components.MapTilerMap
import java.io.File

/*
 * Contribute screen:
 * - List view: search bar + filter chips + cards
 * - Empty state: message + button to open form
 * - Form view: existing contribute form
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeScreen() {
    // Dark theme colours
    val backgroundColor = Color(0xFF0B0B0F)
    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB3B3C2)

    // ✅ START EMPTY (remove fake contributed spot)
    val contributedSpots = remember { mutableStateListOf<ContributedSpot>() }

    // Toggle between list view and form view
    var showForm by remember { mutableStateOf(false) }

    // List view state
    var searchQuery by remember { mutableStateOf("") }
    var filterCommunity by remember { mutableStateOf(false) }
    var filterVerified by remember { mutableStateOf(false) }
    var filterWheelchair by remember { mutableStateOf(false) }
    var filterDogBowl by remember { mutableStateOf(false) }

    // ---------------------------
    // LIST VIEW (default)
    // ---------------------------
    if (!showForm) {
        val tfColors = TextFieldDefaults.colors(
            unfocusedContainerColor = surfaceColor,
            focusedContainerColor = surfaceColor,
            unfocusedTextColor = textPrimary,
            focusedTextColor = textPrimary,
            unfocusedPlaceholderColor = textSecondary,
            focusedPlaceholderColor = textSecondary,
            unfocusedIndicatorColor = borderColor,
            focusedIndicatorColor = accentBlue,
            cursorColor = accentBlue
        )

        val chipColors = FilterChipDefaults.filterChipColors(
            containerColor = elevatedSurfaceColor,
            labelColor = textPrimary,
            selectedContainerColor = accentBlue,
            selectedLabelColor = textPrimary
        )
        val chipBorder = BorderStroke(1.dp, borderColor)

        val filteredSpots = remember(
            contributedSpots,
            searchQuery,
            filterCommunity,
            filterVerified,
            filterWheelchair,
            filterDogBowl
        ) {
            contributedSpots
                .asSequence()
                .filter { spot ->
                    if (searchQuery.isBlank()) true
                    else spot.name.contains(searchQuery, ignoreCase = true)
                }
                .filter { spot -> !filterCommunity || spot.community }
                .filter { spot -> !filterVerified || spot.verified }
                .filter { spot -> !filterWheelchair || spot.wheelchair }
                .filter { spot -> !filterDogBowl || spot.dogBowl }
                .toList()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            Text(
                text = "Contributed spots",
                color = textPrimary,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = tfColors,
                placeholder = { Text("Search your contributed spots", color = textSecondary) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterCommunity,
                    onClick = { filterCommunity = !filterCommunity },
                    label = { Text("Community") },
                    colors = chipColors,
                    border = chipBorder
                )
                FilterChip(
                    selected = filterVerified,
                    onClick = { filterVerified = !filterVerified },
                    label = { Text("Verified") },
                    colors = chipColors,
                    border = chipBorder
                )
                FilterChip(
                    selected = filterWheelchair,
                    onClick = { filterWheelchair = !filterWheelchair },
                    label = { Text("Wheelchair") },
                    colors = chipColors,
                    border = chipBorder
                )
                FilterChip(
                    selected = filterDogBowl,
                    onClick = { filterDogBowl = !filterDogBowl },
                    label = { Text("Dog Bowl") },
                    colors = chipColors,
                    border = chipBorder
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = surfaceColor,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    contributedSpots.isEmpty() -> {
                        EmptyContributeState(
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accentBlue = accentBlue,
                            onContribute = { showForm = true }
                        )
                    }

                    filteredSpots.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No spots match your search/filters",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            filteredSpots.forEach { spot ->
                                ContributedSpotCard(
                                    spot = spot,
                                    elevatedSurfaceColor = elevatedSurfaceColor,
                                    borderColor = borderColor,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { showForm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentBlue,
                    contentColor = textPrimary
                )
            ) {
                Text("Contribute a spot")
            }
        }

        return
    }

    // ---------------------------
    // FORM VIEW
    // ---------------------------

    var markerPosition by remember { mutableStateOf(LatLng(48.2082, 16.3738)) }

    // --- Form States ---
    var spotName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Water Fountain") }
    var expanded by remember { mutableStateOf(false) }

    var isWheelchairAccessible by remember { mutableStateOf(false) }
    var hasDogBowl by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val canSubmit = spotName.trim().isNotEmpty() &&
            description.trim().isNotEmpty() &&
            imageUri != null

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        showImagePicker = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) imageUri = cameraImageUri
        showImagePicker = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val newImageUri = createImageUri(context)
            cameraImageUri = newImageUri
            cameraLauncher.launch(newImageUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ✅ Header with cross instead of Cancel text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Add a Water Spot",
                color = textPrimary,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )

            FilledIconButton(
                onClick = { showForm = false },
                modifier = Modifier.size(42.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = elevatedSurfaceColor,
                    contentColor = textPrimary
                )
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(surfaceColor)
        ) {
            MapTilerMap(
                modifier = Modifier.matchParentSize(),
                center = markerPosition,
                zoom = 15.0,
                onMapReady = { /* no-op */ }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val tfColors = TextFieldDefaults.colors(
            unfocusedContainerColor = surfaceColor,
            focusedContainerColor = surfaceColor,
            unfocusedTextColor = textPrimary,
            focusedTextColor = textPrimary,
            unfocusedPlaceholderColor = textSecondary,
            focusedPlaceholderColor = textSecondary,
            unfocusedIndicatorColor = borderColor,
            focusedIndicatorColor = accentBlue,
            cursorColor = accentBlue
        )

        OutlinedTextField(
            value = spotName,
            onValueChange = { spotName = it },
            label = { Text("Enter address", color = textSecondary) },
            placeholder = { Text("Enter address", color = textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = tfColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                label = { Text("Type", color = textSecondary) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = tfColors,
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = textPrimary
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val types = listOf("Water Fountain", "Paid Tap", "Public Restroom")
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Options",
            color = textPrimary,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = surfaceColor,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isWheelchairAccessible,
                        onCheckedChange = { isWheelchairAccessible = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = accentBlue,
                            uncheckedColor = textSecondary,
                            checkmarkColor = textPrimary
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Wheelchair accessible", color = textPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasDogBowl,
                        onCheckedChange = { hasDogBowl = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = accentBlue,
                            uncheckedColor = textSecondary,
                            checkmarkColor = textPrimary
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Dog bowl available", color = textPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description", color = textSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            shape = RoundedCornerShape(14.dp),
            colors = tfColors
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showImagePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentBlue,
                contentColor = textPrimary
            )
        ) {
            Text("Add Image")
        }

        Spacer(Modifier.height(12.dp))

        imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                contributedSpots.add(
                    ContributedSpot(
                        name = spotName.trim(),
                        type = selectedType,
                        community = true,     // contributions are basically “community”
                        verified = false,
                        wheelchair = isWheelchairAccessible,
                        dogBowl = hasDogBowl,
                        imageResId = null
                    )
                )

                spotName = ""
                selectedType = "Water Fountain"
                expanded = false
                isWheelchairAccessible = false
                hasDogBowl = false
                description = ""
                imageUri = null
                cameraImageUri = null

                showForm = false
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentBlue,
                contentColor = textPrimary,
                disabledContainerColor = Color(0xFF2A2A35),
                disabledContentColor = Color(0xFFB3B3C2)
            )
        ) {
            Text("Submit spot")
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            containerColor = surfaceColor,
            titleContentColor = textPrimary,
            textContentColor = textSecondary,
            title = { Text("Add image") },
            text = { Text("Choose source:") },
            confirmButton = {
                TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("Gallery", color = accentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) {
                    Text("Camera", color = accentBlue)
                }
            }
        )
    }
}

@Composable
private fun EmptyContributeState(
    textPrimary: Color,
    textSecondary: Color,
    accentBlue: Color,
    onContribute: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You currently have no contributions — add one now!",
            color = textSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onContribute,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentBlue,
                contentColor = textPrimary
            )
        ) {
            Text("Contribute a spot")
        }
    }
}

@Composable
private fun ContributedSpotCard(
    spot: ContributedSpot,
    elevatedSurfaceColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        color = elevatedSurfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            spot.imageResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Spot photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.height(12.dp))
            }

            Text(text = spot.name, color = textPrimary, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(text = spot.type, color = textSecondary, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (spot.community) AssistChip(
                    onClick = {},
                    label = { Text("Community") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF0F0F16),
                        labelColor = textPrimary
                    ),
                    border = BorderStroke(1.dp, borderColor)
                )
                if (spot.verified) AssistChip(
                    onClick = {},
                    label = { Text("Verified") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF0F0F16),
                        labelColor = textPrimary
                    ),
                    border = BorderStroke(1.dp, borderColor)
                )
                if (spot.wheelchair) AssistChip(
                    onClick = {},
                    label = { Text("Wheelchair") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF0F0F16),
                        labelColor = textPrimary
                    ),
                    border = BorderStroke(1.dp, borderColor)
                )
                if (spot.dogBowl) AssistChip(
                    onClick = {},
                    label = { Text("Dog Bowl") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF0F0F16),
                        labelColor = textPrimary
                    ),
                    border = BorderStroke(1.dp, borderColor)
                )
            }
        }
    }
}

/*
 * Local UI model for now
 */
data class ContributedSpot(
    val name: String,
    val type: String,
    val community: Boolean,
    val verified: Boolean,
    val wheelchair: Boolean,
    val dogBowl: Boolean,
    val imageResId: Int? = null
)

/*
 * Creates a temporary image URI used to store a photo taken by the camera.
 */
fun createImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images")
    imagesDir.mkdirs()
    val imageFile = File(imagesDir, "camera_image_${System.currentTimeMillis()}.jpg")

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}
