package ch.zeitmessungen.equestre.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject

data class ConsumerModel(
    val id: String?,
    val info: InfoModel?,
    val paused: Boolean?
) {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromJson(json: JSONObject): ConsumerModel {
            return ConsumerModel(
                id = json.optString("id", null),
                info = json.optJSONObject("info")?.let { InfoModel.fromJson(it) },
                paused = if (json.has("paused")) json.optBoolean("paused") else null
            )
        }
    }
}