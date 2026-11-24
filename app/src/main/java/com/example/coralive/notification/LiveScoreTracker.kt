package com.example.coralive.notification

import android.content.Context

object LiveScoreTracker {
    private const val PREFS_NAME = "live_scores_cache"
    private const val ALERTS_PREFS_NAME = "premier_live_prefs"

    fun checkAndNotifyGoals(
        context: Context,
        matches: List<TrackedMatch>
    ) {
        val cachePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alertsPrefs = context.getSharedPreferences(ALERTS_PREFS_NAME, Context.MODE_PRIVATE)

        val favMatches = alertsPrefs.getStringSet("favorite_matches", emptySet()) ?: emptySet()
        val favTeams = alertsPrefs.getStringSet("favorite_teams", emptySet()) ?: emptySet()

        val editor = cachePrefs.edit()

        for (match in matches) {
            if (!match.isLive) continue

            val isMatchSubscribed = favMatches.contains(match.id) ||
                    (match.homeTeamId.isNotBlank() && favTeams.contains(match.homeTeamId)) ||
                    (match.awayTeamId.isNotBlank() && favTeams.contains(match.awayTeamId))

            val key = "score_${match.id}"
            val previousScore = cachePrefs.getString(key, null)
            val currentScore = "${match.homeScore}-${match.awayScore}"

            if (isMatchSubscribed && previousScore != null && previousScore != currentScore) {
                val prevParts = previousScore.split("-").mapNotNull { it.toIntOrNull() }
                if (prevParts.size == 2) {
                    val goalScored = match.homeScore > prevParts[0] || match.awayScore > prevParts[1]
                    if (goalScored) {
                        GoalNotificationHelper.showGoalNotification(
                            context = context,
                            matchId = match.id,
                            homeTeam = match.homeTeam,
                            awayTeam = match.awayTeam,
                            homeScore = match.homeScore,
                            awayScore = match.awayScore
                        )
                    }
                }
            }
            editor.putString(key, currentScore)
        }
        editor.apply()
    }
}

data class TrackedMatch(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamId: String = "",
    val awayTeamId: String = "",
    val homeScore: Int,
    val awayScore: Int,
    val isLive: Boolean
)