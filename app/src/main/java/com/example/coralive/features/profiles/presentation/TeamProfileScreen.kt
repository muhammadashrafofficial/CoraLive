package com.example.coralive.features.profiles.presentation


import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.coralive.core.ui.*
import com.example.coralive.features.matches.domain.MatchItem
import com.example.coralive.features.profiles.domain.SquadPlayerItem
import com.example.coralive.features.profiles.domain.TeamLeaderHighlight
import com.example.coralive.features.profiles.domain.TeamProfileData
import com.example.coralive.ui.DotMatrixText
import com.example.coralive.ui.PillChip
import com.example.coralive.ui.theme.CoraTheme

@Composable
fun TeamProfileScreen(
    leagueCode: String,
    teamId: String,
    onBack: () -> Unit,
    onMatchClick: (matchId: String, leagueCode: String) -> Unit,
    onPlayerClick: (playerId: String) -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: TeamProfileViewModel = viewModel(
        key = "team_${leagueCode}_$teamId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TeamProfileViewModel(app, leagueCode, teamId) as T
            }
        }
    )

    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoraTheme.colors.background)
            .statusBarsPadding()
    ) {
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::loadTeam)
            state.data != null -> {
                val team = state.data!!

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CoraTheme.colors.surface)
                            .border(1.dp, CoraTheme.colors.border, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = CoraTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DotMatrixText(
                        text = team.abbreviation,
                        color = CoraTheme.colors.homeAccent,
                        dotSize = 2.6.dp,
                        dotSpacing = 0.9.dp,
                        charSpacing = 3.5.dp
                    )

                    Surface(
                        modifier = Modifier.clickable { viewModel.toggleTeamNotifications() },
                        shape = CircleShape,
                        color = if (team.isNotificationsEnabled) CoraTheme.colors.homeAccent.copy(alpha = 0.15f)
                        else CoraTheme.colors.surface,
                        border = BorderStroke(
                            1.dp,
                            if (team.isNotificationsEnabled) CoraTheme.colors.homeAccent else CoraTheme.colors.border
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (team.isNotificationsEnabled) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                                contentDescription = "تنبيهات النادي",
                                tint = if (team.isNotificationsEnabled) CoraTheme.colors.homeAccent else CoraTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (team.isNotificationsEnabled) "مُفعّل" else "تنبيهات",
                                color = if (team.isNotificationsEnabled) CoraTheme.colors.homeAccent else CoraTheme.colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = CoraTheme.colors.surface,
                    border = CoraTheme.cardBorder
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!team.logoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = team.logoUrl,
                                contentDescription = team.name,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = team.name,
                                color = CoraTheme.colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${team.venueName} · ${team.standingSummary}",
                                color = CoraTheme.colors.textSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("المباريات", "السكواد", "الإحصائيات والهدافين", "التاريخ والبطولات")
                    items(tabs.size) { index ->
                        PillChip(
                            text = tabs[index],
                            selected = state.selectedTab == index,
                            onClick = { viewModel.selectTab(index) }
                        )
                    }
                }

                when (state.selectedTab) {
                    0 -> TeamMatchesSection(team = team, onMatchClick = onMatchClick)
                    1 -> TeamSquadSection(squad = team.squad, onPlayerClick = onPlayerClick)
                    2 -> TeamStatsAndLeadersSection(team = team, onPlayerClick = onPlayerClick)
                    3 -> TeamHistorySection(team = team)
                }
            }
        }
    }
}

@Composable
private fun TeamMatchesSection(
    team: TeamProfileData,
    onMatchClick: (String, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (team.upcomingMatches.isNotEmpty()) {
            item {
                Text(
                    text = "UPCOMING MATCHES · المباريات القادمة",
                    color = CoraTheme.colors.homeAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            items(team.upcomingMatches) { match ->
                CompactTeamMatchCard(match = match, onClick = { onMatchClick(match.id, match.leagueCode) })
            }
        }

        if (team.pastMatches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PREVIOUS RESULTS · المباريات السابقة",
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            items(team.pastMatches) { match ->
                CompactTeamMatchCard(match = match, onClick = { onMatchClick(match.id, match.leagueCode) })
            }
        }
    }
}

@Composable
private fun CompactTeamMatchCard(match: MatchItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CoraTheme.colors.surface,
        border = CoraTheme.cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${match.homeTeam.shortName} vs ${match.awayTeam.shortName}",
                    color = CoraTheme.colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = match.statusDetail,
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (match.statusState != "pre") {
                DotMatrixText(
                    text = "${match.homeTeam.score}:${match.awayTeam.score}",
                    color = CoraTheme.colors.textPrimary,
                    dotSize = 2.4.dp,
                    dotSpacing = 0.8.dp,
                    charSpacing = 3.dp
                )
            }
        }
    }
}

@Composable
private fun TeamSquadSection(
    squad: List<SquadPlayerItem>,
    onPlayerClick: (String) -> Unit
) {
    if (squad.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا تتوفر بيانات السكواد حالياً", color = CoraTheme.colors.textSecondary)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(squad, key = { it.id }) { player ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayerClick(player.id) },
                shape = RoundedCornerShape(14.dp),
                color = CoraTheme.colors.surface,
                border = CoraTheme.cardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(
                            text = player.jersey.padStart(2, '0'),
                            color = CoraTheme.colors.homeAccent,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(30.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CoraTheme.colors.surfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!player.headshotUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = player.headshotUrl,
                                    contentDescription = player.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(player.name.take(1), color = CoraTheme.colors.textSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(player.name, color = CoraTheme.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("${player.position} · ${player.nationality}", color = CoraTheme.colors.textSecondary, fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = "PROFILE →",
                        color = CoraTheme.colors.textSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamStatsAndLeadersSection(
    team: TeamProfileData,
    onPlayerClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (team.topScorer != null || team.topAssister != null) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    team.topScorer?.let { scorer ->
                        LeaderMiniCard(
                            highlight = scorer,
                            accentColor = CoraTheme.colors.homeAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { onPlayerClick(scorer.playerId) }
                        )
                    }
                    team.topAssister?.let { assister ->
                        LeaderMiniCard(
                            highlight = assister,
                            accentColor = CoraTheme.colors.awayAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { onPlayerClick(assister.playerId) }
                        )
                    }
                }
            }
        }

        items(team.teamStats) { stat ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CoraTheme.colors.surface,
                border = CoraTheme.cardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stat.label, color = CoraTheme.colors.textSecondary, fontSize = 13.sp)
                    DotMatrixText(
                        text = stat.value,
                        color = CoraTheme.colors.textPrimary,
                        dotSize = 2.4.dp,
                        dotSpacing = 0.8.dp,
                        charSpacing = 3.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderMiniCard(
    highlight: TeamLeaderHighlight,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CoraTheme.colors.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = highlight.label,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = highlight.playerName,
                color = CoraTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            DotMatrixText(
                text = highlight.value.padStart(2, '0'),
                color = accentColor,
                dotSize = 3.2.dp,
                dotSpacing = 1.dp,
                charSpacing = 4.dp
            )
        }
    }
}

@Composable
private fun TeamHistorySection(team: TeamProfileData) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(team.historyAndHonors) { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CoraTheme.colors.surface,
                border = CoraTheme.cardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.label, color = CoraTheme.colors.textSecondary, fontSize = 13.sp)
                    Text(item.value, color = CoraTheme.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}