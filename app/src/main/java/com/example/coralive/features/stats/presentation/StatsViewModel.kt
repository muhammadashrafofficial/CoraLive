package com.example.coralive.features.stats.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coralive.core.model.League
import com.example.coralive.core.network.NetworkModule
import com.example.coralive.features.stats.data.StatsRepository
import com.example.coralive.features.stats.domain.LeagueLeadersData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val selectedLeague: League = League.PREMIER_LEAGUE,
    val selectedTabIndex: Int = 0,
    val data: LeagueLeadersData = LeagueLeadersData(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class StatsViewModel(
    private val repository: StatsRepository = StatsRepository(NetworkModule.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun selectLeague(league: League) {
        _uiState.update { it.copy(selectedLeague = league, isLoading = true, error = null) }
        loadStats()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun refresh() {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val leaders = repository.getLeagueLeaders(_uiState.value.selectedLeague.code)
                _uiState.update {
                    it.copy(
                        data = leaders,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "تعذر تحميل إحصائيات اللاعبين حالياً."
                    )
                }
            }
        }
    }
}