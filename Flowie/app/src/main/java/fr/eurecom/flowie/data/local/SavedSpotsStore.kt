package fr.eurecom.flowie.ui.components

import android.content.Context
import fr.eurecom.flowie.data.model.SpotDto
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object SavedSpotsStore {
    private const val PREFS = "saved_spots"

    // NEW: store id -> SpotDto JSON map as one string
    private const val KEY_SPOTS_JSON = "spots_json"

    // OLD (legacy): keep for now so we don't crash if it exists
    private const val KEY_IDS = "ids"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val mapSerializer = MapSerializer(String.serializer(), SpotDto.serializer())

    private fun readMap(context: Context): MutableMap<String, SpotDto> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val raw = prefs.getString(KEY_SPOTS_JSON, null)
        if (raw.isNullOrBlank()) {
            // If you previously saved only IDs, we can't magically rebuild SpotDto offline.
            // So we just return empty here.
            return mutableMapOf()
        }

        return try {
            json.decodeFromString(mapSerializer, raw).toMutableMap()
        } catch (_: Exception) {
            // Corrupted JSON? Reset to empty to avoid crashes.
            mutableMapOf()
        }
    }

    private fun writeMap(context: Context, map: Map<String, SpotDto>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encoded = json.encodeToString(mapSerializer, map)
        prefs.edit().putString(KEY_SPOTS_JSON, encoded).apply()
    }

    /** Returns all saved spot IDs (derived from the stored map). */
    fun getIds(context: Context): Set<String> {
        val map = readMap(context)
        return map.keys
    }

    /** Returns the saved spots (offline). */
    fun getAll(context: Context): List<SpotDto> {
        return readMap(context).values.toList()
    }

    fun isSaved(context: Context, id: String): Boolean {
        return readMap(context).containsKey(id)
    }

    /**
     * Toggle save using the FULL SpotDto.
     * - if saved -> remove it
     * - else -> store it
     *
     * Returns the updated set of saved IDs.
     */
    fun toggle(context: Context, spot: SpotDto): Set<String> {
        val map = readMap(context)

        if (map.containsKey(spot.id)) {
            map.remove(spot.id)
        } else {
            map[spot.id] = spot
        }

        writeMap(context, map)

        // Optional: clean up old legacy ids (prevents confusion)
        // (Only safe because we now store everything properly in KEY_SPOTS_JSON)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_IDS).apply()

        return map.keys
    }

    /** Remove a saved spot by id (useful sometimes). */
    fun remove(context: Context, id: String): Set<String> {
        val map = readMap(context)
        map.remove(id)
        writeMap(context, map)
        return map.keys
    }
}