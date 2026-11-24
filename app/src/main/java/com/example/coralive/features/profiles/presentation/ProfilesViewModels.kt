package com.example.coralive.features.profiles.presentation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coralive.core.network.NetworkModule
import com.example.coralive.features.profiles.data.ProfilesRepository
import com.example.coralive.features.profiles.domain.PlayerProfileData
import com.example.coralive.features.profiles.domain.TeamProfileData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamProfileUiState(
    val data: TeamProfileData? = null,
    val selectedTab: Int = 0, // 0: المباريات, 1: السكواد, 2: الإحصائيات والهدافين, 3: التاريخ والبطولات
    val isLoading: Boolean = true,
    val error: String? = null
)

class TeamProfileViewModel(
    application: Application,
    private val leagueCode: String,
    private val teamId: String
) : AndroidViewModel(application) {

    private val repository = ProfilesRepository(NetworkModule.apiService)
    private val prefs = application.getSharedPreferences("coralive_team_alerts", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(TeamProfileUiState())
    val uiState: StateFlow<TeamProfileUiState> = _uiState.asStateFlow()

    init {
        loadTeam()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleTeamNotifications() {
        val currentData = _uiState.value.data ?: return
        val newState = !currentData.isNotificationsEnabled
        prefs.edit().putBoolean("notify_team_$teamId", newState).apply()

        // ربط النادي بقائمة الفرق المفضلة لإرسال إشعارات الأهداف الفورية
        val mainPrefs = getApplication<Application>().getSharedPreferences("premier_live_prefs", Context.MODE_PRIVATE)
        val favs = (mainPrefs.getStringSet("favorite_teams", emptySet()) ?: emptySet()).toMutableSet()
        if (newState) favs.add(teamId) else favs.remove(teamId)
        mainPrefs.edit().putStringSet("favorite_teams", favs).apply()

        _uiState.update {
            it.copy(data = currentData.copy(isNotificationsEnabled = newState))
        }
    }

    fun loadTeam() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val isNotif = prefs.getBoolean("notify_team_$teamId", false)
                val profile = repository.getTeamProfile(leagueCode, teamId, isNotif)
                _uiState.update { it.copy(data = profile, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "تعذر تحميل بيانات النادي") }
            }
        }
    }
}

data class PlayerProfileUiState(
    val data: PlayerProfileData? = null,
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class PlayerProfileViewModel(
    private val playerId: String
) : ViewModel() {

    private val repository = ProfilesRepository(NetworkModule.apiService)
    private val _uiState = MutableStateFlow(PlayerProfileUiState())
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    init {
        loadPlayer()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadPlayer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = repository.getPlayerProfile(playerId)
                _uiState.update { it.copy(data = profile, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "تعذر تحميل بروفايل اللاعب") }
            }
        }
    }
}