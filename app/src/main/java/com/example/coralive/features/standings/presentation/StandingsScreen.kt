package com.example.coralive.features.standings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.coralive.core.model.League
import com.example.coralive.core.ui.*
import com.example.coralive.features.standings.domain.StandingRowItem
import com.example.coralive.ui.DotMatrixText
import com.example.coralive.ui.PillChip
import com.example.coralive.ui.theme.CoraTheme

@Composable
fun StandingsScreen(
    onTeamClick: (teamId: String, leagueCode: String) -> Unit,
    viewModel: StandingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoraTheme.colors.background)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DotMatrixText(
                text = "STANDINGS",
                color = CoraTheme.colors.textPrimary,
                dotSize = 2.6.dp,
                dotSpacing = 1.dp,
                charSpacing = 4.dp
            )
        }


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

        Spacer(modifier = Modifier.height(6.dp))

        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::loadStandings)
            else -> {
                TableHeader()
                HorizontalDivider(color = CoraTheme.colors.border)
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.rows, key = { "${it.rank}_${it.teamName}" }) { row ->
                        StandingRow(
                            row = row,
                            onClick = {
                                if (row.teamId.isNotBlank()) {
                                    onTeamClick(row.teamId, state.selectedLeague.code)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            modifier = Modifier.width(28.dp),
            color = CoraTheme.colors.textSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "الفريق",
            modifier = Modifier.weight(1f),
            color = CoraTheme.colors.textSecondary,
            fontSize = 11.sp
        )
        Text(
            text = "لعب",
            modifier = Modifier.width(28.dp),
            color = CoraTheme.colors.textSecondary,
            fontSize = 11.sp
        )
        Text(
            text = "ف",
            modifier = Modifier.width(24.dp),
            color = CoraTheme.colors.textSecondary,
            fontSize = 11.sp
        )
        Text(
            text = "ت",
            modifier = Modifier.width(24.dp),
            color = CoraTheme.colors.textSecondary,
            fontSize = 11.sp
        )
        Text(
            text = "خ",
            modifier = Modifier.width(24.dp),
            color = CoraTheme.colors.textSecondary,
            fontSize = 11.sp
        )
        Text(
            text = "+/-",
            modifier = Modifier.width(34.dp),
            color = CoraTheme.colors.textSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "نقاط",
            modifier = Modifier.width(36.dp),
            color = CoraTheme.colors.homeAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StandingRow(
    row: StandingRowItem,
    onClick: () -> Unit
) {
    val rankColor = when {
        row.rank <= 4 -> CoraTheme.colors.homeAccent
        row.rank >= 18 -> CoraTheme.colors.liveRed
        else -> CoraTheme.colors.textSecondary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = CoraTheme.colors.surface,
        border = CoraTheme.cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = row.rank.toString().padStart(2, '0'),
                modifier = Modifier.width(28.dp),
                color = rankColor,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = row.logoUrl,
                    contentDescription = row.teamName,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = row.teamName,
                    color = CoraTheme.colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = row.played.toString(),
                modifier = Modifier.width(28.dp),
                color = CoraTheme.colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Text(
                text = row.won.toString(),
                modifier = Modifier.width(24.dp),
                color = CoraTheme.colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Text(
                text = row.drawn.toString(),
                modifier = Modifier.width(24.dp),
                color = CoraTheme.colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Text(
                text = row.lost.toString(),
                modifier = Modifier.width(24.dp),
                color = CoraTheme.colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Text(
                text = row.goalDiff.toString(),
                modifier = Modifier.width(34.dp),
                color = CoraTheme.colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Box(
                modifier = Modifier.width(36.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                DotMatrixText(
                    text = row.points.toString().padStart(2, '0'),
                    color = CoraTheme.colors.homeAccent,
                    dotSize = 2.dp,
                    dotSpacing = 0.7.dp,
                    charSpacing = 2.5.dp
                )
            }
        }
    }
}