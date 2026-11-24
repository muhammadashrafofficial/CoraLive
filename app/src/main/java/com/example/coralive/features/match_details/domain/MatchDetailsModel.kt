package com.example.coralive.features.match_details.domain

import com.example.coralive.features.matches.domain.TeamInfo


enum class MatchEventType {
    GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION, OTHER
}

data class MatchEventItem(
    val id: String,
    val minuteDisplay: String,
    val type: MatchEventType,
    val title: String,
    val description: String,
    val isHomeTeam: Boolean
)

data class StatComparisonItem(
    val label: String,
    val homeValue: String,
    val awayValue: String
)

data class PlayerLineupItem(
    val name: String,
    val jersey: String,
    val position: String,
    val isStarter: Boolean
)

data class MatchDetails(
    val matchId: String,
    val venueName: String?,
    val statusText: String,
    val isLive: Boolean,
    val homeTeam: TeamInfo,
    val awayTeam: TeamInfo,
    val events: List<MatchEventItem>,
    val stats: List<StatComparisonItem>,
    val homeLineup: List<PlayerLineupItem>,
    val awayLineup: List<PlayerLineupItem>
)
