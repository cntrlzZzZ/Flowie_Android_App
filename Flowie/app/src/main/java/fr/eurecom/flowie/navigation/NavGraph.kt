package fr.eurecom.flowie.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import fr.eurecom.flowie.ui.components.BottomBar
import fr.eurecom.flowie.ui.screens.ContributeScreen
import fr.eurecom.flowie.ui.screens.ExploreScreen
import fr.eurecom.flowie.ui.screens.ProfileScreen
import fr.eurecom.flowie.ui.screens.SavedScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import fr.eurecom.flowie.ui.screens.AboutScreen
import fr.eurecom.flowie.ui.screens.HelpScreen
import fr.eurecom.flowie.ui.screens.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Explore.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Explore.route) { ExploreScreen() }
            composable(Screen.Saved.route) { SavedScreen() }
            composable(Screen.Contribute.route) { ContributeScreen() }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
            composable("settings") { SettingsScreen() }
            composable("help") { HelpScreen() }
            composable("about") { AboutScreen() }
        }
    }
}