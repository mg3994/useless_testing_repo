package ch.zeitmessungen.equestre.data.models

//May be Map
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class InfoModel(
    val title: String?,
    val live: Boolean?,
    val eventTitle: String?,
    val eventTime: String?,
    val eventDate: Date?,
    val category: String?,
    val country: String?
) {
    companion object {
        private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        fun fromJson(json: JSONObject): InfoModel {
            val dateString = json.optString("eventDate", null)
            val parsedDate = dateString?.let {
                try {
                    isoDateFormat.parse(it)
                } catch (e: ParseException) {
                    null
                }
            }

            val liveValue = when (val liveRaw = json.opt("live")) {
                is Int -> liveRaw == 1
                is Boolean -> liveRaw
                else -> null
            }

            return InfoModel(
                title = json.optString("title", null),
                live = liveValue,
                eventTitle = json.optString("eventTitle", null),
                eventTime = json.optString("eventTime", null),
                eventDate = parsedDate,
                category = json.optString("category", null),
                country = json.optString("country", null)
            )
        }
    }
}