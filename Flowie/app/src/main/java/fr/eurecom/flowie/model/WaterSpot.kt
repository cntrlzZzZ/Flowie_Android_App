package fr.eurecom.flowie.model

/*
 * Data model describing a water spot displayed in the app.
 */
data class WaterSpot(
    val name: String,
    val type: String,
    val isAccessible: Boolean,
    val description: String
)