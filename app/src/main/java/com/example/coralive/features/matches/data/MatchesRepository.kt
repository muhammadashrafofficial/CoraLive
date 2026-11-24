package com.example.coralive.features.matches.data

import com.example.coralive.core.network.EspnApiService
import com.example.coralive.features.matches.domain.MatchItem
import com.example.coralive.features.matches.domain.TeamInfo
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*

class MatchesRepository(private val api: EspnApiService) {

    suspend fun getMatches(leagueCode: String, dayOffset: Int): List<MatchItem> {
         val localCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStartMillis = localCal.timeInMillis
        val dayEndMillis = dayStartMillis + 24L * 60 * 60 * 1000

         val queryFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val singleDateParam = queryFormat.format(localCal.time)

        val root = api.getScoreboard(league = leagueCode, dates = singleDateParam)
        val events = root["events"]?.jsonArray ?: return emptyList()

         val nextDayCal = Calendar.getInstance().apply {
            timeInMillis = dayStartMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val nextDayEvents = try {
            val nextRoot = api.getScoreboard(league = leagueCode, dates = queryFormat.format(nextDayCal.time))
            nextRoot["events"]?.jsonArray ?: emptyJsonArray()
        } catch (_: Exception) {
            emptyJsonArray()
        }

        val combinedEvents = (events + nextDayEvents).distinctBy {
            it.jsonObject["id"]?.jsonPrimitive?.contentOrNull
        }

        return combinedEvents.mapNotNull { element ->
            val ev = element.jsonObject
            val id = ev["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val dateStr = ev["date"]?.jsonPrimitive?.contentOrNull ?: ""

             val matchMillis = parseIsoDateToMillis(dateStr)
            if (matchMillis != null && (matchMillis < dayStartMillis || matchMillis >= dayEndMillis)) {
                return@mapNotNull null
            }

            val comps = ev["competitions"]?.jsonArray ?: return@mapNotNull null
            val comp = comps.firstOrNull()?.jsonObject ?: return@mapNotNull null

            val statusObj = (ev["status"] ?: comp["status"])?.jsonObject
            val statusType = statusObj?.get("type")?.jsonObject
            val state = statusType?.get("state")?.jsonPrimitive?.contentOrNull ?: "pre"

            val rawDetail = statusType?.get("shortDetail")?.jsonPrimitive?.contentOrNull
                ?: statusType?.get("detail")?.jsonPrimitive?.contentOrNull ?: ""

            val detail = if (state == "pre" && matchMillis != null) {
                formatLocalTime(matchMillis)
            } else {
                rawDetail
            }

            val completed = statusType?.get("completed")?.jsonPrimitive?.booleanOrNull ?: false
            val venueName = comp["venue"]?.jsonObject?.get("fullName")?.jsonPrimitive?.contentOrNull

            var homeTeam: TeamInfo? = null
            var awayTeam: TeamInfo? = null

            val competitors = comp["competitors"]?.jsonArray ?: emptyJsonArray()
            for (cElem in competitors) {
                val cObj = cElem.jsonObject
                val homeAway = cObj["homeAway"]?.jsonPrimitive?.contentOrNull
                val parsedTeam = parseTeam(cObj, state != "pre")
                if (homeAway == "home") homeTeam = parsedTeam else awayTeam = parsedTeam
            }

            if (homeTeam != null && awayTeam != null) {
                MatchItem(
                    id = id,
                    leagueCode = leagueCode,
                    date = dateStr,
                    statusState = state,
                    statusDetail = detail,
                    isLive = state == "in",
                    isCompleted = completed,
                    venueName = venueName,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam
                )
            } else null
        }.sortedBy { parseIsoDateToMillis(it.date) ?: Long.MAX_VALUE }
    }

    suspend fun getMatchesByDate(leagueCode: String, dateStr: String): List<MatchItem> {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
        val targetDate = java.time.LocalDate.parse(dateStr, formatter)
        val dayOffset = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), targetDate).toInt()
        return getMatches(leagueCode, dayOffset = dayOffset)
    }

    private fun parseIsoDateToMillis(isoDate: String): Long? {
        if (isoDate.isBlank()) return null
        val patterns = listOf("yyyy-MM-dd'T'HH:mm'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                return sdf.parse(isoDate)?.time
            } catch (_: Exception) { }
        }
        return null
    }

    private fun formatLocalTime(millis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun parseTeam(compObj: JsonObject, showScore: Boolean): TeamInfo {
        val teamObj = compObj["team"]?.jsonObject ?: JsonObject(emptyMap())
        val logos = teamObj["logos"]?.jsonArray
        val logoUrl = teamObj["logo"]?.jsonPrimitive?.contentOrNull
            ?: logos?.firstOrNull()?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull

        val rawScore = compObj["score"]?.jsonPrimitive?.contentOrNull ?: "0"
        return TeamInfo(
            id = teamObj["id"]?.jsonPrimitive?.contentOrNull ?: "",
            name = teamObj["displayName"]?.jsonPrimitive?.contentOrNull ?: "",
            shortName = teamObj["shortDisplayName"]?.jsonPrimitive?.contentOrNull
                ?: teamObj["name"]?.jsonPrimitive?.contentOrNull ?: "",
            abbreviation = teamObj["abbreviation"]?.jsonPrimitive?.contentOrNull ?: "",
            logoUrl = logoUrl,
            score = if (showScore) rawScore else "-",
            isWinner = compObj["winner"]?.jsonPrimitive?.booleanOrNull ?: false
        )
    }

    private fun emptyJsonArray() = JsonArray(emptyList())
}