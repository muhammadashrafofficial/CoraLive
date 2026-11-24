package com.example.coralive.features.stats.domain


data class PlayerStatItem(
    val rank: Int,
    val playerId: String,
    val playerName: String,
    val playerHeadshotUrl: String?,
    val teamName: String,
    val teamLogoUrl: String?,
    val value: String
)

data class LeagueLeadersData(
    val topScorers: List<PlayerStatItem> = emptyList(),
    val topAssists: List<PlayerStatItem> = emptyList()
)