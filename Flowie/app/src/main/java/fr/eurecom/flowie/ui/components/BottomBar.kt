package fr.eurecom.flowie.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import fr.eurecom.flowie.navigation.Screen
import androidx.compose.material3.Icon

/*
 * Bottom navigation bar used for main app navigation.
 */
@Composable
fun BottomBar(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf(
        Screen.Explore,
        Screen.Saved,
        Screen.Contribute,
        Screen.Profile
    )
    NavigationBar(modifier = modifier) {
        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry.value?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // avoid building up a large backstack
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) }
            )
        }
    }
}