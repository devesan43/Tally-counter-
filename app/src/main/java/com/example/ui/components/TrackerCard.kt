package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrackerEntity
import com.example.data.TrackerSummary
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackerCard(
    summary: TrackerSummary,
    onQuickAdd: (delta: Long) -> Unit,
    onOpenAddCount: () -> Unit,
    onOpenDetails: () -> Unit,
    onEditTracker: (TrackerEntity) -> Unit,
    onDeleteTracker: (TrackerEntity) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tracker = summary.tracker
    val trackerColor = Color(tracker.colorHex)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onOpenDetails() }
            .testTag("tracker_card_${tracker.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Avatar, Name, Category, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Avatar Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(trackerColor.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tracker.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = trackerColor,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tracker.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = trackerColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tracker.category,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = trackerColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("tracker_menu_${tracker.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Detailed History") },
                            leadingIcon = {
                                Icon(Icons.Default.TrendingUp, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onOpenDetails()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Tracker") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onEditTracker(tracker)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear History") },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onClearHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Tracker", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteTracker(tracker)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Metrics Section: Cumulative Total & Today Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CUMULATIVE TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 0.8.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${summary.cumulativeTotal}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = trackerColor
                        )
                        Text(
                            text = tracker.unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 0.8.sp
                    )
                    Surface(
                        color = if (summary.todayTotal > 0) trackerColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+${summary.todayTotal}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.todayTotal > 0) trackerColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Daily History Pills (e.g. 25/08: 100 | 26/08: 300 | 27/08: 100)
            if (summary.dailyAggregates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Daily Entries (Date: Count)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Show in chronological order for quick visual scanning
                    val sortedChronological = summary.dailyAggregates.sortedBy { it.dateString }
                    sortedChronological.takeLast(6).forEach { daily ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = DateUtils.formatToShortDisplay(daily.dateString),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = ":",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${daily.totalCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = trackerColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Tally Clicker Action Buttons (-1, +1, +10, +50, +100, + Custom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // -1 Decrement
                FilledTonalButton(
                    onClick = { onQuickAdd(-1L) },
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("quick_minus_1_${tracker.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "-1",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // +1
                FilledTonalButton(
                    onClick = { onQuickAdd(1L) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("quick_add_1_${tracker.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "+1",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                // +10
                FilledTonalButton(
                    onClick = { onQuickAdd(10L) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("quick_add_10_${tracker.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "+10",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // +50
                FilledTonalButton(
                    onClick = { onQuickAdd(50L) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("quick_add_50_${tracker.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "+50",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // +100
                FilledTonalButton(
                    onClick = { onQuickAdd(100L) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("quick_add_100_${tracker.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "+100",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                ElevatedButton(
                    onClick = onOpenAddCount,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("custom_add_btn_${tracker.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = trackerColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Count",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Log",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
