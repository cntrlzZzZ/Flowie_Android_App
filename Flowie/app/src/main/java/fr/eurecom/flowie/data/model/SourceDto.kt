package fr.eurecom.flowie.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SourceDto(
    val id: String,
    val name: String,
    val type_label: String,
    val description: String? = null,
    val lat: Double,
    val lng: Double,
    val origin: String,
    val status: String
)
