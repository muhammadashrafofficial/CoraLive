package com.example.coralive.features.matches.domain

data class TeamInfo(
    val id: String,
    val name: String,
    val shortName: String,
    val abbreviation: String,
    val logoUrl: String?,
    val score: String,
    val isWinner: Boolean
)

data class MatchItem(
    val id: String,
    val leagueCode: String,
    val date: String,
    val statusState: String,
    val statusDetail: String,
    val isLive: Boolean,
    val isCompleted: Boolean,
    val venueName: String?,
    val homeTeam: TeamInfo,
    val awayTeam: TeamInfo
)
