package com.example.coralive.features.profiles.presentation


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.coralive.core.ui.*
import com.example.coralive.ui.DotMatrixText
import com.example.coralive.ui.PillChip
import com.example.coralive.ui.theme.CoraTheme

@Composable
fun PlayerProfileScreen(
    playerId: String,
    onBack: () -> Unit
) {
    val viewModel: PlayerProfileViewModel = viewModel(
        key = "player_$playerId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerProfileViewModel(playerId) as T
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
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::loadPlayer)
            state.data != null -> {
                val player = state.data!!

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
                        text = "#${player.jersey.padStart(2, '0')}",
                        color = CoraTheme.colors.homeAccent,
                        dotSize = 2.8.dp,
                        dotSpacing = 0.9.dp,
                        charSpacing = 3.5.dp
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }

                 Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = CoraTheme.colors.surface,
                    border = CoraTheme.cardBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(CoraTheme.colors.surfaceElevated)
                                    .border(1.dp, CoraTheme.colors.homeAccent.copy(alpha = 0.4f), CircleShape),
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
                                    Text(player.name.take(1), color = CoraTheme.colors.homeAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(player.name, color = CoraTheme.colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${player.teamName} · ${player.position}", color = CoraTheme.colors.homeAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = CoraTheme.colors.border)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PlayerBioChip("الجنسية", player.nationality)
                            PlayerBioChip("العمر", player.age)
                            PlayerBioChip("الطول", player.height)
                        }
                    }
                }

                 Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillChip(
                        text = "إحصائيات اللاعب",
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        modifier = Modifier.weight(1f)
                    )
                    PillChip(
                        text = "المسيرة والتاريخ",
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.selectedTab == 0) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(player.seasonStats) { stat ->
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
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(player.careerHistory) { entry ->
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
                                    Column {
                                        Text(entry.clubName, color = CoraTheme.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(entry.seasonOrPeriod, color = CoraTheme.colors.textSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text(
                                        text = "${entry.appearances} مباراة · ${entry.goals} هدف",
                                        color = CoraTheme.colors.homeAccent,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerBioChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = CoraTheme.colors.textSecondary, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = CoraTheme.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}