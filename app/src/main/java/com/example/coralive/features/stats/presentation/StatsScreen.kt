package com.example.coralive.features.stats.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.coralive.core.model.League
import com.example.coralive.core.ui.*
import com.example.coralive.features.stats.domain.PlayerStatItem
import com.example.coralive.ui.DotMatrixText
import com.example.coralive.ui.PillChip
import com.example.coralive.ui.theme.CoraTheme

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoraTheme.colors.background)
    ) {

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(League.entries) { league ->
                PillChip(
                    text = league.titleAr,
                    selected = league == state.selectedLeague,
                    onClick = { viewModel.selectLeague(league) }
                )
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PillChip(
                text = "الهدافون ⚽",
                selected = state.selectedTabIndex == 0,
                onClick = { viewModel.selectTab(0) },
                modifier = Modifier.weight(1f)
            )
            PillChip(
                text = "صناع الأهداف 🎯",
                selected = state.selectedTabIndex == 1,
                onClick = { viewModel.selectTab(1) },
                modifier = Modifier.weight(1f)
            )
        }

        val activeList = if (state.selectedTabIndex == 0) state.data.topScorers else state.data.topAssists
        val statBadgeLabel = if (state.selectedTabIndex == 0) "GOALS" else "ASSISTS"

        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::refresh)
            activeList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "لا توجد إحصائيات متاحة حالياً",
                        color = CoraTheme.colors.textSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    item(key = "leader_${activeList.first().playerId}_${state.selectedTabIndex}") {
                        TopLeaderHeroCard(
                            player = activeList.first(),
                            statLabel = statBadgeLabel
                        )
                    }


                    itemsIndexed(
                        items = activeList.drop(1),
                        key = { index, item -> "${item.playerId}_${state.selectedTabIndex}_$index" }
                    ) { index, player ->
                        PlayerLeaderRowCard(
                            displayRank = if (player.rank > 0) player.rank else index + 2,
                            player = player,
                            statLabel = statBadgeLabel
                        )
                    }
                }
            }
        }
    }
}

/**
 * بطاقة المتصدر الأول (#01) بتصميم LED Scoreboard
 */
@Composable
private fun TopLeaderHeroCard(
    player: PlayerStatItem,
    statLabel: String
) {
    val rankFormatted = (if (player.rank > 0) player.rank else 1).toString().padStart(2, '0')
    val valueFormatted = player.value.trim().padStart(2, '0')

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CoraTheme.colors.surface,
        border = BorderStroke(1.dp, CoraTheme.colors.homeAccent.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(CoraTheme.colors.homeAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TOP LEADER · #$rankFormatted",
                        color = CoraTheme.colors.homeAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = statLabel,
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(CoraTheme.colors.surfaceElevated)
                            .border(1.dp, CoraTheme.colors.homeAccent.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!player.playerHeadshotUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = player.playerHeadshotUrl,
                                contentDescription = player.playerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = player.playerName.take(1).uppercase(),
                                color = CoraTheme.colors.homeAccent,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = player.playerName,
                            color = CoraTheme.colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!player.teamLogoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = player.teamLogoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = player.teamName.uppercase(),
                                color = CoraTheme.colors.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                DotMatrixText(
                    text = valueFormatted,
                    color = CoraTheme.colors.homeAccent,
                    dotSize = 4.dp,
                    dotSpacing = 1.3.dp,
                    charSpacing = 5.dp
                )
            }
        }
    }
}

/**
 * بطاقة اللاعب في باقي الترتيب (#02 فما بعد)
 */
@Composable
private fun PlayerLeaderRowCard(
    displayRank: Int,
    player: PlayerStatItem,
    statLabel: String
) {
    val rankColor = when (displayRank) {
        2 -> CoraTheme.colors.awayAccent
        3 -> CoraTheme.colors.textPrimary
        else -> CoraTheme.colors.textSecondary
    }
    val valueFormatted = player.value.trim().padStart(2, '0')

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CoraTheme.colors.surface,
        border = CoraTheme.cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayRank.toString().padStart(2, '0'),
                color = rankColor,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp)
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CoraTheme.colors.surfaceElevated)
                    .border(1.dp, CoraTheme.colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!player.playerHeadshotUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = player.playerHeadshotUrl,
                        contentDescription = player.playerName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = player.playerName.take(1).uppercase(),
                        color = CoraTheme.colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.playerName,
                    color = CoraTheme.colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!player.teamLogoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = player.teamLogoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = player.teamName,
                        color = CoraTheme.colors.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                DotMatrixText(
                    text = valueFormatted,
                    color = if (displayRank <= 3) CoraTheme.colors.textPrimary else CoraTheme.colors.textSecondary,
                    dotSize = 2.4.dp,
                    dotSpacing = 0.8.dp,
                    charSpacing = 3.dp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = statLabel,
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}