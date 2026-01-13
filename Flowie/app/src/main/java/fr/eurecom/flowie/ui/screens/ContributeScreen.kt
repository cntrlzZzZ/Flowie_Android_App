package fr.eurecom.flowie.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.eurecom.flowie.ui.components.MapTilerMap
import java.io.File

/*
 * Screen allowing users to contribute a new water spot.
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

    // ✅ Submit enabled only when name + description + photo exist
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

        Text(
            "Add a Water Spot",
            color = textPrimary,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Map preview ---
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

        // Shared TextField styling
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

        // --- Spot name ---
        OutlinedTextField(
            value = spotName,
            onValueChange = { spotName = it },
            label = { Text("Spot name", color = textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = tfColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- Dropdown Type ---
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
                // ✅ Only keep these types
                val types = listOf(
                    "Water Fountain",
                    "Paid Tap",
                    "Public Restroom"
                )

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

        // --- Options ---
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

        // --- Description ---
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

        // ✅ Add Image button (blue)
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

        // ✅ Submit: blue only when valid, otherwise grey + disabled
        Button(
            onClick = { /* submit later */ },
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

    // Image picker dialog (dark)
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
