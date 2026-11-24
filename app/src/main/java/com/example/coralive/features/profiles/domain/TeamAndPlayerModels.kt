package com.example.coralive.features.profiles.domain


import com.example.coralive.features.matches.domain.MatchItem

data class TeamProfileData(
    val id: String,
    val leagueCode: String,
    val name: String,
    val shortName: String,
    val abbreviation: String,
    val logoUrl: String?,
    val venueName: String,
    val venueCity: String,
    val standingSummary: String,
    val isNotificationsEnabled: Boolean = false,
    val upcomingMatches: List<MatchItem> = emptyList(),
    val pastMatches: List<MatchItem> = emptyList(),
    val squad: List<SquadPlayerItem> = emptyList(),
    val teamStats: List<KeyMetricItem> = emptyList(),
    val topScorer: TeamLeaderHighlight? = null,
    val topAssister: TeamLeaderHighlight? = null,
    val historyAndHonors: List<KeyMetricItem> = emptyList()
)

data class SquadPlayerItem(
    val id: String,
    val name: String,
    val jersey: String,
    val position: String,
    val age: String,
    val nationality: String,
    val headshotUrl: String?,
    val goals: Int = 0,
    val assists: Int = 0
)

data class TeamLeaderHighlight(
    val playerId: String,
    val playerName: String,
    val headshotUrl: String?,
    val value: String,
    val label: String
)

data class KeyMetricItem(
    val label: String,
    val value: String
)

data class PlayerProfileData(
    val id: String,
    val name: String,
    val jersey: String,
    val position: String,
    val teamName: String,
    val teamId: String,
    val nationality: String,
    val age: String,
    val height: String,
    val weight: String,
    val headshotUrl: String?,
    val seasonStats: List<KeyMetricItem> = emptyList(),
    val careerHistory: List<CareerClubEntry> = emptyList()
)

data class CareerClubEntry(
    val seasonOrPeriod: String,
    val clubName: String,
    val appearances: String,
    val goals: String
)