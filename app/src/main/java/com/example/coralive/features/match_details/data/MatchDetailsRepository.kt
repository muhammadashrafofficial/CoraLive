package com.example.coralive.features.match_details.data

import com.example.coralive.core.network.EspnApiService
import com.example.coralive.features.match_details.domain.MatchDetails
import com.example.coralive.features.match_details.domain.MatchEventItem
import com.example.coralive.features.match_details.domain.MatchEventType
import com.example.coralive.features.match_details.domain.PlayerLineupItem
import com.example.coralive.features.match_details.domain.StatComparisonItem
import com.example.coralive.features.matches.domain.TeamInfo
import kotlinx.serialization.json.*

class MatchDetailsRepository(private val api: EspnApiService) {

    suspend fun getMatchDetails(leagueCode: String, eventId: String): MatchDetails {
        return parseMatchDetails(api.getMatchSummary(league = leagueCode, eventId = eventId), eventId)
    }

    fun parseMatchDetails(root: JsonObject, eventId: String): MatchDetails {

        val header = root["header"]?.jsonObject
        val comp = header?.get("competitions")?.jsonArray?.firstOrNull()?.jsonObject
        val statusType = comp?.get("status")?.jsonObject?.get("type")?.jsonObject
        val state = statusType?.get("state")?.jsonPrimitive?.contentOrNull ?: "pre"
        val statusText = statusType?.get("shortDetail")?.jsonPrimitive?.contentOrNull ?: ""

        var homeTeam = TeamInfo("", "Home", "Home", "", null, "0", false)
        var awayTeam = TeamInfo("", "Away", "Away", "", null, "0", false)

        comp?.get("competitors")?.jsonArray?.forEach { elem ->
            val obj = elem.jsonObject
            val tObj = obj["team"]?.jsonObject ?: JsonObject(emptyMap())
            val logos = tObj["logos"]?.jsonArray
            val logoUrl = tObj["logo"]?.jsonPrimitive?.contentOrNull
                ?: logos?.firstOrNull()?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull

            val info = TeamInfo(
                id = tObj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                name = tObj["displayName"]?.jsonPrimitive?.contentOrNull ?: "",
                shortName = tObj["shortDisplayName"]?.jsonPrimitive?.contentOrNull ?: "",
                abbreviation = tObj["abbreviation"]?.jsonPrimitive?.contentOrNull ?: "",
                logoUrl = logoUrl,
                score = obj["score"]?.jsonPrimitive?.contentOrNull ?: "0",
                isWinner = obj["winner"]?.jsonPrimitive?.booleanOrNull ?: false
            )
            if (obj["homeAway"]?.jsonPrimitive?.contentOrNull == "home") {
                homeTeam = info
            } else {
                awayTeam = info
            }
        }

        // Parse Key Events (Goals, Cards, Substitutions)
        val events = (root["keyEvents"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { elem ->
            val kev = elem.jsonObject
            val typeObj = kev["type"]?.jsonObject
            val typeText = typeObj?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
            val rawType = typeObj?.get("type")?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
            val scoringPlay = kev["scoringPlay"]?.jsonPrimitive?.booleanOrNull ?: false
            val minute = kev["clock"]?.jsonObject?.get("displayValue")?.jsonPrimitive?.contentOrNull ?: ""
            val text = kev["text"]?.jsonPrimitive?.contentOrNull ?: ""
            val teamId = kev["team"]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull

            val eventType = when {
                scoringPlay || rawType.contains("goal") -> MatchEventType.GOAL
                rawType.contains("yellow") -> MatchEventType.YELLOW_CARD
                rawType.contains("red") -> MatchEventType.RED_CARD
                rawType.contains("sub") -> MatchEventType.SUBSTITUTION
                else -> MatchEventType.OTHER
            }

            MatchEventItem(
                id = kev["id"]?.jsonPrimitive?.contentOrNull ?: "${minute}_${text.hashCode()}",
                minuteDisplay = minute,
                type = eventType,
                title = typeText,
                description = text,
                isHomeTeam = teamId == homeTeam.id
            )
        }

        // Parse Match Stats (Boxscore)
        val boxTeams = root["boxscore"]?.jsonObject?.get("teams")?.jsonArray ?: JsonArray(emptyList())
        val homeStatsMap = mutableMapOf<String, Pair<String, String>>()
        val awayStatsMap = mutableMapOf<String, String>()

        boxTeams.forEachIndexed { index, elem ->
            val bt = elem.jsonObject
            val tId = bt["team"]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
            val isHome = (tId == homeTeam.id) || (tId == null && index == 0)
            bt["statistics"]?.jsonArray?.forEach { statElem ->
                val s = statElem.jsonObject
                val name = s["name"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val label = s["label"]?.jsonPrimitive?.contentOrNull ?: name
                val valStr = s["displayValue"]?.jsonPrimitive?.contentOrNull ?: "0"
                if (isHome) {
                    homeStatsMap[name] = label to valStr
                } else {
                    awayStatsMap[name] = valStr
                }
            }
        }

        val statsList = homeStatsMap.map { (key, labelAndHome) ->
            StatComparisonItem(
                label = translateStatLabel(labelAndHome.first),
                homeValue = labelAndHome.second,
                awayValue = awayStatsMap[key] ?: "0"
            )
        }

        // Parse Lineups (Rosters)
        var homeRoster = emptyList<PlayerLineupItem>()
        var awayRoster = emptyList<PlayerLineupItem>()
        root["rosters"]?.jsonArray?.forEachIndexed { idx, rElem ->
            val rGroup = rElem.jsonObject
            val isHome = rGroup["homeAway"]?.jsonPrimitive?.contentOrNull == "home" || idx == 0
            val players = (rGroup["roster"]?.jsonArray ?: JsonArray(emptyList())).map { pElem ->
                val pObj = pElem.jsonObject
                val ath = pObj["athlete"]?.jsonObject
                val pos = pObj["position"]?.jsonObject
                PlayerLineupItem(
                    name = ath?.get("displayName")?.jsonPrimitive?.contentOrNull ?: "",
                    jersey = pObj["jersey"]?.jsonPrimitive?.contentOrNull ?: "-",
                    position = pos?.get("abbreviation")?.jsonPrimitive?.contentOrNull ?: "",
                    isStarter = pObj["starter"]?.jsonPrimitive?.booleanOrNull ?: false
                )
            }
            if (isHome) homeRoster = players else awayRoster = players
        }

        val venueName = root["gameInfo"]?.jsonObject?.get("venue")?.jsonObject?.get("fullName")?.jsonPrimitive?.contentOrNull

        return MatchDetails(
            matchId = eventId,
            venueName = venueName,
            statusText = statusText,
            isLive = state == "in",
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            events = events,
            stats = statsList,
            homeLineup = homeRoster,
            awayLineup = awayRoster
        )
    }

    private fun translateStatLabel(label: String): String {
        return when (label.lowercase()) {
            "possession" -> "الاستحواذ %"
            "shots" -> "إجمالي التسديدات"
            "shots on target" -> "تسديدات على المرمى"
            "fouls" -> "الأخطاء"
            "yellow cards" -> "بطاقات صفراء"
            "red cards" -> "بطاقات حمراء"
            "offsides" -> "التسلل"
            "corner kicks" -> "الركنيات"
            "saves" -> "التصديات"
            else -> label
        }
    }
}
