package ch.zeitmessungen.equestre.data.models
// it is a Map => {"eventId":"55035_1_0"}
import org.json.JSONObject

data class NotifyResumeModel(
    val eventId: String?
) {
    companion object {
        fun fromJson(json: JSONObject): NotifyResumeModel {
            return NotifyResumeModel(
                eventId = json.optString("eventId", null)
            )
        }
    }
}
