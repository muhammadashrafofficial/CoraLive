package com.example.coralive.features.standings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coralive.core.model.League
import com.example.coralive.core.network.NetworkModule
import com.example.coralive.features.standings.data.StandingsRepository
import com.example.coralive.features.standings.domain.StandingRowItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StandingsUiState(
    val selectedLeague: League = League.PREMIER_LEAGUE,
    val rows: List<StandingRowItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class StandingsViewModel(
    private val repository: StandingsRepository = StandingsRepository(NetworkModule.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow(StandingsUiState())
    val uiState: StateFlow<StandingsUiState> = _uiState.asStateFlow()

    init {
        loadStandings()
    }

    fun selectLeague(league: League) {
        _uiState.update { it.copy(selectedLeague = league) }
        loadStandings()
    }

    fun loadStandings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getStandings(_uiState.value.selectedLeague.code)
                _uiState.update { it.copy(rows = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "تعذر تحميل جدول الترتيب") }
            }
        }
    }
}
