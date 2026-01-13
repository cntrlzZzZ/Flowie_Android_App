package fr.eurecom.flowie.ui.screens

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import fr.eurecom.flowie.R
import fr.eurecom.flowie.ui.weather.WeatherViewModel
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/*
 * User profile screen displaying personal information,
 * activity statistics, hydration tracker, and logout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val weatherViewModel = androidx.lifecycle.viewmodel.compose.viewModel<WeatherViewModel>()
    val weatherState by weatherViewModel.uiState.collectAsState()

    remember { LocationServices.getFusedLocationProviderClient(context) }

    // Colours (same palette as other screens)
    val backgroundColor = Color(0xFF0B0B0F)
    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB3B3C2)

    // For now: use Vienna coordinates
    @SuppressLint("MissingPermission")
    fun fetchLocationAndUpdateWeather() {
        weatherViewModel.loadWeather(lat = 48.21, lon = 16.37)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndUpdateWeather()
        } else {
            Log.d("ProfileScreen", "Location permission denied.")
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocationAndUpdateWeather()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Hydration state (dummy for now)
    var dailyGoalLitres by remember { mutableStateOf(2.2f) }
    var consumedLitres by remember { mutableStateOf(1.3f) }

    // Dummy weekly analytics (litres per day)
    val weeklyLitres = remember {
        listOf(1.6f, 1.2f, 1.5f, 0.8f, 2.0f, 1.3f, 1.8f)
    }
    val weekLabels = listOf("So", "Mo", "Tu", "We", "Th", "Fr", "Sa")

    // Bottom sheet state for "Add new cup"
    var showAddCupSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState() // ✅ compatible version
    val scope = rememberCoroutineScope()

    // Dialog state for changing goal
    var showGoalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // --- Top header (avatar left, text right, settings top-right) ---
        Surface(
            color = surfaceColor,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(elevatedSurfaceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_droplet),
                            contentDescription = "Profile photo",
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Peter P.",
                            color = textPrimary,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Member since 2026",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "peter.p@email.com",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                IconButton(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = accentBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- Stats row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherCard(
                icon = weatherState.icon,
                temperature = weatherState.temperature,
                surfaceColor = elevatedSurfaceColor,
                borderColor = borderColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )

            StatCard(
                number = "12",
                label = "Contributed",
                surfaceColor = elevatedSurfaceColor,
                borderColor = borderColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )

            StatCard(
                number = "8",
                label = "Saved",
                surfaceColor = elevatedSurfaceColor,
                borderColor = borderColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hydration tracker + weekly analytics
        HydrationTrackerSection(
            consumedLitres = consumedLitres,
            dailyGoalLitres = dailyGoalLitres,
            onAddCup = { showAddCupSheet = true },
            onChangeGoal = { showGoalDialog = true },
            surfaceColor = surfaceColor,
            elevatedSurfaceColor = elevatedSurfaceColor,
            borderColor = borderColor,
            accentBlue = accentBlue,
            textPrimary = textPrimary,
            textSecondary = textSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        WeeklyAnalyticsCard(
            title = "Weekly analytics",
            values = weeklyLitres,
            labels = weekLabels,
            surfaceColor = surfaceColor,
            borderColor = borderColor,
            accentBlue = accentBlue,
            textPrimary = textPrimary,
            textSecondary = textSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        // --- Logout ---
        ProfileButton(
            text = "Logout",
            icon = null,
            surfaceColor = elevatedSurfaceColor,
            borderColor = borderColor,
            textPrimary = textPrimary,
            accentBlue = accentBlue
        ) { /* logout later */ }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // Bottom sheet: Add new cup
    if (showAddCupSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    showAddCupSheet = false
                }
            },
            sheetState = sheetState,
            containerColor = surfaceColor
        ) {
            AddCupBottomSheet(
                accentBlue = accentBlue,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                borderColor = borderColor,
                onAddLitres = { litres ->
                    consumedLitres = max(0f, consumedLitres + litres)
                    scope.launch {
                        sheetState.hide()
                        showAddCupSheet = false
                    }
                },
                onClose = {
                    scope.launch {
                        sheetState.hide()
                        showAddCupSheet = false
                    }
                }
            )
        }
    }

    // Dialog: Change daily goal
    if (showGoalDialog) {
        var input by remember(dailyGoalLitres) { mutableStateOf(dailyGoalLitres.toString()) }

        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            containerColor = surfaceColor,
            titleContentColor = textPrimary,
            textContentColor = textSecondary,
            title = { Text("Change daily goal") },
            text = {
                Column {
                    Text("Enter your daily goal in litres (e.g. 2.2).")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF0B0B0F),
                            focusedContainerColor = Color(0xFF0B0B0F),
                            unfocusedTextColor = textPrimary,
                            focusedTextColor = textPrimary,
                            unfocusedIndicatorColor = borderColor,
                            focusedIndicatorColor = accentBlue,
                            cursorColor = accentBlue
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = input.replace(',', '.').toFloatOrNull()
                        if (parsed != null && parsed > 0.1f && parsed < 10f) {
                            dailyGoalLitres = parsed
                            consumedLitres = min(consumedLitres, dailyGoalLitres * 2f)
                            showGoalDialog = false
                        }
                    }
                ) {
                    Text("Save", color = accentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }
}

@Composable
fun HydrationTrackerSection(
    consumedLitres: Float,
    dailyGoalLitres: Float,
    onAddCup: () -> Unit,
    onChangeGoal: () -> Unit,
    surfaceColor: Color,
    elevatedSurfaceColor: Color,
    borderColor: Color,
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val progress = if (dailyGoalLitres <= 0f) 0f else (consumedLitres / dailyGoalLitres).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Hydration",
                color = textPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HydrationRing(
                        progress = progress,
                        trackColor = elevatedSurfaceColor,
                        accentBlue = accentBlue
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percent%",
                            color = textPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily goal",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = accentBlue,
                        trackColor = Color(0xFF0F0F16)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = String.format("%.1f L out of %.1f L", consumedLitres, dailyGoalLitres),
                        color = textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = onAddCup,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentBlue,
                            contentColor = textPrimary
                        )
                    ) {
                        Text("Add new cup")
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = onChangeGoal) {
                        Text("Change daily goal", color = accentBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun HydrationRing(
    progress: Float,
    trackColor: Color,
    accentBlue: Color
) {
    val sweep = Brush.sweepGradient(
        listOf(Color(0xFF8FE8FF), accentBlue, accentBlue)
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 18.dp.toPx()
        val padding = strokeWidth / 2f
        val size = Size(this.size.width - padding * 2, this.size.height - padding * 2)

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(padding, padding),
            size = size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            brush = sweep,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(padding, padding),
            size = size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun WeeklyAnalyticsCard(
    title: String,
    values: List<Float>,
    labels: List<String>,
    surfaceColor: Color,
    borderColor: Color,
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(14.dp))

            val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                val barCount = values.size
                val gap = 12.dp.toPx()
                val barWidth = (size.width - gap * (barCount - 1)) / barCount
                val corner = CornerRadius(18f, 18f)

                values.forEachIndexed { i, v ->
                    val ratio = (v / maxVal).coerceIn(0f, 1f)
                    val barHeight = size.height * ratio
                    val left = i * (barWidth + gap)
                    val top = size.height - barHeight
                    drawRoundRect(
                        color = accentBlue,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = corner
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.take(values.size).forEach { label ->
                    Text(text = label, color = textSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun AddCupBottomSheet(
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color,
    borderColor: Color,
    onAddLitres: (Float) -> Unit,
    onClose: () -> Unit
) {
    var customMl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Add new cup",
            color = textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { onAddLitres(0.2f) },
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
            ) { Text("200 ml") }

            OutlinedButton(
                onClick = { onAddLitres(0.3f) },
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
            ) { Text("300 ml") }

            OutlinedButton(
                onClick = { onAddLitres(0.5f) },
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
            ) { Text("500 ml") }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Custom amount (ml)",
            color = textSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = customMl,
            onValueChange = { customMl = it },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFF0B0B0F),
                focusedContainerColor = Color(0xFF0B0B0F),
                unfocusedTextColor = textPrimary,
                focusedTextColor = textPrimary,
                unfocusedIndicatorColor = borderColor,
                focusedIndicatorColor = accentBlue,
                cursorColor = accentBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val ml = customMl.toIntOrNull()
                if (ml != null && ml > 0) {
                    onAddLitres(ml / 1000f)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentBlue,
                contentColor = textPrimary
            )
        ) {
            Text("Add")
        }

        Spacer(Modifier.height(10.dp))

        TextButton(onClick = onClose) {
            Text("Close", color = textSecondary)
        }

        Spacer(Modifier.height(10.dp))
    }
}

/* ---------- Existing cards/buttons ---------- */

@Composable
fun StatCard(
    number: String,
    label: String,
    surfaceColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.size(86.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }
    }
}

@Composable
fun ProfileButton(
    text: String,
    icon: ImageVector? = null,
    surfaceColor: Color,
    borderColor: Color,
    textPrimary: Color,
    accentBlue: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = accentBlue
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = text,
                color = textPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun WeatherCard(
    icon: String,
    temperature: String,
    surfaceColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.size(86.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = MaterialTheme.typography.headlineMedium.fontSize)
            Spacer(modifier = Modifier.height(4.dp))
            Text(temperature, color = textSecondary)
        }
    }
}
