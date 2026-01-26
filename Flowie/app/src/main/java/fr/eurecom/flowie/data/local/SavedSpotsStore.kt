package fr.eurecom.flowie.ui.components

import android.content.Context

object SavedSpotsStore {
    private const val PREFS = "saved_spots"
    private const val KEY_IDS = "ids"

    fun getIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_IDS, emptySet()) ?: emptySet()
    }

    fun setIds(context: Context, ids: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_IDS, ids).apply()
    }

    fun toggle(context: Context, id: String): Set<String> {
        val current = getIds(context).toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        setIds(context, current)
        return current
    }
}
