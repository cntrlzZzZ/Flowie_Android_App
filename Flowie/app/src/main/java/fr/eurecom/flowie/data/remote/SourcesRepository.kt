package fr.eurecom.flowie.data.remote

import fr.eurecom.flowie.data.model.SourceDto
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SourcesRepository {

    suspend fun fetchInBbox(
        minLat: Double, minLng: Double,
        maxLat: Double, maxLng: Double,
        maxRows: Int = 2000
    ): List<SourceDto> {

        val params = buildJsonObject {
            put("min_lat", minLat)
            put("min_lng", minLng)
            put("max_lat", maxLat)
            put("max_lng", maxLng)
            put("max_rows", maxRows)
        }

        return SupabaseProvider.client.postgrest
            .rpc("sources_in_bbox", params)
            .decodeList()
    }
}
