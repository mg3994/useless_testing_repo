package ch.zeitmessungen.equestre.data.models

import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class HorseModel(
    val age: Int?,
    val birthday: Date?,
    val father: String?,
    val fatherOfMother: String?,
    val gender: String?,
    val idx: Int?,
    val mother: String?,
    val name: String?,
    val owner: String?,
    val passport: String?,
    val signalementLabel: String?
) {
    companion object {
        private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        fun fromJson(json: JSONObject): HorseModel {
            val birthdayString = json.optString("birthday", null)
            val birthdayDate = try {
                birthdayString?.let { isoDateFormat.parse(it) }
            } catch (e: ParseException) {
                null
            }

            return HorseModel(
                age = if (json.has("age")) json.optInt("age") else null,
                birthday = birthdayDate,
                father = json.optString("father", null),
                fatherOfMother = json.optString("fatherOfMother", null),
                gender = json.optString("gender", null),
                idx = if (json.has("idx")) json.optInt("idx") else null,
                mother = json.optString("mother", null),
                name = json.optString("name", null),
                owner = json.optString("owner", null),
                passport = json.optString("passport", null),
                signalementLabel = json.optString("signalementLabel", null)
            )
        }

        fun fromJsonArray(array: JSONArray): List<HorseModel> {
            val list = mutableListOf<HorseModel>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(fromJson(obj))
            }
            return list
        }
    }
}
