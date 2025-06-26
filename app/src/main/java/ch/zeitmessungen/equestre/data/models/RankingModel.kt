package ch.zeitmessungen.equestre.data.models

import org.json.JSONArray
import org.json.JSONObject

data class RankingModel(
    val ranking: List<List<String?>>?, // Nested lists, first one contains header HTML strings, others data rows
    val teamRanking: List<Any>?,       // Assuming empty or unknown content, keep as list of Any or make more specific if known
    val gameInfo: GameInfo?
) {
    companion object {
        fun fromJson(json: JSONObject): RankingModel {
            val rankingList = mutableListOf<List<String?>>()
            val rankingJsonArray = json.optJSONArray("ranking")
            if (rankingJsonArray != null) {
                for (i in 0 until rankingJsonArray.length()) {
                    val innerArray = rankingJsonArray.optJSONArray(i)
                    if (innerArray != null) {
                        val row = mutableListOf<String?>()
                        for (j in 0 until innerArray.length()) {
                            val cell = innerArray.opt(j)
                            row.add(cell?.toString())
                        }
                        rankingList.add(row)
                    }
                }
            }

            val teamRankingJsonArray = json.optJSONArray("team_ranking")
            val teamRankingList = mutableListOf<Any>()
            if (teamRankingJsonArray != null) {
                for (i in 0 until teamRankingJsonArray.length()) {
                    teamRankingList.add(teamRankingJsonArray.get(i))
                }
            }

            val gameInfoObj = json.optJSONObject("gameInfo")
            val gameInfo = gameInfoObj?.let { GameInfo.fromJson(it) }

            return RankingModel(
                ranking = if (rankingList.isNotEmpty()) rankingList else null,
                teamRanking = if (teamRankingList.isNotEmpty()) teamRankingList else null,
                gameInfo = gameInfo
            )
        }
    }
}

data class GameInfo(
    val allowedTime: Int?,
    val allowedTimeJumpoff: Int?,
    val registeredCount: Int?,
    val rankingCount: Int?,
    val startedCount: Int?,
    val clearedCount: Int?,
    val comingupCount: Int?,
    val tableType: Int?,
    val twoPhase: Int?
) {
    companion object {
        fun fromJson(json: JSONObject): GameInfo {
            return GameInfo(
                allowedTime = json.optInt("allowed_time"),
                allowedTimeJumpoff = json.optInt("allowed_time_jumpoff"),
                registeredCount = json.optInt("registered_count"),
                rankingCount = json.optInt("ranking_count"),
                startedCount = json.optInt("started_count"),
                clearedCount = json.optInt("cleared_count"),
                comingupCount = json.optInt("comingup_count"),
                tableType = json.optInt("table_type"),
                twoPhase = json.optInt("two_phase")
            )
        }
    }
}
