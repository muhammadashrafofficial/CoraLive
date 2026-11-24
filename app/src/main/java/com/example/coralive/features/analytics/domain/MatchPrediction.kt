package com.example.coralive.features.analytics.domain

data class ScorelineProb(
    val homeGoals: Int,
    val awayGoals: Int,
    val probability: Double
)

data class TeamAnalyticsProfile(
    val teamName: String,
    val attackRating: Double,
    val defenceRating: Double,
    val goalsPerGame: Double,
    val concededPerGame: Double,
    val pointsPerGame: Double
)

data class MatchAnalyticsReport(
    val homeWinProb: Double,
    val drawProb: Double,
    val awayWinProb: Double,
    val expectedHomeGoals: Double,
    val expectedAwayGoals: Double,
    val mostLikelyScore: ScorelineProb,
    val topScorelines: List<ScorelineProb>,
    val homeProfile: TeamAnalyticsProfile,
    val awayProfile: TeamAnalyticsProfile
)