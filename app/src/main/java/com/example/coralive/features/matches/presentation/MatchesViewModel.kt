package com.example.coralive.features.matches.presentation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coralive.core.model.League
import com.example.coralive.core.network.NetworkModule
import com.example.coralive.features.matches.data.MatchesRepository
import com.example.coralive.features.matches.domain.MatchItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class LeagueMatchesSection(
    val league: League,
    val matches: List<MatchItem>
)

data class MatchesUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val dateTabs: List<LocalDate> = (-3..7).map { LocalDate.now().plusDays(it.toLong()) },
    val leagueSections: List<LeagueMatchesSection> = emptyList(),
    val notifiedMatchIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MatchesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MatchesRepository(NetworkModule.apiService)
    private val prefs = application.getSharedPreferences("premier_live_prefs", Context.MODE_PRIVATE)
    private val apiDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    private val _uiState = MutableStateFlow(
        MatchesUiState(
            notifiedMatchIds = prefs.getStringSet("favorite_matches", emptySet()) ?: emptySet()
        )
    )
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        loadAllLeaguesForSelectedDate()
    }

    fun selectDate(date: LocalDate) {
        if (date == _uiState.value.selectedDate) return
        _uiState.update { it.copy(selectedDate = date) }
        loadAllLeaguesForSelectedDate()
    }

    fun toggleMatchNotification(match: MatchItem) {
        val currentSet = _uiState.value.notifiedMatchIds.toMutableSet()
        val isNowEnabled = if (currentSet.contains(match.id)) {
            currentSet.remove(match.id)
            false
        } else {
            currentSet.add(match.id)
            true
        }
        prefs.edit().putStringSet("favorite_matches", currentSet).apply()

         val notifMatchesTeams = (prefs.getStringSet("match_alert_team_ids", emptySet()) ?: emptySet()).toMutableSet()
        if (isNowEnabled) {
            if (match.homeTeam.id.isNotBlank()) notifMatchesTeams.add("${match.id}:${match.homeTeam.id}")
            if (match.awayTeam.id.isNotBlank()) notifMatchesTeams.add("${match.id}:${match.awayTeam.id}")
        } else {
            notifMatchesTeams.removeAll { it.startsWith("${match.id}:") }
        }
        prefs.edit().putStringSet("match_alert_team_ids", notifMatchesTeams).apply()

        _uiState.update { it.copy(notifiedMatchIds = currentSet) }
    }

    fun loadAllLeaguesForSelectedDate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val dateStr = _uiState.value.selectedDate.format(apiDateFormatter)
            try {
                var failedLeagues = 0
                val results = coroutineScope {
                    League.entries.map { league ->
                        async {
                            try {
                                val matches = repository.getMatchesByDate(league.code, dateStr)
                                LeagueMatchesSection(league = league, matches = matches)
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }.awaitAll()
                }
                failedLeagues = results.count { it == null }
                 if (failedLeagues == results.size) throw java.io.IOException("all leagues failed")
                val sections = results.filterNotNull().filter { it.matches.isNotEmpty() }

                _uiState.update {
                    it.copy(
                        leagueSections = sections,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "تعذر تحميل مباريات اليوم، تحقق من الاتصال"
                    )
                }
            }
        }
    }
}