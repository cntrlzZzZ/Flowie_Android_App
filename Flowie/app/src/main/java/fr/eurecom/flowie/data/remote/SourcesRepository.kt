package fr.eurecom.flowie.data.remote

import android.content.Context
import android.net.Uri
import fr.eurecom.flowie.data.model.SpotDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

class SourcesRepository {

    suspend fun fetchInBbox(
        minLat: Double, minLng: Double,
        maxLat: Double, maxLng: Double,
        maxRows: Long = 2000
    ): List<SpotDto> {
        return SupabaseProvider.client
            .from("sources")
            .select {
                filter {
                    gte("lat", minLat)
                    lte("lat", maxLat)
                    gte("lng", minLng)
                    lte("lng", maxLng)
                }
                limit(maxRows)
            }
            .decodeList()
    }

    suspend fun fetchById(id: String): SpotDto? {
        return SupabaseProvider.client
            .from("sources")
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeList<SpotDto>()
            .firstOrNull()
    }

    suspend fun fetchByIds(ids: List<String>): List<SpotDto> = coroutineScope {
        ids.distinct()
            .map { id -> async { fetchById(id) } }
            .awaitAll()
            .filterNotNull()
    }

    /**
     * Fetch ONLY the currently logged-in user's community contributions.
     * This depends on `created_by` being set (either by us, or by DB trigger/default).
     */
    suspend fun fetchMyCommunitySources(
        userId: String,
        maxRows: Long = 200
    ): List<SpotDto> {
        return SupabaseProvider.client
            .from("sources")
            .select {
                filter {
                    eq("origin", "community")
                    eq("created_by", userId)
                }
                order("created_at", Order.DESCENDING)
                limit(maxRows)
            }
            .decodeList()
    }

    /**
     * Step 3:
     * - optionally upload image to storage bucket "spots-images"
     * - insert a community source into public.sources
     *
     * Returns the inserted SpotDto.
     */
    suspend fun createCommunitySource(
        context: Context,
        address: String,
        lat: Double,
        lng: Double,
        typeLabel: String,           // "outdoor_water_fountain" | "indoor_water_fountain"
        status: String,              // "active" | "inactive"
        wheelchairAccess: Boolean?,  // true or null
        dogBowl: Boolean?,           // true or null
        imageUri: Uri?
    ): SpotDto {
        val imagePath: String? = if (imageUri != null) {
            uploadSpotImage(context, imageUri)
        } else null

        // ✅ Ensure created_by is populated (helps "my contributions" query)
        val currentUserId = AuthManager.currentUserIdOrNull()

        val payload = SourceInsert(
            origin = "community",
            typeLabel = typeLabel,
            lat = lat,
            lng = lng,
            status = status,
            wheelchairAccess = wheelchairAccess,
            dogBowl = dogBowl,
            imagePath = imagePath,
            address = address,
            createdBy = currentUserId
        )

        // Insert + select inserted row back
        return SupabaseProvider.client
            .from("sources")
            .insert(payload) { select() }
            .decodeList<SpotDto>()
            .first()
    }

    private suspend fun uploadSpotImage(context: Context, uri: Uri): String =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver

            val mime = resolver.getType(uri) ?: "image/jpeg"
            val ext = when (mime.lowercase()) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Could not read image bytes")

            val filePath = "community/${UUID.randomUUID()}.$ext"

            SupabaseProvider.client.storage
                .from("spots-images")
                .upload(
                    path = filePath,
                    data = bytes,
                    upsert = false
                )

            filePath
        }
}

@Serializable
private data class SourceInsert(
    val origin: String,
    @SerialName("type_label") val typeLabel: String,
    val lat: Double,
    val lng: Double,
    val status: String,
    @SerialName("wheelchair_access") val wheelchairAccess: Boolean? = null,
    @SerialName("dog_bowl") val dogBowl: Boolean? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val address: String,
    @SerialName("created_by") val createdBy: String? = null
)