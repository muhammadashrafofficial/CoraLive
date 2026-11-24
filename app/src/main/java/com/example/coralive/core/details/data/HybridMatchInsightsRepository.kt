package com.example.coralive.core.details.data

import com.example.coralive.core.network.EspnApiService
import kotlinx.serialization.json.*

data class TimelineEventItem(
    val id: String,
    val minute: String,
    val type: String,
    val title: String,
    val scoreSnapshot: String,
    val isHomeTeam: Boolean?
)

class HybridMatchInsightsRepository(
    private val espnApi: EspnApiService
) {
    suspend fun getImportantTimeline(
        leagueCode: String,
        matchId: String,
        homeTeamId: String
    ): List<TimelineEventItem> {
        return try {
            parseTimeline(espnApi.getMatchSummary(leagueCode, matchId), homeTeamId)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseTimeline(summary: JsonObject, homeTeamId: String): List<TimelineEventItem> {
        return try {
            val keyEvents = summary["keyEvents"]?.jsonArray ?: return emptyList()
            keyEvents.mapIndexedNotNull { index, elem ->
                val obj = elem.jsonObject
                val text = obj["text"]?.jsonPrimitive?.contentOrNull
                    ?: obj["shortText"]?.jsonPrimitive?.contentOrNull
                    ?: return@mapIndexedNotNull null
                val clock = obj["clock"]?.jsonObject?.get("displayValue")?.jsonPrimitive?.contentOrNull ?: ""
                val isScoring = obj["scoringPlay"]?.jsonPrimitive?.booleanOrNull == true
                val typeText = obj["type"]?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                val teamId = obj["team"]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull

                val eventType = when {
                    isScoring && "pen" in typeText -> "penalty"
                    isScoring -> "goal"
                    "red" in typeText -> "red"
                    "yellow" in typeText -> "yellow"
                    "sub" in typeText -> "sub"
                    else -> "other"
                }

                TimelineEventItem(
                    id = "espn_$index",
                    minute = clock,
                    type = eventType,
                    title = text,
                    scoreSnapshot = "",
                    isHomeTeam = teamId?.let { it == homeTeamId }
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}