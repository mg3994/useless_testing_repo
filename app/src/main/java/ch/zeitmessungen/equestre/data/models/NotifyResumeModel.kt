package ch.zeitmessungen.equestre.data.models

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
