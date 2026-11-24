package com.example.coralive.features.match_details.presentation

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coralive.core.ui.*
import com.example.coralive.features.analytics.presentation.MatchAnalyticsSection
import com.example.coralive.features.match_details.domain.MatchDetails
import com.example.coralive.features.match_details.domain.MatchEventItem
import com.example.coralive.features.match_details.domain.MatchEventType
import com.example.coralive.features.match_details.domain.PlayerLineupItem
import com.example.coralive.features.match_details.domain.StatComparisonItem
import com.example.coralive.ui.DotMatrixMomentumCard
import com.example.coralive.ui.DotMatrixText
import com.example.coralive.ui.MomentumGoalMarker
import com.example.coralive.ui.PillChip
import com.example.coralive.ui.theme.CoraTheme
import kotlin.math.sin

@Composable
fun MatchDetailsScreen(
    leagueCode: String,
    matchId: String,
    onBack: () -> Unit,
    onTeamClick: (teamId: String) -> Unit
) {
    val viewModel: MatchDetailsViewModel = viewModel(
        key = "${leagueCode}_$matchId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MatchDetailsViewModel(leagueCode, matchId) as T
            }
        }
    )

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoraTheme.colors.background)
            .statusBarsPadding()
    ) {
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::retry)
            state.details != null -> {
                val details = state.details!!

                // 1. الشريط العلوي (زر الرجوع الدائري + وقت المباراة بالنقاط + زر المشاركة الدائري)
                TopMatchClockBar(
                    statusText = details.statusText,
                    isLive = details.isLive,
                    onBack = onBack,
                    onShare = {
                        val shareText = "${details.homeTeam.shortName} ${details.homeTeam.score}:${details.awayTeam.score} ${details.awayTeam.shortName} (${details.statusText})"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "مشاركة النتيجة"))
                    }
                )

                // 2. لوحة النتيجة الرئيسية بدون إطار مع أسماء مسجلي الأهداف
                DotMatrixScoreboardHeader(
                    details = details,
                    onTeamClick = onTeamClick)

                // 3. شريط التبويبات الكبسولي (Live, Stats, Lineups, Recap)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("Live", "Stats", "Lineup", "Recap")
                    tabs.forEachIndexed { idx, title ->
                        PillChip(
                            text = title,
                            selected = state.selectedTab == idx,
                            onClick = { viewModel.selectTab(idx) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. محتوى كل تبويب
                when (state.selectedTab) {
                    0 -> LiveMomentumAndEventsTab(details = details, state = state)
                    1 -> DotMatrixStatsTab(details = details)
                    2 -> DotMatrixLineupsTab(
                        homeAbbr = details.homeTeam.abbreviation.ifBlank { details.homeTeam.shortName.take(3) }.uppercase(),
                        awayAbbr = details.awayTeam.abbreviation.ifBlank { details.awayTeam.shortName.take(3) }.uppercase(),
                        homeName = details.homeTeam.shortName,
                        awayName = details.awayTeam.shortName,
                        homeLineup = details.homeLineup,
                        awayLineup = details.awayLineup
                    )
                    3 -> RecapAnalyticsTab(state = state)
                }
            }
        }
    }
}

/**
 * الشريط العلوي: زر رجوع دائري + ساعة المباراة بنقاط LED + زر مشاركة دائري
 */
@Composable
private fun TopMatchClockBar(
    statusText: String,
    isLive: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    val cleanClock = statusText
        .replace("LIVE", "", ignoreCase = true)
        .replace("'", "")
        .trim()
        .ifBlank { "00:00" }

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

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(CoraTheme.colors.liveRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            DotMatrixText(
                text = cleanClock,
                color = if (isLive) CoraTheme.colors.liveRed else CoraTheme.colors.textSecondary,
                dotSize = 2.4.dp,
                dotSpacing = 0.8.dp,
                charSpacing = 3.dp
            )
        }

        IconButton(
            onClick = onShare,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CoraTheme.colors.surface)
                .border(1.dp, CoraTheme.colors.border, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "مشاركة",
                tint = CoraTheme.colors.textPrimary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/**
 * لوحة النتيجة الرئيسية العريضة: اختصار الفريق + الاسم الكامل + مسجلي الأهداف تحت كل فريق
 */
@Composable
private fun DotMatrixScoreboardHeader(
    details: MatchDetails,
    onTeamClick: (String) -> Unit) {
    val homeAbbr = details.homeTeam.abbreviation.ifBlank { details.homeTeam.shortName.take(3) }.uppercase()
    val awayAbbr = details.awayTeam.abbreviation.ifBlank { details.awayTeam.shortName.take(3) }.uppercase()

    // استخراج ملخص الأهداف تحت كل فريق (مثلاً: Okafor 23' 61')
    val homeGoalsSummary = remember(details.events) {
        details.events
            .filter { it.type == MatchEventType.GOAL && it.isHomeTeam }
            .joinToString("  ") { ev ->
                val scorer = extractScorerShortName(ev.description)
                "$scorer ${ev.minuteDisplay}"
            }
    }

    val awayGoalsSummary = remember(details.events) {
        details.events
            .filter { it.type == MatchEventType.GOAL && !it.isHomeTeam }
            .joinToString("  ") { ev ->
                val scorer = extractScorerShortName(ev.description)
                "$scorer ${ev.minuteDisplay}"
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // الفريق المضيف (يسار)
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = details.homeTeam.id.isNotBlank()) {
                    onTeamClick(details.homeTeam.id)
                }
        ) {
            DotMatrixText(
                text = homeAbbr,
                color = CoraTheme.colors.homeAccent,
                dotSize = 2.8.dp,
                dotSpacing = 0.9.dp,
                charSpacing = 3.5.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = details.homeTeam.shortName,
                color = CoraTheme.colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (homeGoalsSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = homeGoalsSummary,
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // النتيجة في المنتصف بنقاط LED كبيرة
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            DotMatrixText(
                text = "${details.homeTeam.score}:${details.awayTeam.score}",
                color = CoraTheme.colors.textPrimary,
                dotSize = 4.6.dp,
                dotSpacing = 1.5.dp,
                charSpacing = 6.dp
            )
        }

        // الفريق الضيف (يمين)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = details.homeTeam.id.isNotBlank()) {
                    onTeamClick(details.homeTeam.id)
                }
        ) {
            DotMatrixText(
                text = awayAbbr,
                color = CoraTheme.colors.awayAccent,
                dotSize = 2.8.dp,
                dotSpacing = 0.9.dp,
                charSpacing = 3.5.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = details.awayTeam.shortName,
                color = CoraTheme.colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (awayGoalsSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = awayGoalsSummary,
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * تبويب Live: يجمع مخطط الزخم MOMENTUM + بطاقة AI WHY IT MATTERS + بطاقات الأحداث الزمنية
 */
@Composable
private fun LiveMomentumAndEventsTab(
    details: MatchDetails,
    state: MatchDetailsUiState
) {
    val homeAbbr = details.homeTeam.abbreviation.ifBlank { details.homeTeam.shortName.take(3) }.uppercase()
    val awayAbbr = details.awayTeam.abbreviation.ifBlank { details.awayTeam.shortName.take(3) }.uppercase()

    // تحويل الأهداف إلى علامات على مخطط الزخم
    val goalMarkers = remember(details.events) {
        details.events
            .filter { it.type == MatchEventType.GOAL }
            .map { ev ->
                val min = ev.minuteDisplay.filter { it.isDigit() }.take(2).toIntOrNull() ?: 45
                MomentumGoalMarker(
                    minute = min,
                    isHome = ev.isHomeTeam,
                    label = "GOAL ${ev.minuteDisplay}"
                )
            }
    }

    // توليد منحنى الزخم بناءً على الإحصائيات والأحداث الفعلية للمباراة
    val momentumSeries = remember(details) {
        buildMomentumSeries(details)
    }

    val currentMinute = remember(details.statusText, details.isLive) {
        val parsed = details.statusText.filter { it.isDigit() }.take(2).toIntOrNull()
        when {
            parsed != null && parsed > 0 -> parsed.coerceIn(1, 90)
            details.isLive -> 67
            else -> 90
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. بطاقة الرسم البياني النقطي للزخم MOMENTUM
        item(key = "momentum_card") {
            DotMatrixMomentumCard(
                homeAbbr = homeAbbr,
                awayAbbr = awayAbbr,
                momentumValues = momentumSeries,
                goalMarkers = goalMarkers,
                currentMinute = currentMinute
            )
        }

        // 2. بطاقة التحليل الذكي AI · WHY IT MATTERS
        item(key = "ai_insight_card") {
            AiTacticalInsightCard(
                details = details,
                state = state,
                currentMinute = currentMinute
            )
        }

        // 3. بطاقات الأحداث المتسلسلة أسفل الشاشة (61' Okafor 2:1)
        items(details.events.reversed(), key = { it.id }) { event ->
            DotMatrixEventRowCard(
                event = event,
                scoreSnapshot = "${details.homeTeam.score}:${details.awayTeam.score}"
            )
        }
    }
}

/**
 * بطاقة AI · WHY IT MATTERS أسفل مخطط الزخم
 */
@Composable
private fun AiTacticalInsightCard(
    details: MatchDetails,
    state: MatchDetailsUiState,
    currentMinute: Int
) {
    val insightText = remember(details, state.analyticsReport) {
        val lastGoal = details.events.lastOrNull { it.type == MatchEventType.GOAL }
        val homePoss = details.stats.firstOrNull {
            it.label.contains("الاستحواذ") || it.label.contains("possession", ignoreCase = true)
        }?.homeValue

        when {
            lastGoal != null -> {
                val team = if (lastGoal.isHomeTeam) details.homeTeam.shortName else details.awayTeam.shortName
                "$team shifted momentum after scoring at ${lastGoal.minuteDisplay}, creating high pressing pressure in the final third."
            }
            homePoss != null -> {
                "${details.homeTeam.shortName} holds $homePoss possession against ${details.awayTeam.shortName} as both sides contest midfield control."
            }
            else -> {
                "${details.homeTeam.shortName} and ${details.awayTeam.shortName} are contesting midfield control with compact defensive lines."
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CoraTheme.colors.surface,
        border = CoraTheme.cardBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CoraTheme.colors.surfaceElevated,
                    border = BorderStroke(1.dp, CoraTheme.colors.border)
                ) {
                    Text(
                        text = "AI",
                        color = CoraTheme.colors.textPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WHY IT MATTERS · ${currentMinute}'",
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = insightText,
                color = CoraTheme.colors.textPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * صف الحدث الزمني بنفس تنسيق البطاقة السفلية في الصورة:
 * الدقيقة يساراً + نقطة ملونة حسب الفريق + اسم اللاعب والتفاصيل + النتيجة بنقاط LED يميناً
 */
@Composable
private fun DotMatrixEventRowCard(
    event: MatchEventItem,
    scoreSnapshot: String
) {
    val teamDotColor = if (event.isHomeTeam) CoraTheme.colors.homeAccent else CoraTheme.colors.awayAccent
    val eventBadge = when (event.type) {
        MatchEventType.GOAL -> "GOAL"
        MatchEventType.YELLOW_CARD -> "YC"
        MatchEventType.RED_CARD -> "RC"
        MatchEventType.SUBSTITUTION -> "SUB"
        MatchEventType.OTHER -> "INFO"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CoraTheme.colors.surface,
        border = CoraTheme.cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.minuteDisplay.ifBlank { "-" },
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(42.dp)
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(teamDotColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = extractScorerShortName(event.description).ifBlank { event.title },
                        color = CoraTheme.colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.description.ifBlank { event.title },
                        color = CoraTheme.colors.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (event.type == MatchEventType.GOAL) {
                DotMatrixText(
                    text = scoreSnapshot,
                    color = CoraTheme.colors.textPrimary,
                    dotSize = 2.4.dp,
                    dotSpacing = 0.8.dp,
                    charSpacing = 3.dp
                )
            } else {
                Text(
                    text = eventBadge,
                    color = when (event.type) {
                        MatchEventType.YELLOW_CARD -> Color(0xFFFACC15)
                        MatchEventType.RED_CARD -> CoraTheme.colors.liveRed
                        else -> CoraTheme.colors.textSecondary
                    },
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * تبويب Stats: مقارنة الإحصائيات بأشرطة LED منقطة ثنائية اللون (سماوي vs وردي)
 */
@Composable
private fun DotMatrixStatsTab(details: MatchDetails) {
    if (details.stats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("الإحصائيات غير متوفرة حالياً", color = CoraTheme.colors.textSecondary)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(details.stats) { item ->
            StatComparisonDotCard(item)
        }
    }
}

@Composable
private fun StatComparisonDotCard(item: StatComparisonItem) {
    val homeNum = item.homeValue.replace("%", "").trim().toFloatOrNull() ?: 50f
    val awayNum = item.awayValue.replace("%", "").trim().toFloatOrNull() ?: 50f
    val total = (homeNum + awayNum).takeIf { it > 0f } ?: 1f
    val homeRatio = (homeNum / total).coerceIn(0.05f, 0.95f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CoraTheme.colors.surface,
        border = CoraTheme.cardBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.homeValue,
                    color = CoraTheme.colors.homeAccent,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.label,
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.awayValue,
                    color = CoraTheme.colors.awayAccent,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // شريط النقاط الثنائي
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val totalDots = 26
                val homeDots = (homeRatio * totalDots).toInt()
                repeat(totalDots) { index ->
                    val dotColor = if (index < homeDots) CoraTheme.colors.homeAccent else CoraTheme.colors.awayAccent
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

/**
 * تبويب Lineups: عرض التشكيلة الأساسية لكل فريق بتصميم بطاقات داكنة
 */
@Composable
private fun DotMatrixLineupsTab(
    homeAbbr: String,
    awayAbbr: String,
    homeName: String,
    awayName: String,
    homeLineup: List<PlayerLineupItem>,
    awayLineup: List<PlayerLineupItem>
) {
    if (homeLineup.isEmpty() && awayLineup.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لم يتم إعلان التشكيلة بعد", color = CoraTheme.colors.textSecondary)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            TeamLineupSectionCard(
                abbr = homeAbbr,
                teamName = homeName,
                accentColor = CoraTheme.colors.homeAccent,
                players = homeLineup.filter { it.isStarter }.ifEmpty { homeLineup }
            )
        }

        item {
            TeamLineupSectionCard(
                abbr = awayAbbr,
                teamName = awayName,
                accentColor = CoraTheme.colors.awayAccent,
                players = awayLineup.filter { it.isStarter }.ifEmpty { awayLineup }
            )
        }
    }
}

@Composable
private fun TeamLineupSectionCard(
    abbr: String,
    teamName: String,
    accentColor: Color,
    players: List<PlayerLineupItem>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CoraTheme.colors.surface,
        border = CoraTheme.cardBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DotMatrixText(
                    text = abbr,
                    color = accentColor,
                    dotSize = 2.2.dp,
                    dotSpacing = 0.8.dp,
                    charSpacing = 3.dp
                )
                Text(
                    text = teamName,
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            players.forEach { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.jersey.padStart(2, '0'),
                            color = accentColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(30.dp)
                        )
                        Text(
                            text = player.name,
                            color = CoraTheme.colors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = player.position,
                        color = CoraTheme.colors.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * تبويب Recap: التحليلات المتقدمة Dixon-Coles وتقرير الأداء
 */
@Composable
private fun RecapAnalyticsTab(state: MatchDetailsUiState) {
    val report = state.analyticsReport
    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("جاري حساب التحليلات الإحصائية...", color = CoraTheme.colors.textSecondary)
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        MatchAnalyticsSection(report = report)
    }
}

/**
 * استخراج اسم اللاعب المختصر من وصف الحدث
 */
private fun extractScorerShortName(description: String): String {
    if (description.isBlank()) return ""
    val beforeParen = description.substringBefore("(").substringBefore("-").trim()
    val parts = beforeParen.split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> parts.last()
        parts.size == 1 -> parts.first()
        else -> beforeParen.take(14)
    }
}

/**
 * بناء مصفوفة الزخم (30 عموداً) من أحداث وإحصائيات المباراة الفعلية
 */
private fun buildMomentumSeries(details: MatchDetails): List<Float> {
    val cols = 30
    val homePossession = details.stats
        .firstOrNull { it.label.contains("الاستحواذ") || it.label.contains("possession", true) }
        ?.homeValue?.replace("%", "")?.trim()?.toFloatOrNull() ?: 50f

    val baseBias = ((homePossession - 50f) / 50f).coerceIn(-0.4f, 0.4f)
    val series = MutableList(cols) { idx ->
        val wave = (sin(idx * 0.55) * 0.45f).toFloat()
        (baseBias + wave).coerceIn(-0.85f, 0.85f)
    }

    // تعزيز ذروة الزخم عند دقائق الأهداف والبطاقات
    details.events.forEach { ev ->
        val minute = ev.minuteDisplay.filter { it.isDigit() }.take(2).toIntOrNull() ?: return@forEach
        val col = ((minute.coerceIn(0, 90) / 90f) * (cols - 1)).toInt().coerceIn(0, cols - 1)
        val spike = when (ev.type) {
            MatchEventType.GOAL -> if (ev.isHomeTeam) 1.0f else -1.0f
            MatchEventType.RED_CARD -> if (ev.isHomeTeam) -0.8f else 0.8f
            else -> if (ev.isHomeTeam) 0.5f else -0.5f
        }
        series[col] = spike
        if (col > 0) series[col - 1] = (series[col - 1] + spike * 0.5f).coerceIn(-1f, 1f)
    }

    return series
}