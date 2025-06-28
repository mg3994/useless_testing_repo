package ch.zeitmessungen.equestre.data.models

import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents a rider competitor in the event.
 * @property num The unique competitor number.
 * @property name The full name of the rider, parsed directly from the 'rider_idx' field.
 */
data class RiderEntry(
    val num: Int,
    val name: String
) {
    companion object {
        /**
         * Parses a JSON object to create a RiderEntry instance.
         * Assumes 'num' is the ID and 'rider_idx' is the name.
         */
        fun fromJson(json: JSONObject): RiderEntry {
            return RiderEntry(
                num = json.optInt("num"),
                name = json.optString("rider_idx", "Rider Name N/A")
            )
        }

        /**
         * Parses a JSON array to create a list of RiderEntry instances.
         */
        fun fromJsonArray(jsonArray: JSONArray): List<RiderEntry> {
            val entries = mutableListOf<RiderEntry>()
            for (i in 0 until jsonArray.length()) {
                entries.add(fromJson(jsonArray.getJSONObject(i)))
            }
            return entries
        }
    }
}