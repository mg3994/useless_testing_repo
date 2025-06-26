package ch.zeitmessungen.equestre.data.models
// List of => [{"horse_idx":2,"num":1,"rider_idx":2},{"horse_idx":3,"num":2,"rider_idx":3},{"horse_idx":4,"num":3,"rider_idx":4},{"horse_idx":5,"num":4,"rider_idx":5},{"horse_idx":6,"num":5,"rider_idx":6},{"horse_idx":7,"num":6,"rider_idx":7},{"horse_idx":8,"num":7,"rider_idx":8},{"horse_idx":9,"num":8,"rider_idx":9},{"horse_idx":10,"num":9,"rider_idx":10},{"horse_idx":11,"num":10,"rider_idx":11},{"horse_idx":12,"num":11,"rider_idx":12}]
import org.json.JSONObject

data class CompetitorModel(
    val horseIdx: Int?,
    val num: Int?,
    val riderIdx: Int?
) {
    companion object {
        fun fromJson(json: JSONObject?): CompetitorModel? {
            if (json == null || json.length() == 0) return null

            return CompetitorModel(
                horseIdx = if (json.has("horse_idx") && !json.isNull("horse_idx")) json.optInt("horse_idx") else null,
                num = if (json.has("num") && !json.isNull("num")) json.optInt("num") else null,
                riderIdx = if (json.has("rider_idx") && !json.isNull("rider_idx")) json.optInt("rider_idx") else null
            )
        }
    }
}
