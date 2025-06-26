package ch.zeitmessungen.equestre.data.models



import org.json.JSONObject

data class ScoreDetail(
    val point: Int?,
    val time: Long?
) {
    companion object {
        fun fromJson(json: JSONObject): ScoreDetail {
            return ScoreDetail(
                point = json.optInt("point"),
                time = if (json.has("time")) json.optLong("time") else null
            )
        }
    }
}

data class ScoreModel(
    val lane1: ScoreDetail?,
    val lane2: ScoreDetail?
) {
    companion object {
        fun fromJson(json: JSONObject): ScoreModel {
            return ScoreModel(
                lane1 = json.optJSONObject("lane1")?.let { ScoreDetail.fromJson(it) },
                lane2 = json.optJSONObject("lane2")?.let { ScoreDetail.fromJson(it) }
            )
        }
    }
}

data class RealtimeModel(
    val num: Int?,
    val lane: Int?,
    val startTime: Long?,
    val score: ScoreModel?
) {
    companion object {
        fun fromJson(json: JSONObject): RealtimeModel {
            return RealtimeModel(
                num = json.optInt("num"),
                lane = json.optInt("lane"),
                startTime = json.optLong("startTime"),
                score = json.optJSONObject("score")?.let { ScoreModel.fromJson(it) }
            )
        }
    }
}

