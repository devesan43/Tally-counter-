package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Draws visual tally marks (groups of 5 strokes with diagonal cross)
 */
@Composable
fun TallyMarksVisualizer(
    count: Long,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return

    val displayCount = count.coerceAtMost(200) // limit visual groups to prevent canvas overflow
    val fullGroups = (displayCount / 5).toInt()
    val remainder = (displayCount % 5).toInt()
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(fullGroups) {
            TallyGroup(count = 5, color = color)
        }
        if (remainder > 0) {
            TallyGroup(count = remainder, color = color)
        }
    }
}

@Composable
fun TallyGroup(
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(28.dp)
            .height(26.dp)
    ) {
        val strokeWidth = 3.dp.toPx()
        val spacing = size.width / 4.5f
        val startY = 3.dp.toPx()
        val endY = size.height - 3.dp.toPx()

        // Draw vertical strokes (up to 4)
        val verticalCount = count.coerceAtMost(4)
        for (i in 0 until verticalCount) {
            val x = (i + 0.5f) * spacing
            drawLine(
                color = color,
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // Draw diagonal cross stroke if 5
        if (count >= 5) {
            drawLine(
                color = color,
                start = Offset(2.dp.toPx(), endY - 2.dp.toPx()),
                end = Offset(size.width - 2.dp.toPx(), startY + 2.dp.toPx()),
                strokeWidth = strokeWidth * 1.1f,
                cap = StrokeCap.Round
            )
        }
    }
}
