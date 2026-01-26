package fr.eurecom.flowie.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SpotDto(
    val id: String,
    val origin: String,

    @SerialName("external_id")
    val externalId: String? = null,

    @SerialName("type_label")
    val typeLabel: String,

    val lat: Double,
    val lng: Double,

    val status: String,

    @SerialName("created_by")
    val createdBy: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("wheelchair_access")
    val wheelchairAccess: Boolean? = null,

    @SerialName("dog_bowl")
    val dogBowl: Boolean? = null,

    @SerialName("image_path")
    val imagePath: String? = null,

    val address: String? = null
)
