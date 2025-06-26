package ch.zeitmessungen.equestre.data.models

import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class RiderModel(
    val birthday: Date?,
    val city: String?,
    val club: String?,
    val firstName: String?,
    val idx: Int?,
    val lastName: String?,
    val license: String?,
    val nation: String?
) {
    companion object {
        private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        fun fromJson(json: JSONObject): RiderModel {
            val birthdayString = json.optString("birthday", null)
            val birthdayDate = try {
                birthdayString?.let { isoDateFormat.parse(it) }
            } catch (e: ParseException) {
                null
            }

            return RiderModel(
                birthday = birthdayDate,
                city = json.optString("city", null),
                club = json.optString("club", null),
                firstName = json.optString("firstName", null),
                idx = if (json.has("idx")) json.optInt("idx") else null,
                lastName = json.optString("lastName", null),
                license = json.optString("license", null),
                nation = json.optString("nation", null)
            )
        }

        fun fromJsonArray(array: JSONArray): List<RiderModel> {
            val list = mutableListOf<RiderModel>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(fromJson(obj))
            }
            return list
        }
    }
}
