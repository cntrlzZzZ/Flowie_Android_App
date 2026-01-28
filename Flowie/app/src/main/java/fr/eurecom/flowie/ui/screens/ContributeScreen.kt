package fr.eurecom.flowie.ui.screens

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.eurecom.flowie.BuildConfig
import fr.eurecom.flowie.data.model.SpotDto
import fr.eurecom.flowie.data.remote.AuthManager
import fr.eurecom.flowie.data.remote.SourcesRepository
import fr.eurecom.flowie.ui.components.MapTilerMap
import fr.eurecom.flowie.ui.components.SavedSpotsStore
import fr.eurecom.flowie.ui.components.SpotDetailsBottomSheet
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private enum class SpotType(val dbValue: String, val label: String) {
    Outdoor("outdoor_water_fountain", "Outdoor water fountain"),
    Indoor("indoor_water_fountain", "Indoor water fountain")
}

private enum class SpotStatus(val dbValue: String, val label: String) {
    Active("active", "Active"),
    Inactive("inactive", "Inactive")
}

/**
 * ✅ Fix: save/restore a MutableState<LatLng> so `var mapCenter by ...` works.
 * `by` delegation works for State<T>, not for raw LatLng.
 */
private val LatLngStateSaver: Saver<MutableState<LatLng>, List<Double>> = Saver(
    save = { state -> listOf(state.value.latitude, state.value.longitude) },
    restore = { list -> mutableStateOf(LatLng(list[0], list[1])) }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeScreen(
    onLoginRequested: () -> Unit
) {
    // Dark theme colours
    val backgroundColor = Color(0xFF0B0B0F)
    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB3B3C2)

    // Auth gate
    val sessionStatus by AuthManager.sessionStatus.collectAsState()
    val isLoggedIn = sessionStatus is SessionStatus.Authenticated
    val userId = remember(sessionStatus) { AuthManager.currentUserIdOrNull() }

    if (!isLoggedIn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Contribute", color = textPrimary, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "You can’t contribute unless you log in.",
                color = textSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLoginRequested,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
            ) {
                Text("Register / login to contribute")
            }
        }
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { SourcesRepository() }

    // Saved ids (for the bottom sheet Save button)
    var savedIds by remember { mutableStateOf(SavedSpotsStore.getIds(context)) }

    // Keep BOTH:
    // - the DTOs (for bottom sheet)
    // - the UI list (for your card rendering)
    var mySpotDtos by remember { mutableStateOf<List<SpotDto>>(emptyList()) }
    val contributedSpots = remember { mutableStateListOf<ContributedSpot>() }

    // Bottom sheet state
    var selectedSpot by remember { mutableStateOf<SpotDto?>(null) }
    var isOpeningSpot by remember { mutableStateOf(false) }

    // ✅ IMPORTANT: save this across camera activity recreation
    var showForm by rememberSaveable { mutableStateOf(false) }

    // Fetch state for "My contributions"
    var isLoadingMyContrib by remember { mutableStateOf(false) }
    var myContribError by remember { mutableStateOf<String?>(null) }

    fun refreshMyContributions() {
        val uid = userId
        if (uid.isNullOrBlank()) {
            myContribError = "Missing user id (not logged in properly)."
            return
        }

        scope.launch {
            isLoadingMyContrib = true
            myContribError = null
            try {
                val spots = repo.fetchMyCommunitySources(uid)
                mySpotDtos = spots

                contributedSpots.clear()
                contributedSpots.addAll(spots.map { it.toContributedSpot() })
            } catch (e: Exception) {
                myContribError = e.message ?: "Unknown error"
            } finally {
                isLoadingMyContrib = false
            }
        }
    }

    // ✅ Fetch when we land on the list view (and when coming back from the form)
    LaunchedEffect(userId, showForm) {
        if (!showForm && userId != null) {
            refreshMyContributions()
        }
    }

    // List view state
    var searchQuery by remember { mutableStateOf("") }
    var filterActive by remember { mutableStateOf(false) }
    var filterInactive by remember { mutableStateOf(false) }
    var filterWheelchair by remember { mutableStateOf(false) }
    var filterDogBowl by remember { mutableStateOf(false) }

    suspend fun openSpotBottomSheet(spotId: String) {
        // 1) try cache
        val cached = mySpotDtos.firstOrNull { it.id == spotId }
        if (cached != null) {
            selectedSpot = cached
            return
        }

        // 2) fallback fetch
        isOpeningSpot = true
        try {
            val fetched = repo.fetchById(spotId)
            if (fetched != null) {
                selectedSpot = fetched
            } else {
                Toast.makeText(context, "Couldn’t load this spot.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Couldn’t open spot: ${e.message ?: "Unknown error"}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            isOpeningSpot = false
        }
    }

    // ---------------------------
    // LIST VIEW
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
            filterActive,
            filterInactive,
            filterWheelchair,
            filterDogBowl
        ) {
            contributedSpots
                .asSequence()
                .filter { spot ->
                    if (searchQuery.isBlank()) true
                    else spot.name.contains(searchQuery, ignoreCase = true)
                }
                // ✅ Active/Inactive chips
                .filter { spot ->
                    val anyStatusFilter = filterActive || filterInactive
                    if (!anyStatusFilter) return@filter true
                    if (filterActive && filterInactive) return@filter true
                    if (filterActive) spot.status == "active" else spot.status == "inactive"
                }
                .filter { spot -> !filterWheelchair || spot.wheelchair }
                .filter { spot -> !filterDogBowl || spot.dogBowl }
                .toList()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "My contributions",
                        color = textPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = { refreshMyContributions() },
                        enabled = !isLoadingMyContrib
                    ) {
                        Text("Refresh", color = accentBlue)
                    }
                }

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

                // ✅ Community is implicit (always on). ✅ Verified chip removed.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterActive,
                        onClick = { filterActive = !filterActive },
                        label = { Text("Active") },
                        colors = chipColors,
                        border = chipBorder
                    )
                    FilterChip(
                        selected = filterInactive,
                        onClick = { filterInactive = !filterInactive },
                        label = { Text("Inactive") },
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
                        isLoadingMyContrib -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = accentBlue)
                            }
                        }

                        myContribError != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Couldn’t load contributions:\n${myContribError!!}",
                                        color = textSecondary,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { refreshMyContributions() },
                                        border = BorderStroke(1.dp, borderColor),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                                    ) {
                                        Text("Try again")
                                    }
                                }
                            }
                        }

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
                                        textSecondary = textSecondary,
                                        onClick = {
                                            scope.launch { openSpotBottomSheet(spot.id) }
                                        }
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

            // Optional tiny loading overlay while fetching a spot by id
            if (isOpeningSpot) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentBlue)
                }
            }

            // ✅ Option A: same bottom sheet, inside Contribute screen
            selectedSpot?.let { spot ->
                SpotDetailsBottomSheet(
                    spot = spot,
                    isSaved = savedIds.contains(spot.id),
                    onToggleSave = {
                        savedIds = SavedSpotsStore.toggle(context, spot)
                    },
                    onDismiss = { selectedSpot = null }
                )
            }
        }

        return
    }

    // ---------------------------
    // FORM VIEW
    // ---------------------------

    var mapCenter by rememberSaveable(saver = LatLngStateSaver) {
        mutableStateOf(LatLng(48.2082, 16.3738))
    }

    var address by rememberSaveable { mutableStateOf("") }
    var addressEditedByUser by rememberSaveable { mutableStateOf(false) }

    var isGeocoding by remember { mutableStateOf(false) }
    var lastGeocodedCenter by remember { mutableStateOf<LatLng?>(null) }
    var pendingGeocodeJob by remember { mutableStateOf<Job?>(null) }
    var lastPinAddress by remember { mutableStateOf<String?>(null) }

    var selectedType by rememberSaveable { mutableStateOf(SpotType.Outdoor) }
    var typeExpanded by remember { mutableStateOf(false) }

    var selectedStatus by rememberSaveable { mutableStateOf(SpotStatus.Active) }
    var statusExpanded by remember { mutableStateOf(false) }

    var isWheelchairAccessible by rememberSaveable { mutableStateOf(false) }
    var hasDogBowl by rememberSaveable { mutableStateOf(false) }

    var imageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraImageUriString by rememberSaveable { mutableStateOf<String?>(null) }

    val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }
    @Suppress("UNUSED_VARIABLE")
    val cameraImageUri: Uri? = cameraImageUriString?.let { Uri.parse(it) }

    var showImagePicker by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    val canSubmit = !isSubmitting && address.trim().isNotEmpty()

    fun requestReverseGeocode(center: LatLng) {
        val last = lastGeocodedCenter
        val movedTinyAmount =
            last != null && kotlin.math.abs(last.latitude - center.latitude) < 0.00005 &&
                    kotlin.math.abs(last.longitude - center.longitude) < 0.00005
        if (movedTinyAmount) return

        pendingGeocodeJob?.cancel()
        pendingGeocodeJob = scope.launch {
            delay(350)

            isGeocoding = true
            val result = reverseGeocodeToSingleLine(context, center)
            isGeocoding = false

            lastGeocodedCenter = center
            lastPinAddress = result

            if (!addressEditedByUser || address.isBlank()) {
                if (!result.isNullOrBlank()) {
                    address = result
                    addressEditedByUser = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { requestReverseGeocode(mapCenter) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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
                onClick = { if (!isSubmitting) showForm = false },
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
                center = mapCenter,
                zoom = 15.0,
                followUserLocation = false,
                onCameraIdle = { centre ->
                    mapCenter = centre
                    requestReverseGeocode(centre)
                }
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "Centre pin",
                    tint = accentBlue,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isGeocoding) "Finding address…" else "Move map to set address",
                    color = textPrimary.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = address,
            onValueChange = {
                address = it
                addressEditedByUser = true
            },
            label = { Text("Address", color = textSecondary) },
            placeholder = { Text("Use the map pin or type it", color = textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = tfColors,
            enabled = !isSubmitting
        )

        val pinAddr = lastPinAddress
        if (addressEditedByUser && !pinAddr.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        address = pinAddr
                        addressEditedByUser = false
                    },
                    enabled = !isSubmitting
                ) {
                    Text("Use pin address", color = accentBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box {
            OutlinedTextField(
                value = selectedType.label,
                onValueChange = {},
                label = { Text("Type", color = textSecondary) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = tfColors,
                enabled = !isSubmitting,
                trailingIcon = {
                    IconButton(onClick = { typeExpanded = true }, enabled = !isSubmitting) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = textPrimary)
                    }
                }
            )

            DropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                SpotType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        onClick = {
                            selectedType = type
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box {
            OutlinedTextField(
                value = selectedStatus.label,
                onValueChange = {},
                label = { Text("Status", color = textSecondary) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = tfColors,
                enabled = !isSubmitting,
                trailingIcon = {
                    IconButton(onClick = { statusExpanded = true }, enabled = !isSubmitting) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = textPrimary)
                    }
                }
            )

            DropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false }
            ) {
                SpotStatus.entries.forEach { st ->
                    DropdownMenuItem(
                        text = { Text(st.label) },
                        onClick = {
                            selectedStatus = st
                            statusExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Options", color = textPrimary, style = MaterialTheme.typography.titleMedium)
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
                        enabled = !isSubmitting,
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
                        enabled = !isSubmitting,
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

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showImagePicker = true },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF000000),
                contentColor = textPrimary,
                disabledContainerColor = Color(0xFF2A2A35),
                disabledContentColor = Color(0xFFB3B3C2)
            )
        ) {
            Text(if (imageUri == null) "Add Image (optional)" else "Change Image")
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

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    imageUriString = null
                    cameraImageUriString = null
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
            ) {
                Text("Remove image")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    isSubmitting = true
                    try {
                        val inserted = repo.createCommunitySource(
                            context = context,
                            address = address.trim(),
                            lat = mapCenter.latitude,
                            lng = mapCenter.longitude,
                            typeLabel = selectedType.dbValue,
                            status = selectedStatus.dbValue,
                            wheelchairAccess = if (isWheelchairAccessible) true else null,
                            dogBowl = if (hasDogBowl) true else null,
                            imageUri = imageUri
                        )

                        // Keep list feeling instant (and also refresh after closing the form)
                        // Also keep DTO cache in sync (for bottom sheet)
                        mySpotDtos = listOf(inserted) + mySpotDtos
                        contributedSpots.add(0, inserted.toContributedSpot())

                        Toast.makeText(context, "Submitted ✅", Toast.LENGTH_SHORT).show()

                        address = ""
                        addressEditedByUser = false
                        lastPinAddress = null
                        lastGeocodedCenter = null

                        selectedType = SpotType.Outdoor
                        typeExpanded = false

                        selectedStatus = SpotStatus.Active
                        statusExpanded = false

                        isWheelchairAccessible = false
                        hasDogBowl = false

                        imageUriString = null
                        cameraImageUriString = null

                        showForm = false
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Submit failed: ${e.message ?: "Unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isSubmitting = false
                    }
                }
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
            if (isSubmitting) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                    color = textPrimary
                )
                Spacer(Modifier.width(10.dp))
                Text("Submitting…")
            } else {
                Text("Submit spot")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUriString = uri?.toString()
        showImagePicker = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUriString = cameraImageUriString
        } else {
            cameraImageUriString = null
            Toast.makeText(context, "Could not capture photo", Toast.LENGTH_SHORT).show()
        }
        showImagePicker = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val newUri = createImageUri(context)
            cameraImageUriString = newUri.toString()
            cameraLauncher.launch(newUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
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
    textSecondary: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = elevatedSurfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            spot.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
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

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (spot.status == "inactive") "Inactive" else "Active") },
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

data class ContributedSpot(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val wheelchair: Boolean,
    val dogBowl: Boolean,
    val imageUrl: String? = null
)

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

private suspend fun reverseGeocodeToSingleLine(
    context: Context,
    latLng: LatLng
): String? = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())

        val list: List<Address> =
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) ?: emptyList()

        val a = list.firstOrNull() ?: return@withContext null

        val line0 = a.getAddressLine(0)
        if (!line0.isNullOrBlank()) return@withContext line0.trim()

        listOfNotNull(a.thoroughfare, a.subThoroughfare, a.locality)
            .joinToString(" ")
            .trim()
            .ifBlank { null }
    } catch (_: Exception) {
        null
    }
}

private fun SpotDto.toContributedSpot(): ContributedSpot {
    val typePretty = when (typeLabel) {
        "outdoor_water_fountain" -> "Outdoor water fountain"
        "indoor_water_fountain" -> "Indoor water fountain"
        else -> typeLabel
    }

    val publicImageUrl = imagePath?.let { path ->
        "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/spots-images/$path"
    }

    return ContributedSpot(
        id = id,
        name = address ?: "Unknown address",
        type = typePretty,
        status = status,
        wheelchair = wheelchairAccess == true,
        dogBowl = dogBowl == true,
        imageUrl = publicImageUrl
    )
}