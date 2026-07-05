package com.elendheim.chords.data

import android.content.Context
import android.content.SharedPreferences
import com.elendheim.chords.model.SavedChord
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the chord library as a JSON array in SharedPreferences.
 * Small, dependable, and plenty for a personal library.
 */
class ChordStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("chord_library", Context.MODE_PRIVATE)

    fun load(): List<SavedChord> {
        val raw = prefs.getString(KEY_CHORDS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                val notesArray = obj.optJSONArray("notes") ?: return@mapNotNull null
                SavedChord(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    notes = (0 until notesArray.length()).map { notesArray.getInt(it) },
                    createdAt = obj.optLong("createdAt")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(chords: List<SavedChord>) {
        val array = JSONArray()
        for (chord in chords) {
            val obj = JSONObject()
            obj.put("id", chord.id)
            obj.put("name", chord.name)
            obj.put("notes", JSONArray(chord.notes))
            obj.put("createdAt", chord.createdAt)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CHORDS, array.toString()).apply()
    }

    private companion object {
        const val KEY_CHORDS = "chords"
    }
}
