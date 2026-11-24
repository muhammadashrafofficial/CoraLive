package com.example.coralive.features.matches.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.coralive.core.ui.ErrorState
import com.example.coralive.core.ui.LoadingState
import com.example.coralive.features.matches.domain.MatchItem
import com.example.coralive.ui.DotMatrixText
import com.example.coralive.ui.PillChip
import com.example.coralive.ui.theme.CoraTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MatchesScreen(
    onMatchClick: (matchId: String, leagueCode: String) -> Unit,
    viewModel: MatchesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val today = remember { LocalDate.now() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoraTheme.colors.background)
            .statusBarsPadding()
    ) {
         Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DotMatrixText(
                text = "MATCHDAY",
                color = CoraTheme.colors.textPrimary,
                dotSize = 2.6.dp,
                dotSpacing = 1.dp,
                charSpacing = 4.dp
            )

            Text(
                text = state.selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)).uppercase(),
                color = CoraTheme.colors.textSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

         LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.dateTabs) { date ->
                val label = when (date) {
                    today -> "اليوم"
                    today.minusDays(1) -> "أمس"
                    today.plusDays(1) -> "غداً"
                    else -> date.format(DateTimeFormatter.ofPattern("EEE d/M", Locale("ar")))
                }
                PillChip(
                    text = label,
                    selected = date == state.selectedDate,
                    onClick = { viewModel.selectDate(date) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::loadAllLeaguesForSelectedDate)
            state.leagueSections.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "لا توجد مباريات مجدولة في هذا اليوم",
                        color = CoraTheme.colors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                 LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.leagueSections.forEach { section ->
                         item(key = "header_${section.league.code}") {
                            LeagueSectionHeader(
                                leagueTitle = section.league.titleAr,
                                matchCount = section.matches.size
                            )
                        }

                         items(
                            items = section.matches,
                            key = { "${section.league.code}_${it.id}" }
                        ) { match ->
                            val isAlertEnabled = state.notifiedMatchIds.contains(match.id)
                            MatchDayCard(
                                match = match,
                                isAlertEnabled = isAlertEnabled,
                                onToggleAlert = { viewModel.toggleMatchNotification(match) },
                                onClick = { onMatchClick(match.id, match.leagueCode) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueSectionHeader(
    leagueTitle: String,
    matchCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(10.dp),
        color = CoraTheme.colors.surfaceElevated,
        border = CoraTheme.cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CoraTheme.colors.homeAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = leagueTitle,
                    color = CoraTheme.colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "$matchCount MATCHES",
                color = CoraTheme.colors.textSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun MatchDayCard(
    match: MatchItem,
    isAlertEnabled: Boolean,
    onToggleAlert: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CoraTheme.colors.surface,
        border = if (match.isLive) {
            BorderStroke(1.dp, CoraTheme.colors.liveRed.copy(alpha = 0.5f))
        } else {
            CoraTheme.cardBorder
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.isLive) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CoraTheme.colors.liveRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = match.statusDetail,
                        color = if (match.isLive) CoraTheme.colors.liveRed else CoraTheme.colors.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (match.isLive) FontWeight.Bold else FontWeight.Normal
                    )
                }

                 Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onToggleAlert),
                    shape = CircleShape,
                    color = if (isAlertEnabled) {
                        CoraTheme.colors.homeAccent.copy(alpha = 0.16f)
                    } else {
                        CoraTheme.colors.surfaceElevated
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isAlertEnabled) CoraTheme.colors.homeAccent else CoraTheme.colors.border
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAlertEnabled) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                            contentDescription = "تنبيه المباراة",
                            tint = if (isAlertEnabled) CoraTheme.colors.homeAccent else CoraTheme.colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        if (isAlertEnabled) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "مُنبّه",
                                color = CoraTheme.colors.homeAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

             Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = match.homeTeam.logoUrl,
                        contentDescription = match.homeTeam.shortName,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam.shortName,
                        color = CoraTheme.colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                 Box(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.statusState == "pre") {
                        Text(
                            text = "VS",
                            color = CoraTheme.colors.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        DotMatrixText(
                            text = "${match.homeTeam.score}:${match.awayTeam.score}",
                            color = if (match.isLive) CoraTheme.colors.homeAccent else CoraTheme.colors.textPrimary,
                            dotSize = 2.4.dp,
                            dotSpacing = 0.8.dp,
                            charSpacing = 3.dp
                        )
                    }
                }

                 Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.awayTeam.shortName,
                        color = CoraTheme.colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AsyncImage(
                        model = match.awayTeam.logoUrl,
                        contentDescription = match.awayTeam.shortName,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}