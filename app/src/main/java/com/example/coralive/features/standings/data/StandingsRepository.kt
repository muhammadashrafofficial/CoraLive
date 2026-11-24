package com.example.coralive.features.standings.data

import com.example.coralive.core.network.EspnApiService
import com.example.coralive.features.standings.domain.StandingRowItem
import kotlinx.serialization.json.*

class StandingsRepository(private val api: EspnApiService) {

    suspend fun getStandings(leagueCode: String): List<StandingRowItem> {
        val root = api.getStandings(leagueCode)
        val firstGroup = root["children"]?.jsonArray?.firstOrNull()?.jsonObject ?: root
        val entries = firstGroup["standings"]?.jsonObject?.get("entries")?.jsonArray ?: return emptyList()

        return entries.mapIndexed { idx, entryElem ->
            val entry = entryElem.jsonObject
            val teamObj = entry["team"]?.jsonObject
            val logos = teamObj?.get("logos")?.jsonArray
            val logoUrl = teamObj?.get("logo")?.jsonPrimitive?.contentOrNull
                ?: logos?.firstOrNull()?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull
            val teamId = teamObj?.get("id")?.jsonPrimitive?.contentOrNull ?: ""
            val teamName = teamObj?.get("displayName")?.jsonPrimitive?.contentOrNull
                ?: teamObj?.get("name")?.jsonPrimitive?.contentOrNull
                ?: "Team"

            val statsMap = mutableMapOf<String, Int>()
            entry["stats"]?.jsonArray?.forEach { statElem ->
                val s = statElem.jsonObject
                val name = s["name"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val value = s["value"]?.jsonPrimitive?.doubleOrNull?.toInt() ?: 0
                statsMap[name] = value
            }

            StandingRowItem(
                teamId = teamId,
                rank = statsMap["rank"] ?: (idx + 1),
                teamName = teamName,
                logoUrl = logoUrl,
                played = statsMap["gamesPlayed"] ?: 0,
                won = statsMap["wins"] ?: 0,
                drawn = statsMap["ties"] ?: 0,
                lost = statsMap["losses"] ?: 0,
                goalDiff = statsMap["pointDifferential"] ?: 0,
                points = statsMap["points"] ?: 0
            )
        }
    }}
