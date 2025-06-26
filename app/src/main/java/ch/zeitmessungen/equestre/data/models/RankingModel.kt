package ch.zeitmessungen.equestre.data.models
// it is a Map =>  {"ranking":[["<span data-key=\"RANK\"><\/span>","<span data-key=\"NUMBER\"><\/span>","<span data-key=\"HORSE\"><\/span>","<span data-key=\"RIDER\"><\/span>","<span data-key=\"NATION\"><\/span>","<span data-key=\"POINTS\"><\/span>","<span data-key=\"TIME\"><\/span>"],[1,1,"","","","0.00","603.93"],[1,7,"","","","0.00","90.24"],[1,9,"","","","0.00","86.57"],[1,11,"","","","0.00","82.09"],[5,4,"","","","4.00","110.22"],[5,6,"","","","4.00","82.46"],[7,5,"","","","8.00","81.75"],[8,10,"","","","16.00","125.73"],[9,3,"","","","<span class=\"point-label\" data-key=\"OFF_COURSE\">OFF_COURSE<\/span>",""],[9,8,"","","","<span class=\"point-label\" data-key=\"OFF_COURSE\">OFF_COURSE<\/span>",""]],"team_ranking":[],"gameInfo":{"allowed_time":0,"allowed_time_jumpoff":0,"registered_count":10,"ranking_count":10,"started_count":10,"cleared_count":4,"comingup_count":4,"table_type":0,"two_phase":0}}
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
