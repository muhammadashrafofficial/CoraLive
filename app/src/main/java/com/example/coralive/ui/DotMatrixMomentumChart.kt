package com.example.coralive.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coralive.ui.theme.CoraTheme
import kotlin.math.abs
import kotlin.math.roundToInt

data class MomentumGoalMarker(
    val minute: Int,
    val isHome: Boolean,
    val label: String = "GOAL ${minute}'"
)

@Composable
fun DotMatrixMomentumCard(
    homeAbbr: String,
    awayAbbr: String,
    momentumValues: List<Float>,
    goalMarkers: List<MomentumGoalMarker>,
    currentMinute: Int = 90,
    modifier: Modifier = Modifier
) {
    val homeColor = CoraTheme.colors.homeAccent
    val awayColor = CoraTheme.colors.awayAccent
    val inactiveDotColor = Color(0xFF1E1E24)

    Surface(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = "MOMENTUM",
                    color = CoraTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendDotItem(color = homeColor, label = homeAbbr)
                    LegendDotItem(color = awayColor, label = awayAbbr)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            val homeGoals = goalMarkers.filter { it.isHome }
            if (homeGoals.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    homeGoals.forEach { goal ->
                        GoalTagBox(text = goal.label, accentColor = homeColor)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }


            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val cols = 30
                val halfRows = 5
                val totalRows = halfRows * 2 + 1

                val stepX = size.width / (cols - 1).coerceAtLeast(1)
                val stepY = size.height / (totalRows - 1).coerceAtLeast(1)
                val dotRadius = 2.4.dp.toPx()


                val htX = stepX * 14.5f
                drawLine(
                    color = Color(0xFF33333A),
                    start = Offset(htX, 0f),
                    end = Offset(htX, size.height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )


                val currentCol = ((currentMinute.coerceIn(0, 90) / 90f) * (cols - 1)).roundToInt()
                val liveX = currentCol * stepX
                drawLine(
                    color = Color(0xFFEF4444),
                    start = Offset(liveX, 0f),
                    end = Offset(liveX, size.height),
                    strokeWidth = 1.2.dp.toPx()
                )

                for (c in 0 until cols) {
                    val value = momentumValues.getOrElse(c) { 0f }.coerceIn(-1f, 1f)
                    val activeMagnitude = (abs(value) * halfRows).roundToInt()
                    val isPlayed = c <= currentCol

                    for (r in 0 until totalRows) {
                        val rowOffsetFromCenter = halfRows - r
                        val dotColor = when {
                            !isPlayed -> inactiveDotColor
                            rowOffsetFromCenter == 0 && value != 0f ->
                                if (value > 0f) homeColor.copy(alpha = 0.7f) else awayColor.copy(alpha = 0.7f)
                            value > 0f && rowOffsetFromCenter in 1..activeMagnitude -> homeColor
                            value < 0f && rowOffsetFromCenter in -activeMagnitude..-1 -> awayColor
                            else -> inactiveDotColor
                        }

                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(c * stepX, r * stepY)
                        )
                    }
                }
            }

            val awayGoals = goalMarkers.filter { !it.isHome }
            if (awayGoals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    awayGoals.forEach { goal ->
                        GoalTagBox(text = goal.label, accentColor = awayColor)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0'", color = CoraTheme.colors.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("45'", color = CoraTheme.colors.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("90'", color = CoraTheme.colors.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun LegendDotItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = CoraTheme.colors.textPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GoalTagBox(text: String, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            color = accentColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}