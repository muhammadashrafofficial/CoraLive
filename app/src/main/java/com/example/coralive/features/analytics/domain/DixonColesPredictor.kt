package com.example.coralive.features.analytics.domain

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max


object DixonColesPredictor {

    data class TeamStandingInput(
        val teamId: String,
        val teamName: String,
        val played: Int,
        val wins: Int,
        val draws: Int,
        val losses: Int,
        val goalsFor: Int,
        val goalsAgainst: Int,
        val points: Int
    )

    fun analyzeMatch(
        homeTeamId: String,
        awayTeamId: String,
        homeTeamFallbackName: String,
        awayTeamFallbackName: String,
        standings: List<TeamStandingInput>,
        homeAdvantage: Double = 0.22,
        rho: Double = -0.06,
        maxGoals: Int = 7
    ): MatchAnalyticsReport {
        val validTeams = standings.filter { it.played > 0 }

         val totalGoals = validTeams.sumOf { it.goalsFor }.toDouble()
        val totalMatches = validTeams.sumOf { it.played }.toDouble()
        val leagueAvgGoalsPerTeam = if (totalMatches > 0) (totalGoals / totalMatches).coerceIn(0.8, 2.5) else 1.35

        val homeData = validTeams.find { it.teamId == homeTeamId || it.teamName.equals(homeTeamFallbackName, true) }
        val awayData = validTeams.find { it.teamId == awayTeamId || it.teamName.equals(awayTeamFallbackName, true) }

         val homeGfPerGame = if (homeData != null && homeData.played > 0) homeData.goalsFor.toDouble() / homeData.played else leagueAvgGoalsPerTeam
        val homeGaPerGame = if (homeData != null && homeData.played > 0) homeData.goalsAgainst.toDouble() / homeData.played else leagueAvgGoalsPerTeam

        val awayGfPerGame = if (awayData != null && awayData.played > 0) awayData.goalsFor.toDouble() / awayData.played else leagueAvgGoalsPerTeam
        val awayGaPerGame = if (awayData != null && awayData.played > 0) awayData.goalsAgainst.toDouble() / awayData.played else leagueAvgGoalsPerTeam

         val homeAttack = ln(max(homeGfPerGame, 0.25) / leagueAvgGoalsPerTeam)
        val homeDefence = -ln(max(homeGaPerGame, 0.25) / leagueAvgGoalsPerTeam)

        val awayAttack = ln(max(awayGfPerGame, 0.25) / leagueAvgGoalsPerTeam)
        val awayDefence = -ln(max(awayGaPerGame, 0.25) / leagueAvgGoalsPerTeam)

         val lambdaHome = (leagueAvgGoalsPerTeam * exp(homeAttack - awayDefence + homeAdvantage)).coerceIn(0.25, 4.5)
        val lambdaAway = (leagueAvgGoalsPerTeam * exp(awayAttack - homeDefence)).coerceIn(0.20, 4.0)

         val matrix = Array(maxGoals + 1) { DoubleArray(maxGoals + 1) }
        var totalMass = 0.0

        for (h in 0..maxGoals) {
            for (a in 0..maxGoals) {
                val baseProb = poissonPmf(h, lambdaHome) * poissonPmf(a, lambdaAway)
                val tau = dcLowScoreCorrection(h, a, lambdaHome, lambdaAway, rho)
                val cellProb = max(baseProb * tau, 0.0)
                matrix[h][a] = cellProb
                totalMass += cellProb
            }
        }

         var probHomeWin = 0.0
        var probDraw = 0.0
        var probAwayWin = 0.0
        val allScorelines = mutableListOf<ScorelineProb>()

        for (h in 0..maxGoals) {
            for (a in 0..maxGoals) {
                val normProb = if (totalMass > 0) matrix[h][a] / totalMass else 0.0
                allScorelines.add(ScorelineProb(h, a, normProb))
                when {
                    h > a -> probHomeWin += normProb
                    h == a -> probDraw += normProb
                    else -> probAwayWin += normProb
                }
            }
        }

        val sortedScorelines = allScorelines.sortedByDescending { it.probability }

        val minGf = validTeams.minOfOrNull { it.goalsFor.toDouble() / it.played } ?: 0.5
        val maxGf = validTeams.maxOfOrNull { it.goalsFor.toDouble() / it.played } ?: 2.5
        val minGa = validTeams.minOfOrNull { it.goalsAgainst.toDouble() / it.played } ?: 0.5
        val maxGa = validTeams.maxOfOrNull { it.goalsAgainst.toDouble() / it.played } ?: 2.5

        fun scaleAttack(gf: Double): Double =
            if (maxGf > minGf) (((gf - minGf) / (maxGf - minGf)) * 100.0).coerceIn(10.0, 100.0) else 50.0

        fun scaleDefence(ga: Double): Double =
            if (maxGa > minGa) ((1.0 - (ga - minGa) / (maxGa - minGa)) * 100.0).coerceIn(10.0, 100.0) else 50.0

        val homeProfile = TeamAnalyticsProfile(
            teamName = homeData?.teamName ?: homeTeamFallbackName,
            attackRating = scaleAttack(homeGfPerGame),
            defenceRating = scaleDefence(homeGaPerGame),
            goalsPerGame = homeGfPerGame,
            concededPerGame = homeGaPerGame,
            pointsPerGame = if (homeData != null && homeData.played > 0) homeData.points.toDouble() / homeData.played else 1.2
        )

        val awayProfile = TeamAnalyticsProfile(
            teamName = awayData?.teamName ?: awayTeamFallbackName,
            attackRating = scaleAttack(awayGfPerGame),
            defenceRating = scaleDefence(awayGaPerGame),
            goalsPerGame = awayGfPerGame,
            concededPerGame = awayGaPerGame,
            pointsPerGame = if (awayData != null && awayData.played > 0) awayData.points.toDouble() / awayData.played else 1.2
        )

        return MatchAnalyticsReport(
            homeWinProb = probHomeWin,
            drawProb = probDraw,
            awayWinProb = probAwayWin,
            expectedHomeGoals = lambdaHome,
            expectedAwayGoals = lambdaAway,
            mostLikelyScore = sortedScorelines.first(),
            topScorelines = sortedScorelines.take(4),
            homeProfile = homeProfile,
            awayProfile = awayProfile
        )
    }

    private fun poissonPmf(k: Int, lambda: Double): Double {
        var factorial = 1.0
        for (i in 2..k) factorial *= i
        var power = 1.0
        for (i in 1..k) power *= lambda
        return (power * exp(-lambda)) / factorial
    }

    private fun dcLowScoreCorrection(
        homeGoals: Int,
        awayGoals: Int,
        lambdaHome: Double,
        lambdaAway: Double,
        rho: Double
    ): Double {
        return when {
            homeGoals == 0 && awayGoals == 0 -> 1.0 - (lambdaHome * lambdaAway * rho)
            homeGoals == 0 && awayGoals == 1 -> 1.0 + (lambdaHome * rho)
            homeGoals == 1 && awayGoals == 0 -> 1.0 + (lambdaAway * rho)
            homeGoals == 1 && awayGoals == 1 -> 1.0 - rho
            else -> 1.0
        }
    }
}