package com.example.coralive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coralive.ui.theme.CoraTheme


@Composable
fun DotMatrixText(
    text: String,
    color: Color = CoraTheme.colors.textPrimary,
    dotSize: Dp = 3.2.dp,
    dotSpacing: Dp = 1.2.dp,
    charSpacing: Dp = 5.dp,
    modifier: Modifier = Modifier
) {
    val cleanText = text.uppercase()
    val glyphs = cleanText.map { DotMatrixFont.getGlyph(it) }
    if (glyphs.isEmpty()) return

    val rows = 7
    val totalCols = glyphs.sumOf { it[0].length }
    val gapsCount = (glyphs.size - 1).coerceAtLeast(0)

    val totalWidth = dotSize * totalCols + dotSpacing * (totalCols - glyphs.size).coerceAtLeast(0) + charSpacing * gapsCount
    val totalHeight = dotSize * rows + dotSpacing * (rows - 1)

    Canvas(modifier = modifier.size(width = totalWidth, height = totalHeight)) {
        val dPx = dotSize.toPx()
        val sPx = dotSpacing.toPx()
        val cPx = charSpacing.toPx()

        var cursorX = 0f
        glyphs.forEach { grid ->
            val cols = grid[0].length
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (grid[r][c] == '1') {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(
                                x = cursorX + c * (dPx + sPx),
                                y = r * (dPx + sPx)
                            ),
                            size = Size(dPx, dPx),
                            cornerRadius = CornerRadius(dPx * 0.22f, dPx * 0.22f)
                        )
                    }
                }
            }
            cursorX += cols * dPx + (cols - 1).coerceAtLeast(0) * sPx + cPx
        }
    }
}

/**
 * شريط احتمالات منقط (Dotted Probability Bar) كما في التصميم
 */
@Composable
fun DottedProbabilityBar(
    homeProb: Float,
    drawProb: Float,
    awayProb: Float,
    totalDots: Int = 34,
    modifier: Modifier = Modifier
) {
    val homeDots = (homeProb * totalDots).toInt().coerceIn(0, totalDots)
    val drawDots = (drawProb * totalDots).toInt().coerceIn(0, totalDots - homeDots)
    val awayDots = (totalDots - homeDots - drawDots).coerceAtLeast(0)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val dotColor = when {
                index < homeDots -> CoraTheme.colors.homeAccent
                index < homeDots + drawDots -> CoraTheme.colors.textSecondary.copy(alpha = 0.5f)
                else -> CoraTheme.colors.awayAccent
            }
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

/**
 * كبسولة تبويب أبيض/أسود (Pill Tab) مثل Live / Stats / Lineups
 */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) CoraTheme.colors.pillActiveBg else CoraTheme.colors.surface
    val textColor = if (selected) CoraTheme.colors.pillActiveText else CoraTheme.colors.textSecondary
    val borderColor = if (selected) CoraTheme.colors.pillActiveBg else CoraTheme.colors.border

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * شارة البث المباشر ● LIVE 67'
 */
@Composable
fun DotLiveBadge(statusText: String, isLive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLive) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(CoraTheme.colors.liveRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = if (isLive) "LIVE $statusText" else statusText.uppercase(),
            color = if (isLive) CoraTheme.colors.liveRed else CoraTheme.colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

private object DotMatrixFont {
    private val map = mapOf(
        '0' to listOf("01110", "10001", "10011", "10101", "11001", "10001", "01110"),
        '1' to listOf("00100", "01100", "00100", "00100", "00100", "00100", "01110"),
        '2' to listOf("01110", "10001", "00001", "00110", "01000", "10000", "11111"),
        '3' to listOf("11110", "00001", "00001", "01110", "00001", "00001", "11110"),
        '4' to listOf("00010", "00110", "01010", "10010", "11111", "00010", "00010"),
        '5' to listOf("11111", "10000", "11110", "00001", "00001", "10001", "01110"),
        '6' to listOf("01110", "10000", "11110", "10001", "10001", "10001", "01110"),
        '7' to listOf("11111", "00001", "00010", "00100", "01000", "01000", "01000"),
        '8' to listOf("01110", "10001", "10001", "01110", "10001", "10001", "01110"),
        '9' to listOf("01110", "10001", "10001", "01111", "00001", "00001", "01110"),
        ':' to listOf("00", "00", "11", "00", "11", "00", "00"),
        '-' to listOf("000", "000", "000", "111", "000", "000", "000"),
        ' ' to listOf("000", "000", "000", "000", "000", "000", "000"),
        'A' to listOf("01110", "10001", "10001", "11111", "10001", "10001", "10001"),
        'B' to listOf("11110", "10001", "10001", "11110", "10001", "10001", "11110"),
        'C' to listOf("01110", "10001", "10000", "10000", "10000", "10001", "01110"),
        'D' to listOf("11100", "10010", "10001", "10001", "10001", "10010", "11100"),
        'E' to listOf("11111", "10000", "10000", "11110", "10000", "10000", "11111"),
        'F' to listOf("11111", "10000", "10000", "11110", "10000", "10000", "10000"),
        'G' to listOf("01110", "10001", "10000", "10111", "10001", "10001", "01110"),
        'H' to listOf("10001", "10001", "10001", "11111", "10001", "10001", "10001"),
        'I' to listOf("01110", "00100", "00100", "00100", "00100", "00100", "01110"),
        'J' to listOf("00111", "00010", "00010", "00010", "10010", "10010", "01100"),
        'K' to listOf("10001", "10010", "10100", "11000", "10100", "10010", "10001"),
        'L' to listOf("10000", "10000", "10000", "10000", "10000", "10000", "11111"),
        'M' to listOf("10001", "11011", "10101", "10101", "10001", "10001", "10001"),
        'N' to listOf("10001", "11001", "10101", "10011", "10001", "10001", "10001"),
        'O' to listOf("01110", "10001", "10001", "10001", "10001", "10001", "01110"),
        'P' to listOf("11110", "10001", "10001", "11110", "10000", "10000", "10000"),
        'Q' to listOf("01110", "10001", "10001", "10001", "10101", "10010", "01101"),
        'R' to listOf("11110", "10001", "10001", "11110", "10100", "10010", "10001"),
        'S' to listOf("01110", "10001", "10000", "01110", "00001", "10001", "01110"),
        'T' to listOf("11111", "00100", "00100", "00100", "00100", "00100", "00100"),
        'U' to listOf("10001", "10001", "10001", "10001", "10001", "10001", "01110"),
        'V' to listOf("10001", "10001", "10001", "10001", "10001", "01010", "00100"),
        'W' to listOf("10001", "10001", "10001", "10101", "10101", "11011", "10001"),
        'X' to listOf("10001", "10001", "01010", "00100", "01010", "10001", "10001"),
        'Y' to listOf("10001", "10001", "01010", "00100", "00100", "00100", "00100"),
        'Z' to listOf("11111", "00001", "00010", "00100", "01000", "10000", "11111")
    )

    fun getGlyph(c: Char): List<String> = map[c] ?: map['-']!!
}