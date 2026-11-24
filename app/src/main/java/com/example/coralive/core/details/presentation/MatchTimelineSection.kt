package com.example.coralive.core.details.presentation


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coralive.core.details.data.TimelineEventItem

@Composable
fun MatchTimelineSection(
    events: List<TimelineEventItem>,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF111115), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF23232A), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "MATCH TIMELINE // KEY EVENTS",
            color = Color(0xFF9E9EA9),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        events.forEach { event ->
            val (badgeColor, badgeSymbol) = when (event.type) {
                "goal", "penalty" -> Color(0xFF00E676) to "● GOAL"
                "red" -> Color(0xFFFF1744) to "■ RED"
                "yellow" -> Color(0xFFFFD600) to "■ YEL"
                "sub" -> Color(0xFF40C4FF) to "⇄ SUB"
                else -> Color(0xFF8E8E93) to "• EVT"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF08080A), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                 Text(
                    text = event.minute.padStart(3, ' '),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                 Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.16f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeSymbol,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                 Text(
                    text = event.title,
                    color = Color(0xFFE5E5EA),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )

                 if (event.scoreSnapshot.isNotBlank() && (event.type == "goal" || event.type == "penalty")) {
                    Text(
                        text = event.scoreSnapshot,
                        color = Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}