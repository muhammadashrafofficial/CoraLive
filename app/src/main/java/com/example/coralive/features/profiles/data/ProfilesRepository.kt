package com.example.coralive.features.profiles.data


import com.example.coralive.core.network.EspnApiService
import com.example.coralive.features.matches.domain.MatchItem
import com.example.coralive.features.matches.domain.TeamInfo
import com.example.coralive.features.profiles.domain.*
import kotlinx.serialization.json.*

class ProfilesRepository(private val api: EspnApiService) {

    suspend fun getTeamProfile(
        leagueCode: String,
        teamId: String,
        isNotifEnabled: Boolean
    ): TeamProfileData {
        val teamRoot = api.getTeamDetails(leagueCode, teamId)
        val teamObj = teamRoot["team"]?.jsonObject ?: JsonObject(emptyMap())

        val name = teamObj["displayName"]?.jsonPrimitive?.contentOrNull ?: "Club"
        val shortName = teamObj["shortDisplayName"]?.jsonPrimitive?.contentOrNull ?: name
        val abbr = teamObj["abbreviation"]?.jsonPrimitive?.contentOrNull
            ?: shortName.take(3).uppercase()
        val logoUrl = teamObj["logos"]?.jsonArray?.firstOrNull()?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull
        val venueObj = teamObj["franchise"]?.jsonObject?.get("venue")?.jsonObject
        val venueName = venueObj?.get("fullName")?.jsonPrimitive?.contentOrNull ?: "Main Stadium"
        val venueCity = venueObj?.get("address")?.jsonObject?.get("city")?.jsonPrimitive?.contentOrNull ?: ""
        val standingSummary = teamObj["standingSummary"]?.jsonPrimitive?.contentOrNull ?: ""

         val recordItems = teamObj["record"]?.jsonObject?.get("items")?.jsonArray
        val firstRecord = recordItems?.firstOrNull()?.jsonObject
        val summaryRecord = firstRecord?.get("summary")?.jsonPrimitive?.contentOrNull ?: "-"
        val teamStats = mutableListOf(
            KeyMetricItem("سجل الموسم (ف-ت-خ)", summaryRecord)
        )
        firstRecord?.get("stats")?.jsonArray?.forEach { st ->
            val obj = st.jsonObject
            val statName = obj["name"]?.jsonPrimitive?.contentOrNull ?: ""
            val valStr = obj["displayValue"]?.jsonPrimitive?.contentOrNull
                ?: obj["value"]?.jsonPrimitive?.doubleOrNull?.toInt()?.toString()
            if (valStr != null && statName in listOf("points", "pointsFor", "pointsAgainst", "goalDifferential", "gamesPlayed")) {
                val arLabel = when (statName) {
                    "points" -> "النقاط"
                    "pointsFor" -> "أهداف مسجلة"
                    "pointsAgainst" -> "أهداف مستقبلة"
                    "goalDifferential" -> "فارق الأهداف"
                    "gamesPlayed" -> "المباريات الملعوبة"
                    else -> statName
                }
                teamStats.add(KeyMetricItem(arLabel, valStr))
            }
        }

         val upcoming = mutableListOf<MatchItem>()
        val past = mutableListOf<MatchItem>()
        try {
            val schedRoot = api.getTeamSchedule(leagueCode, teamId)
            val events = schedRoot["events"]?.jsonArray ?: JsonArray(emptyList())
            for (evElem in events) {
                val ev = evElem.jsonObject
                val comp = ev["competitions"]?.jsonArray?.firstOrNull()?.jsonObject ?: continue
                val statusType = comp["status"]?.jsonObject?.get("type")?.jsonObject
                val state = statusType?.get("state")?.jsonPrimitive?.contentOrNull ?: "pre"
                val detail = statusType?.get("shortDetail")?.jsonPrimitive?.contentOrNull ?: ""

                var home = TeamInfo("", "Home", "Home", "", null, "0", false)
                var away = TeamInfo("", "Away", "Away", "", null, "0", false)

                comp["competitors"]?.jsonArray?.forEach { cElem ->
                    val c = cElem.jsonObject
                    val t = c["team"]?.jsonObject ?: JsonObject(emptyMap())
                    val scoreVal = c["score"]?.jsonObject?.get("displayValue")?.jsonPrimitive?.contentOrNull
                        ?: c["score"]?.jsonPrimitive?.contentOrNull ?: "0"
                    val info = TeamInfo(
                        id = t["id"]?.jsonPrimitive?.contentOrNull ?: "",
                        name = t["displayName"]?.jsonPrimitive?.contentOrNull ?: "",
                        shortName = t["shortDisplayName"]?.jsonPrimitive?.contentOrNull ?: "",
                        abbreviation = t["abbreviation"]?.jsonPrimitive?.contentOrNull ?: "",
                        logoUrl = t["logos"]?.jsonArray?.firstOrNull()?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull,
                        score = scoreVal,
                        isWinner = c["winner"]?.jsonPrimitive?.booleanOrNull ?: false
                    )
                    if (c["homeAway"]?.jsonPrimitive?.contentOrNull == "home") home = info else away = info
                }

                val item = MatchItem(
                    id = ev["id"]?.jsonPrimitive?.contentOrNull ?: "",
                    leagueCode = leagueCode,
                    date = ev["date"]?.jsonPrimitive?.contentOrNull ?: "",
                    statusState = state,
                    statusDetail = detail,
                    isLive = state == "in",
                    isCompleted = state == "post",
                    venueName = comp["venue"]?.jsonObject?.get("fullName")?.jsonPrimitive?.contentOrNull,
                    homeTeam = home,
                    awayTeam = away
                )
                if (state == "post") past.add(item) else upcoming.add(item)
            }
        } catch (_: Exception) { }

         val squadList = mutableListOf<SquadPlayerItem>()
        try {
            val rosterRoot = api.getTeamRoster(leagueCode, teamId)
            val athletes = rosterRoot["athletes"]?.jsonArray ?: JsonArray(emptyList())
            for (athElem in athletes) {
                val ath = athElem.jsonObject
                val pId = ath["id"]?.jsonPrimitive?.contentOrNull ?: continue
                val pName = ath["displayName"]?.jsonPrimitive?.contentOrNull ?: continue
                val jersey = ath["jersey"]?.jsonPrimitive?.contentOrNull ?: "-"
                val pos = ath["position"]?.jsonObject?.get("abbreviation")?.jsonPrimitive?.contentOrNull ?: "-"
                val age = ath["age"]?.jsonPrimitive?.intOrNull?.toString() ?: "-"
                val nat = ath["citizenship"]?.jsonPrimitive?.contentOrNull ?: "-"
                val headshot = ath["headshot"]?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull

                var goals = 0
                var assists = 0
                ath["statistics"]?.jsonObject?.get("splits")?.jsonObject?.get("categories")?.jsonArray?.forEach { cat ->
                    cat.jsonObject["stats"]?.jsonArray?.forEach { st ->
                        val sObj = st.jsonObject
                        val sName = sObj["name"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
                        val sVal = sObj["value"]?.jsonPrimitive?.doubleOrNull?.toInt() ?: 0
                        if (sName == "totalgoals" || sName == "goals") goals = sVal
                        if (sName == "goalassists" || sName == "assists") assists = sVal
                    }
                }

                squadList.add(
                    SquadPlayerItem(
                        id = pId,
                        name = pName,
                        jersey = jersey,
                        position = pos,
                        age = age,
                        nationality = nat,
                        headshotUrl = headshot,
                        goals = goals,
                        assists = assists
                    )
                )
            }
        } catch (_: Exception) { }

        val topScorerPlayer = squadList.maxByOrNull { it.goals }?.takeIf { it.goals > 0 }
        val topAssistPlayer = squadList.maxByOrNull { it.assists }?.takeIf { it.assists > 0 }

        val historyHonors = listOf(
            KeyMetricItem("النادي", name),
            KeyMetricItem("الملعب الرئيسي", venueName),
            KeyMetricItem("المدينة", venueCity.ifBlank { "أوروبا" }),
            KeyMetricItem("الوضع الحالي", standingSummary.ifBlank { "منافس في الدوري" }),
            KeyMetricItem("عدد لاعبي القائمة", "${squadList.size} لاعب")
        )

        return TeamProfileData(
            id = teamId,
            leagueCode = leagueCode,
            name = name,
            shortName = shortName,
            abbreviation = abbr,
            logoUrl = logoUrl,
            venueName = venueName,
            venueCity = venueCity,
            standingSummary = standingSummary,
            isNotificationsEnabled = isNotifEnabled,
            upcomingMatches = upcoming.take(10),
            pastMatches = past.takeLast(10).reversed(),
            squad = squadList,
            teamStats = teamStats,
            topScorer = topScorerPlayer?.let {
                TeamLeaderHighlight(it.id, it.name, it.headshotUrl, it.goals.toString(), "هداف النادي")
            },
            topAssister = topAssistPlayer?.let {
                TeamLeaderHighlight(it.id, it.name, it.headshotUrl, it.assists.toString(), "الأكثر صناعة")
            },
            historyAndHonors = historyHonors
        )
    }

    suspend fun getPlayerProfile(playerId: String): PlayerProfileData {
        val root = api.getAthleteProfile(playerId)
        val ath = root["athlete"]?.jsonObject ?: JsonObject(emptyMap())

        val name = ath["displayName"]?.jsonPrimitive?.contentOrNull ?: "Player"
        val jersey = ath["jersey"]?.jsonPrimitive?.contentOrNull ?: "-"
        val pos = ath["position"]?.jsonObject?.get("displayName")?.jsonPrimitive?.contentOrNull ?: "-"
        val teamObj = ath["team"]?.jsonObject
        val teamName = teamObj?.get("displayName")?.jsonPrimitive?.contentOrNull ?: "-"
        val teamId = teamObj?.get("id")?.jsonPrimitive?.contentOrNull ?: ""
        val nat = ath["citizenship"]?.jsonPrimitive?.contentOrNull ?: "-"
        val age = ath["age"]?.jsonPrimitive?.intOrNull?.toString()?.let { "$it سنة" } ?: "-"
        val height = ath["displayHeight"]?.jsonPrimitive?.contentOrNull ?: "-"
        val weight = ath["displayWeight"]?.jsonPrimitive?.contentOrNull ?: "-"
        val headshot = ath["headshot"]?.jsonObject?.get("href")?.jsonPrimitive?.contentOrNull

        val statsList = mutableListOf<KeyMetricItem>()
        ath["statsSummary"]?.jsonObject?.get("statistics")?.jsonArray?.forEach { stElem ->
            val st = stElem.jsonObject
            val label = st["displayName"]?.jsonPrimitive?.contentOrNull ?: return@forEach
            val valStr = st["displayValue"]?.jsonPrimitive?.contentOrNull ?: "0"
            statsList.add(KeyMetricItem(label, valStr))
        }

        val careerList = mutableListOf<CareerClubEntry>()
        ath["seasonHistory"]?.jsonArray?.forEach { hElem ->
            val h = hElem.jsonObject
            val season = h["displayName"]?.jsonPrimitive?.contentOrNull ?: "-"
            val club = h["team"]?.jsonObject?.get("displayName")?.jsonPrimitive?.contentOrNull ?: teamName
            careerList.add(
                CareerClubEntry(
                    seasonOrPeriod = season,
                    clubName = club,
                    appearances = h["appearances"]?.jsonPrimitive?.contentOrNull ?: "-",
                    goals = h["goals"]?.jsonPrimitive?.contentOrNull ?: "-"
                )
            )
        }
        if (careerList.isEmpty()) {
            careerList.add(
                CareerClubEntry(
                    seasonOrPeriod = "الموسم الحالي",
                    clubName = teamName,
                    appearances = statsList.firstOrNull()?.value ?: "-",
                    goals = statsList.firstOrNull { it.label.contains("Goal", true) }?.value ?: "0"
                )
            )
        }

        return PlayerProfileData(
            id = playerId,
            name = name,
            jersey = jersey,
            position = pos,
            teamName = teamName,
            teamId = teamId,
            nationality = nat,
            age = age,
            height = height,
            weight = weight,
            headshotUrl = headshot,
            seasonStats = statsList.ifEmpty {
                listOf(
                    KeyMetricItem("النادي الحالي", teamName),
                    KeyMetricItem("المركز", pos),
                    KeyMetricItem("رقم القميص", "#$jersey")
                )
            },
            careerHistory = careerList
        )
    }
}