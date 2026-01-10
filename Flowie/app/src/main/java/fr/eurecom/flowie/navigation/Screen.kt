package fr.eurecom.flowie.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/*
 * Sealed class defining all main navigation destinations.
 * Each screen has a route, label, and icon used by the BottomBar.
 */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Explore : Screen("explore", "Explore", Icons.Default.Place)
    object Saved : Screen("saved", "Saved", Icons.Default.Bookmark)
    object Contribute : Screen("contribute", "Contribute", Icons.Default.AddCircle)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}