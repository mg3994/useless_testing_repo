package ch.zeitmessungen.equestre.data.models

import org.json.JSONObject
// List of =>[{"horse_idx":2,"num":1,"pos":0,"rider_idx":2,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":3,"num":2,"pos":1,"rider_idx":3,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":4,"num":3,"pos":2,"rider_idx":4,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":5,"num":4,"pos":3,"rider_idx":5,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":6,"num":5,"pos":4,"rider_idx":6,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":7,"num":6,"pos":5,"rider_idx":7,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":8,"num":7,"pos":6,"rider_idx":8,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":9,"num":8,"pos":7,"rider_idx":9,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":10,"num":9,"pos":8,"rider_idx":10,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":11,"num":10,"pos":9,"rider_idx":11,"start_time":0,"score":{"lane1":{},"lane2":{}}},{"horse_idx":12,"num":11,"pos":10,"rider_idx":12,"start_time":0,"score":{"lane1":{},"lane2":{}}}]
data class StartlistModel(
    val horseIdx: Int?,
    val num: Int?,
    val pos: Int?,
    val riderIdx: Int?,
    val startTime: Long?,
    val score: ScoreModel?
) {
    companion object {
        fun fromJson(json: JSONObject?): StartlistModel? {
            if (json == null || json.length() == 0) return null

            return StartlistModel(
                horseIdx = if (json.has("horse_idx") && !json.isNull("horse_idx")) json.optInt("horse_idx") else null,
                num = if (json.has("num") && !json.isNull("num")) json.optInt("num") else null,
                pos = if (json.has("pos") && !json.isNull("pos")) json.optInt("pos") else null,
                riderIdx = if (json.has("rider_idx") && !json.isNull("rider_idx")) json.optInt("rider_idx") else null,
                startTime = if (json.has("start_time") && !json.isNull("start_time")) json.optLong("start_time") else null,
                score = ScoreModel.fromJson(json.optJSONObject("score"))
            )
        }
    }
}
