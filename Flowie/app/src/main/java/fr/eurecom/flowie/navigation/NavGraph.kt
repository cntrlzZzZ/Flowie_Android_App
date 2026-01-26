package fr.eurecom.flowie.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.eurecom.flowie.data.remote.AuthManager
import fr.eurecom.flowie.ui.components.BottomBar
import fr.eurecom.flowie.ui.screens.*
import io.github.jan.supabase.gotrue.SessionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    var guestMode by rememberSaveable { mutableStateOf(false) }

    // derive login state from sessionStatus (no isLoggedInFlow needed)
    val sessionStatus by AuthManager.sessionStatus.collectAsState()
    val isLoggedIn = sessionStatus is SessionStatus.Authenticated

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Explore.route,
        Screen.Saved.route,
        Screen.Contribute.route,
        Screen.Profile.route
    )

    // If auth completes while we're on login/loading -> jump into the app
    LaunchedEffect(isLoggedIn, guestMode) {
        if (isLoggedIn || guestMode) {
            navController.navigate(Screen.Explore.route) {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- AUTH ---
            composable("login") {
                LoginScreen(
                    onContinueAsGuest = { guestMode = true },
                    onLoginStarted = { navController.navigate("loading") }
                )
            }

            composable("loading") {
                LoadingScreen()
            }

            // --- MAIN APP ---
            composable(Screen.Explore.route) { ExploreScreen() }
            composable(Screen.Saved.route) { SavedScreen() }
            composable(Screen.Contribute.route) { ContributeScreen() }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
            composable("settings") { SettingsScreen(navController = navController) }
        }
    }
}
