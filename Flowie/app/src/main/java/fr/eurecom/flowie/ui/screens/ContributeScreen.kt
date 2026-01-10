package fr.eurecom.flowie.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import fr.eurecom.flowie.ui.components.MapTilerMap
import com.mapbox.mapboxsdk.geometry.LatLng

/*
 * Screen allowing users to contribute a new water spot.
 * Includes a map preview, form inputs, image upload (camera/gallery),
 * and accessibility information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeScreen() {
    var markerPosition by remember {
        mutableStateOf(LatLng(48.2082, 16.3738))
    }
    // --- Form States ---
    var spotName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Water Fountain") }
    var expanded by remember { mutableStateOf(false) }
    var isAccessible by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

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
        if (success) {
            imageUri = cameraImageUri
        }
        showImagePicker = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraImageUri = createImageUri(context)
            cameraLauncher.launch(cameraImageUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            "Add a Water Spot",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Map preview ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            MapTilerMap(
                modifier = Modifier.matchParentSize(),
                center = markerPosition,
                zoom = 15.0,
                onMapReady = { /* no op for now */ }
            )

        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Form ---

        OutlinedTextField(
            value = spotName,
            onValueChange = { spotName = it },
            label = { Text("Spot name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- Dropdown Type ---
        Box {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                label = { Text("Type") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val types = listOf(
                    "Water Fountain",
                    "Restaurant / Bar",
                    "Paid Tap",
                    "Public Restroom",
                    "Bottle Refill"
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isAccessible,
                onCheckedChange = { isAccessible = it }
            )
            Text("Wheelchair accessible", color = Color.White)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showImagePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp)
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

        Button(
            onClick = { /* submit later */ },
            enabled = false, // activation quand backend + valid
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Submit spot")
        }
    }

    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Add image") },
            text = { Text("Choose source:") },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) {
                    Text("Camera")
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

    // Crée un fichier unique
    val imageFile = File(imagesDir, "camera_image_${System.currentTimeMillis()}.jpg")

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}