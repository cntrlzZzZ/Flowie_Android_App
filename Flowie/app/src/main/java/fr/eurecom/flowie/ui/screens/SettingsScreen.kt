package fr.eurecom.flowie.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

private enum class SettingsPage {
    HOME, FAQ, ABOUT, PRIVACY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

    // Same palette as the rest of your app
    val backgroundColor = Color(0xFF0B0B0F)
    val surfaceColor = Color(0xFF12121A)
    val elevatedSurfaceColor = Color(0xFF1A1A24)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB3B3C2)
    val okGreen = Color(0xFF3DDC84)

    var page by remember { mutableStateOf(SettingsPage.HOME) }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }

    // Simple “status” helpers (best-effort)
    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    val locationStatus = if (hasLocationPermission()) "Allowed" else "Not allowed"
    val locationStatusColour = if (hasLocationPermission()) okGreen else textSecondary

    // Notifications permission varies by Android version (13+ runtime permission),
    // so keep it simple and just point users to system settings.
    val notificationsStatus = "Manage in system settings"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (page == SettingsPage.HOME) navController.popBackStack()
                        else page = SettingsPage.HOME
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary
                    )
                }

                Spacer(Modifier.width(6.dp))

                Text(
                    text = when (page) {
                        SettingsPage.HOME -> "Settings"
                        SettingsPage.FAQ -> SettingsContent.faqTitle
                        SettingsPage.ABOUT -> SettingsContent.aboutTitle
                        SettingsPage.PRIVACY -> SettingsContent.privacyTitle
                    },
                    color = textPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))

            when (page) {
                SettingsPage.HOME -> {
                    // --- Permissions & Data ---
                    SettingsCard(
                        title = "Permissions & data",
                        surfaceColor = surfaceColor,
                        borderColor = borderColor
                    ) {
                        SettingsRowNoTrailingIcon(
                            icon = Icons.Filled.PinDrop,
                            title = "Location",
                            subtitle = "We use location to show your position on the map.",
                            status = locationStatus,
                            statusColour = locationStatusColour,
                            accentBlue = accentBlue,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { openAppSettings() }
                        )

                        SettingsRowNoTrailingIcon(
                            icon = Icons.Filled.Notifications,
                            title = "Notifications",
                            subtitle = "We use notifications for alerts (if you turn them on).",
                            status = notificationsStatus,
                            statusColour = textSecondary,
                            accentBlue = accentBlue,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { openAppSettings() }
                        )

                        SettingsRowNoTrailingIcon(
                            icon = Icons.Filled.PhotoCamera,
                            title = "Photos",
                            subtitle = "We use photos to show what a spot looks like.",
                            status = "Only upload photos you’re comfortable sharing publicly.",
                            statusColour = textSecondary,
                            accentBlue = accentBlue,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { /* no system setting needed, keep it informational */ }
                        )

                        SettingsRowNoTrailingIcon(
                            icon = Icons.Filled.PrivacyTip,
                            title = "Data storage",
                            subtitle = "Saved spots are stored locally on your device.",
                            status = "You can clear them by deleting app data in system settings.",
                            statusColour = textSecondary,
                            accentBlue = accentBlue,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { openAppSettings() }
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // --- App info pages ---
                    SettingsCard(
                        title = "App",
                        surfaceColor = surfaceColor,
                        borderColor = borderColor
                    ) {
                        SettingsRowSimple(
                            icon = Icons.Filled.QuestionAnswer,
                            title = "FAQ",
                            subtitle = "Common questions + quick help",
                            accentBlue = accentBlue,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { page = SettingsPage.FAQ }
                        )

                        SettingsRowSimple(
                            icon = Icons.Filled.Info,
                            title = "About us",
                            subtitle = "What Flowie is about",
                            accentBlue = accentBlue,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { page = SettingsPage.ABOUT }
                        )

                        SettingsRowSimple(
                            icon = Icons.Filled.PrivacyTip,
                            title = "Privacy & Safety",
                            subtitle = "Your data + safe usage tips",
                            accentBlue = accentBlue,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { page = SettingsPage.PRIVACY }
                        )
                    }
                }

                SettingsPage.FAQ -> {
                    SettingsContent.faqItems.forEach { item ->
                        FaqCard(
                            question = item.question,
                            answer = item.answer,
                            surfaceColor = surfaceColor,
                            borderColor = borderColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                SettingsPage.ABOUT -> {
                    InfoCard(
                        body = SettingsContent.aboutBody,
                        surfaceColor = surfaceColor,
                        borderColor = borderColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                SettingsPage.PRIVACY -> {
                    InfoCard(
                        body = SettingsContent.privacyBody,
                        surfaceColor = surfaceColor,
                        borderColor = borderColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    InfoCard(
                        body = SettingsContent.safetyBody,
                        surfaceColor = surfaceColor,
                        borderColor = borderColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    surfaceColor: Color,
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Surface(
            color = surfaceColor,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), content = content)
        }
    }
}

@Composable
private fun SettingsRowSimple(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accentBlue.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentBlue)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = textPrimary, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = textSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SettingsRowNoTrailingIcon(
    icon: ImageVector,
    title: String,
    subtitle: String,
    status: String,
    statusColour: Color,
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accentBlue.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentBlue)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = textPrimary, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = textSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Text(status, color = statusColour, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FaqCard(
    question: String,
    answer: String,
    surfaceColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = question,
                color = textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(text = answer, color = textSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InfoCard(
    body: String,
    surfaceColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = body, color = textSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
