package com.example.coralive.features.standings.domain

data class StandingRowItem(
    val teamId: String = "",
    val rank: Int,
    val teamName: String,
    val logoUrl: String?,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalDiff: Int,
    val points: Int
)
