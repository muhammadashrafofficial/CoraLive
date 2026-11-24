package com.example.coralive.features.stats.data

import com.example.coralive.core.network.EspnApiService
import com.example.coralive.features.stats.domain.LeagueLeadersData
import com.example.coralive.features.stats.domain.PlayerStatItem
import kotlinx.serialization.json.*

class StatsRepository(private val api: EspnApiService) {

    suspend fun getLeagueLeaders(leagueCode: String): LeagueLeadersData {
        val root = api.getLeagueStatistics(leagueCode)
        val statsArray = root["stats"]?.jsonArray ?: return LeagueLeadersData()

        var scorers = emptyList<PlayerStatItem>()
        var assists = emptyList<PlayerStatItem>()


        val goalCategoryKeys = listOf("goals", "totalgoals", "goal", "scorers")
        val assistCategoryKeys = listOf("assists", "totalassists", "assist", "goalassists")


        for (targetKey in goalCategoryKeys) {
            val matchedCat = statsArray.firstOrNull { elem ->
                val name = elem.jsonObject["name"]?.jsonPrimitive?.contentOrNull?.lowercase()?.trim() ?: ""
                name == targetKey
            }?.jsonObject

            if (matchedCat != null) {
                val leadersArray = matchedCat["leaders"]?.jsonArray
                if (!leadersArray.isNullOrEmpty()) {
                    scorers = leadersArray.mapIndexedNotNull { index, leaderElem ->
                        parseLeader(index + 1, leaderElem.jsonObject)
                    }
                    if (scorers.isNotEmpty()) break
                }
            }
        }


        if (scorers.isEmpty()) {
            for (categoryElem in statsArray) {
                val catObj = categoryElem.jsonObject
                val catName = catObj["name"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                if (catName.contains("goal") &&
                    !catName.contains("per") &&
                    !catName.contains("against") &&
                    !catName.contains("winning") &&
                    !catName.contains("diff")
                ) {
                    val leadersArray = catObj["leaders"]?.jsonArray ?: continue
                    scorers = leadersArray.mapIndexedNotNull { index, leaderElem ->
                        parseLeader(index + 1, leaderElem.jsonObject)
                    }
                    if (scorers.isNotEmpty()) break
                }
            }
        }


        for (targetKey in assistCategoryKeys) {
            val matchedCat = statsArray.firstOrNull { elem ->
                val name = elem.jsonObject["name"]?.jsonPrimitive?.contentOrNull?.lowercase()?.trim() ?: ""
                name == targetKey
            }?.jsonObject

            if (matchedCat != null) {
                val leadersArray = matchedCat["leaders"]?.jsonArray
                if (!leadersArray.isNullOrEmpty()) {
                    assists = leadersArray.mapIndexedNotNull { index, leaderElem ->
                        parseLeader(index + 1, leaderElem.jsonObject)
                    }
                    if (assists.isNotEmpty()) break
                }
            }
        }


        if (assists.isEmpty()) {
            for (categoryElem in statsArray) {
                val catObj = categoryElem.jsonObject
                val catName = catObj["name"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                if (catName.contains("assist") && !catName.contains("per")) {
                    val leadersArray = catObj["leaders"]?.jsonArray ?: continue
                    assists = leadersArray.mapIndexedNotNull { index, leaderElem ->
                        parseLeader(index + 1, leaderElem.jsonObject)
                    }
                    if (assists.isNotEmpty()) break
                }
            }
        }

        return LeagueLeadersData(topScorers = scorers, topAssists = assists)
    }

    private fun parseLeader(rank: Int, leaderObj: JsonObject): PlayerStatItem? {
        val athleteObj = leaderObj["athlete"]?.jsonObject ?: return null
        val id = athleteObj["id"]?.jsonPrimitive?.contentOrNull ?: "$rank"
        val name = athleteObj["displayName"]?.jsonPrimitive?.contentOrNull
            ?: athleteObj["shortName"]?.jsonPrimitive?.contentOrNull ?: return null

        val headshotUrl = athleteObj["headshot"]?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull

        val teamObj = athleteObj["team"]?.jsonObject ?: leaderObj["team"]?.jsonObject
        val teamName = teamObj?.get("shortDisplayName")?.jsonPrimitive?.contentOrNull
            ?: teamObj?.get("displayName")?.jsonPrimitive?.contentOrNull ?: ""
        val teamLogoUrl = teamObj?.get("logos")?.jsonArray?.firstOrNull()?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull
            ?: teamObj?.get("logo")?.jsonPrimitive?.contentOrNull


        val rawStr = leaderObj["displayValue"]?.jsonPrimitive?.contentOrNull
            ?: leaderObj["value"]?.jsonPrimitive?.contentOrNull
            ?: "0"
        val cleanValue = rawStr.toDoubleOrNull()?.toInt()?.toString() ?: rawStr.trim()

        return PlayerStatItem(
            rank = rank,
            playerId = id,
            playerName = name,
            playerHeadshotUrl = headshotUrl,
            teamName = teamName,
            teamLogoUrl = teamLogoUrl,
            value = cleanValue
        )
    }
}