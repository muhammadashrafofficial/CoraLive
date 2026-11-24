package com.example.coralive.core.network

import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EspnApiService {

     @GET("apis/site/v2/sports/soccer/{league}/scoreboard")
    suspend fun getScoreboard(
        @Path("league") league: String,
        @Query("dates") dates: String? = null
    ): JsonObject

     @GET("apis/site/v2/sports/soccer/{league}/summary")
    suspend fun getMatchSummary(
        @Path("league") league: String,
        @Query("event") eventId: String
    ): JsonObject

     @GET("apis/v2/sports/soccer/{league}/standings")
    suspend fun getStandings(
        @Path("league") league: String,
        @Query("season") season: Int? = null
    ): JsonObject

     @GET("apis/site/v2/sports/soccer/{league}/statistics")
    suspend fun getLeagueStatistics(
        @Path("league") league: String
    ): JsonObject

    @GET("apis/site/v2/sports/soccer/{league}/teams/{teamId}")
    suspend fun getTeamDetails(
        @Path("league") league: String,
        @Path("teamId") teamId: String
    ): JsonObject

    @GET("apis/site/v2/sports/soccer/{league}/teams/{teamId}/roster")
    suspend fun getTeamRoster(
        @Path("league") league: String,
        @Path("teamId") teamId: String
    ): JsonObject

    @GET("apis/site/v2/sports/soccer/{league}/teams/{teamId}/schedule")
    suspend fun getTeamSchedule(
        @Path("league") league: String,
        @Path("teamId") teamId: String
    ): JsonObject

    @GET("apis/common/v3/sports/soccer/athletes/{athleteId}")
    suspend fun getAthleteProfile(
        @Path("athleteId") athleteId: String
    ): JsonObject

}