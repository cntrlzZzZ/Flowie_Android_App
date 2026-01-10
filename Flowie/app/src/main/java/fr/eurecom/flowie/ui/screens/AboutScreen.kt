package fr.eurecom.flowie.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


/*
 * About screen presenting the purpose and key features of the application.
 */
@Composable
fun AboutScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(" Flowie is a smart hydration companion designed to help users stay hydrated throughout their daily activities. \n" +
                "\n" +
                " The app allows users to locate nearby water points on an interactive map, contribute new water spots, \n" +
                " and save their favorite locations for easy access. By combining real-time weather data with step count and movement information, " +
                " Flowie adapts to each user's activity level and environment. \n" +
                "\n" +
                " Using temperature, distance traveled, and physical effort, the app can estimate hydration needs and notify users when it is time to drink water. " +
                " Flowie aims to promote healthier habits, prevent dehydration, and encourage sustainable use of public water sources, especially in urban environments. \n", color = Color.Black)
    }
}