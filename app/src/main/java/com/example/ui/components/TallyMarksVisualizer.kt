package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun TallyMarksVisualizer(
    count: Long,
    color: Color = MaterialTheme.colorScheme.primary,
    maxClustersToShow: Int = 10,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return

    val fullGroups = (count / 5).toInt()
    val remainder = (count % 5).toInt()
    val displayGroups = min(fullGroups, maxClustersToShow)

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render 5-tally bundles
        for (i in 0 until displayGroups) {
            TallyBundleFive(color = color)
        }

        // Render remainder tally bundle
        if (remainder > 0 && fullGroups < maxClustersToShow) {
            TallyBundlePartial(count = remainder, color = color)
        }

        if (fullGroups > maxClustersToShow) {
            Text(
                text = "+${(count - (maxClustersToShow * 5))} more",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
fun TallyBundleFive(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 36.dp, height = 30.dp)) {
        val strokeWidth = 3.5.dp.toPx()
        val spacing = size.width / 5

        // 4 vertical lines
        for (i in 1..4) {
            val x = i * spacing
            drawLine(
                color = color,
                start = Offset(x, 2.dp.toPx()),
                end = Offset(x, size.height - 2.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // 1 diagonal slash
        drawLine(
            color = color,
            start = Offset(spacing * 0.6f, size.height - 3.dp.toPx()),
            end = Offset(spacing * 4.4f, 3.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun TallyBundlePartial(
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = (count * 9 + 6).dp, height = 30.dp)) {
        val strokeWidth = 3.5.dp.toPx()
        val spacing = size.width / (count + 1)

        for (i in 1..count) {
            val x = i * spacing
            drawLine(
                color = color,
                start = Offset(x, 2.dp.toPx()),
                end = Offset(x, size.height - 2.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
