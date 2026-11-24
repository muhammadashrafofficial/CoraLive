package com.example.coralive.features.analytics.presentation


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coralive.features.analytics.domain.MatchAnalyticsReport
import com.example.coralive.ui.DotMatrixText
import com.example.coralive.ui.DottedProbabilityBar
import com.example.coralive.ui.theme.CoraTheme
import java.util.Locale

@Composable
fun MatchAnalyticsSection(report: MatchAnalyticsReport) {
    val homePct = (report.homeWinProb * 100).toInt()
    val drawPct = (report.drawProb * 100).toInt()
    val awayPct = maxOf(0, 100 - homePct - drawPct)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CoraTheme.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
         Surface(
            shape = RoundedCornerShape(18.dp),
            color = CoraTheme.colors.surface,
            border = CoraTheme.cardBorder
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${report.homeProfile.teamName.uppercase()} $homePct%",
                        color = CoraTheme.colors.homeAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "DRAW $drawPct%",
                        color = CoraTheme.colors.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${report.awayProfile.teamName.uppercase()} $awayPct%",
                        color = CoraTheme.colors.awayAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                DottedProbabilityBar(
                    homeProb = report.homeWinProb.toFloat(),
                    drawProb = report.drawProb.toFloat(),
                    awayProb = report.awayWinProb.toFloat()
                )

                Spacer(modifier = Modifier.height(14.dp))

                 val insightText = buildAiInsight(report, homePct, awayPct)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CoraTheme.colors.surfaceElevated)
                        .border(1.dp, CoraTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, CoraTheme.colors.textSecondary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI",
                            color = CoraTheme.colors.textPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = insightText,
                        color = CoraTheme.colors.textPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

         Surface(
            shape = RoundedCornerShape(18.dp),
            color = CoraTheme.colors.surface,
            border = CoraTheme.cardBorder
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DIXON-COLES PREDICTED SCORELINE",
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.2f", report.expectedHomeGoals),
                            color = CoraTheme.colors.homeAccent,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text("xG HOME", fontSize = 10.sp, color = CoraTheme.colors.textSecondary, fontFamily = FontFamily.Monospace)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DotMatrixText(
                            text = "${report.mostLikelyScore.homeGoals}:${report.mostLikelyScore.awayGoals}",
                            color = CoraTheme.colors.textPrimary,
                            dotSize = 4.dp,
                            dotSpacing = 1.2.dp,
                            charSpacing = 5.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PROB ${(report.mostLikelyScore.probability * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = CoraTheme.colors.textSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.2f", report.expectedAwayGoals),
                            color = CoraTheme.colors.awayAccent,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text("xG AWAY", fontSize = 10.sp, color = CoraTheme.colors.textSecondary, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    report.topScorelines.forEach { sc ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CoraTheme.colors.surfaceElevated)
                                .border(1.dp, CoraTheme.colors.border, RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${sc.homeGoals}:${sc.awayGoals}",
                                color = CoraTheme.colors.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${(sc.probability * 100).toInt()}%",
                                color = CoraTheme.colors.textSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

         Surface(
            shape = RoundedCornerShape(18.dp),
            color = CoraTheme.colors.surface,
            border = CoraTheme.cardBorder
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "TEAM PERFORMANCE METRICS",
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                MetricBarRow(
                    label = "ATTACK RATING",
                    homeVal = "${report.homeProfile.attackRating.toInt()}",
                    awayVal = "${report.awayProfile.attackRating.toInt()}",
                    homeFrac = (report.homeProfile.attackRating / 100.0).toFloat(),
                    awayFrac = (report.awayProfile.attackRating / 100.0).toFloat()
                )

                MetricBarRow(
                    label = "DEFENCE RATING",
                    homeVal = "${report.homeProfile.defenceRating.toInt()}",
                    awayVal = "${report.awayProfile.defenceRating.toInt()}",
                    homeFrac = (report.homeProfile.defenceRating / 100.0).toFloat(),
                    awayFrac = (report.awayProfile.defenceRating / 100.0).toFloat()
                )
            }
        }
    }
}

@Composable
private fun MetricBarRow(
    label: String,
    homeVal: String,
    awayVal: String,
    homeFrac: Float,
    awayFrac: Float
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(homeVal, color = CoraTheme.colors.homeAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(label, color = CoraTheme.colors.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text(awayVal, color = CoraTheme.colors.awayAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(
                progress = { homeFrac.coerceIn(0f, 1f) },
                modifier = Modifier.weight(1f).height(5.dp).clip(CircleShape),
                color = CoraTheme.colors.homeAccent,
                trackColor = CoraTheme.colors.dotInactive
            )
            LinearProgressIndicator(
                progress = { awayFrac.coerceIn(0f, 1f) },
                modifier = Modifier.weight(1f).height(5.dp).clip(CircleShape),
                color = CoraTheme.colors.awayAccent,
                trackColor = CoraTheme.colors.dotInactive
            )
        }
    }
}

private fun buildAiInsight(report: MatchAnalyticsReport, homePct: Int, awayPct: Int): String {
    val favorite = if (homePct >= awayPct) report.homeProfile else report.awayProfile
    val favPct = maxOf(homePct, awayPct)
    return "يمتلك ${favorite.teamName} أفضلية إحصائية بنسبة $favPct% بمعدل تسجيل ${String.format(Locale.US, "%.2f", favorite.goalsPerGame)} هدف/مباراة، والنتيجة الأقرب رياضياً هي ${report.mostLikelyScore.homeGoals}-${report.mostLikelyScore.awayGoals}."
}