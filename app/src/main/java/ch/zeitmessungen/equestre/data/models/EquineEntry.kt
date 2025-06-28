package ch.zeitmessungen.equestre.data.models

import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents a horse competitor in the event.
 * @property num The unique competitor number. This is the key to link with realtime data.
 * @property name The name of the horse, parsed directly from the 'horse_idx' field.
 */
data class EquineEntry(
    val num: Int,
    val name: String
) {
    companion object {
        /**
         * Parses a JSON object to create an EquineEntry instance.
         * Assumes 'num' is the ID and 'horse_idx' is the name.
         */
        fun fromJson(json: JSONObject): EquineEntry {
            return EquineEntry(
                num = json.optInt("num"),
                name = json.optString("horse_idx", "Horse Name N/A")
            )
        }

        /**
         * Parses a JSON array to create a list of EquineEntry instances.
         */
        fun fromJsonArray(jsonArray: JSONArray): List<EquineEntry> {
            val entries = mutableListOf<EquineEntry>()
            for (i in 0 until jsonArray.length()) {
                entries.add(fromJson(jsonArray.getJSONObject(i)))
            }
            return entries
        }
    }
}