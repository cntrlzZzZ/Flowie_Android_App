package fr.eurecom.flowie.model

data class WaterSpot(
    val name: String,
    val type: String,
    val isAccessible: Boolean,
    val description: String
)