package com.example.coralive.features.match_details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coralive.core.details.data.HybridMatchInsightsRepository
import com.example.coralive.core.details.data.TimelineEventItem
import com.example.coralive.core.network.EspnApiService
import com.example.coralive.core.network.NetworkModule
import com.example.coralive.features.analytics.domain.DixonColesPredictor
import com.example.coralive.features.analytics.domain.MatchAnalyticsReport
import com.example.coralive.features.match_details.data.MatchDetailsRepository
import com.example.coralive.features.match_details.domain.MatchDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

data class MatchDetailsUiState(
    val details: MatchDetails? = null,
    val analyticsReport: MatchAnalyticsReport? = null,
    val timelineEvents: List<TimelineEventItem> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class MatchDetailsViewModel(
    private val leagueCode: String,
    private val matchId: String,
    private val api: EspnApiService = NetworkModule.apiService,
    private val repository: MatchDetailsRepository = MatchDetailsRepository(api),
    private val insightsRepo: HybridMatchInsightsRepository = HybridMatchInsightsRepository(api)
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailsUiState())
    val uiState: StateFlow<MatchDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadDetails(initial = true)
            while (isActive) {
                delay(10_000L)
                if (_uiState.value.details?.isLive == true) {
                    loadDetails(initial = false)
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun retry() {
        viewModelScope.launch { loadDetails(initial = true) }
    }

    private suspend fun loadDetails(initial: Boolean) {
        if (initial) _uiState.update { it.copy(isLoading = true, error = null) }
        try {

            val summary = api.getMatchSummary(league = leagueCode, eventId = matchId)
            val data = repository.parseMatchDetails(summary, matchId)
            val timeline = insightsRepo.parseTimeline(summary, data.homeTeam.id)
            val currentReport = _uiState.value.analyticsReport ?: computeAnalytics(data)

            _uiState.update {
                it.copy(
                    details = data,
                    analyticsReport = currentReport,
                    timelineEvents = timeline,
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (initial) {
                _uiState.update { it.copy(isLoading = false, error = "تعذر جلب تفاصيل المباراة") }
            }
        }
    }

    private suspend fun computeAnalytics(details: MatchDetails): MatchAnalyticsReport? {
        return try {
            val standingsJson = api.getStandings(leagueCode)
            val standingsInputs = parseStandingsForAnalytics(standingsJson)
            DixonColesPredictor.analyzeMatch(
                homeTeamId = details.homeTeam.id,
                awayTeamId = details.awayTeam.id,
                homeTeamFallbackName = details.homeTeam.shortName,
                awayTeamFallbackName = details.awayTeam.shortName,
                standings = standingsInputs
            )
        } catch (_: Exception) {
            DixonColesPredictor.analyzeMatch(
                homeTeamId = details.homeTeam.id,
                awayTeamId = details.awayTeam.id,
                homeTeamFallbackName = details.homeTeam.shortName,
                awayTeamFallbackName = details.awayTeam.shortName,
                standings = emptyList()
            )
        }
    }

    private fun parseStandingsForAnalytics(root: JsonObject): List<DixonColesPredictor.TeamStandingInput> {
        val result = mutableListOf<DixonColesPredictor.TeamStandingInput>()
        val children = root["children"]?.jsonArray ?: return result

        for (group in children) {
            val entries = group.jsonObject["standings"]?.jsonObject?.get("entries")?.jsonArray ?: continue
            for (entryElem in entries) {
                val entry = entryElem.jsonObject
                val teamObj = entry["team"]?.jsonObject ?: continue
                val teamId = teamObj["id"]?.jsonPrimitive?.contentOrNull ?: ""
                val teamName = teamObj["shortDisplayName"]?.jsonPrimitive?.contentOrNull
                    ?: teamObj["displayName"]?.jsonPrimitive?.contentOrNull ?: ""

                val statsMap = mutableMapOf<String, Int>()
                entry["stats"]?.jsonArray?.forEach { statElem ->
                    val statObj = statElem.jsonObject
                    val name = statObj["name"]?.jsonPrimitive?.contentOrNull ?: ""
                    val value = statObj["value"]?.jsonPrimitive?.doubleOrNull?.toInt()
                        ?: statObj["displayValue"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                    if (name.isNotEmpty() && value != null) {
                        statsMap[name] = value
                    }
                }

                result.add(
                    DixonColesPredictor.TeamStandingInput(
                        teamId = teamId,
                        teamName = teamName,
                        played = statsMap["gamesPlayed"] ?: 0,
                        wins = statsMap["wins"] ?: 0,
                        draws = statsMap["ties"] ?: 0,
                        losses = statsMap["losses"] ?: 0,
                        goalsFor = statsMap["pointsFor"] ?: 0,
                        goalsAgainst = statsMap["pointsAgainst"] ?: 0,
                        points = statsMap["points"] ?: 0
                    )
                )
            }
        }
        return result
    }
}