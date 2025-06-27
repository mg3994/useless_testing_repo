package ch.zeitmessungen.equestre.data.models


import org.json.JSONObject

/**
 * Data class representing the structure of a "final" event received from the socket.
 * This model captures the competitor's number, lane, start time, and score details.
 * It is designed to mirror the structure of the 'realtime' event for consistency
 * in data handling for final results.
 */
data class FinalModel(
    val num: Int?, // Competitor number
    val lane: Int?, // Lane number
    val startTime: Long?, // Start time in milliseconds
    val score: ScoreModel? // Nested score details for lane1 and lane2
) {
    companion object {
        /**
         * Factory method to create a FinalModel instance from a JSONObject.
         *
         * @param jsonObject The JSONObject containing the final event data.
         * @return A FinalModel instance, or null if the input JSONObject is null.
         */
        fun fromJson(jsonObject: JSONObject?): FinalModel? {
            if (jsonObject == null) {
                return null
            }

            // Parse basic fields, handling potential missing values
            val num = if (jsonObject.has("num")) jsonObject.getInt("num") else null
            val lane = if (jsonObject.has("lane")) jsonObject.getInt("lane") else null
            val startTime = if (jsonObject.has("startTime")) jsonObject.getLong("startTime") else null

            // Parse the nested 'score' object using ScoreModel's fromJson
            val scoreJson = if (jsonObject.has("score")) jsonObject.optJSONObject("score") else null
            val score = ScoreModel.fromJson(scoreJson)

            return FinalModel(num, lane, startTime, score)
        }
    }

    /**
     * Data class representing the score details within the FinalModel.
     * Contains score information for lane1 and lane2.
     */
    data class ScoreModel(
        val lane1: LaneScoreModel?, // Score details for lane 1
        val lane2: LaneScoreModel? // Score details for lane 2
    ) {
        companion object {
            /**
             * Factory method to create a ScoreModel instance from a JSONObject.
             *
             * @param jsonObject The JSONObject containing the score data.
             * @return A ScoreModel instance, or null if the input JSONObject is null.
             */
            fun fromJson(jsonObject: JSONObject?): ScoreModel? {
                if (jsonObject == null) {
                    return null
                }

                // Parse lane1 and lane2 score details
                val lane1Json = if (jsonObject.has("lane1")) jsonObject.optJSONObject("lane1") else null
                val lane1 = LaneScoreModel.fromJson(lane1Json)

                val lane2Json = if (jsonObject.has("lane2")) jsonObject.optJSONObject("lane2") else null
                val lane2 = LaneScoreModel.fromJson(lane2Json)

                return ScoreModel(lane1, lane2)
            }
        }

        /**
         * Data class representing the score details for a specific lane (e.g., lane1 or lane2).
         * Contains point and time information.
         */
        data class LaneScoreModel(
            val point: Int?, // Points scored in the lane
            val time: Long? // Time recorded for the lane in milliseconds
        ) {
            companion object {
                /**
                 * Factory method to create a LaneScoreModel instance from a JSONObject.
                 *
                 * @param jsonObject The JSONObject containing the lane score data.
                 * @return A LaneScoreModel instance, or null if the input JSONObject is null.
                 */
                fun fromJson(jsonObject: JSONObject?): LaneScoreModel? {
                    if (jsonObject == null) {
                        return null
                    }

                    // Parse point and time, handling potential missing values
                    val point = if (jsonObject.has("point")) jsonObject.getInt("point") else null
                    val time = if (jsonObject.has("time")) jsonObject.getLong("time") else null

                    return LaneScoreModel(point, time)
                }
            }
        }
    }
}

