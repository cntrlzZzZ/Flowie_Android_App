package fr.eurecom.flowie.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import fr.eurecom.flowie.model.StepRepository

@Composable
fun ProfileScreen(navController: NavController) {

    val steps by StepRepository.steps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- Avatar ---
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- User info ---
        Text("John Doe", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Text("john.doe@email.com", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(30.dp))

        // --- Stats row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Steps today", steps.toString())
            StatCard("Added", "12")
            StatCard("Saved", "8")
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- Settings cards ---
        ProfileButton(text = "Settings", icon = Icons.Default.Settings) {
            navController.navigate("settings")
        }
        ProfileButton(text = "Help / FAQ", icon = Icons.Default.Settings) {
            navController.navigate("help")
        }
        ProfileButton(text = "About", icon = Icons.Default.Settings) {
            navController.navigate("about")
        }
        ProfileButton("Logout")
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(90.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text(label, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ProfileButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick, // ✅ ICI
        color = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}