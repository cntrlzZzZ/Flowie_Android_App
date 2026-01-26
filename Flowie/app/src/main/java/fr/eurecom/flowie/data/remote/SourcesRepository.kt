package fr.eurecom.flowie.data.remote

import fr.eurecom.flowie.data.model.SpotDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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
}
