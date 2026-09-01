package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComparisonMatrix
import com.example.data.DailyCountEntry
import com.example.data.GrandCumulativeStats
import com.example.data.MonthlyAggregate
import com.example.data.TrackerEntity
import com.example.data.TrackerSummary
import com.example.data.YearlyAggregate
import com.example.util.DateUtils
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiPeriodLogsView(
    summaries: List<TrackerSummary>,
    grandStats: GrandCumulativeStats,
    matrix: ComparisonMatrix,
    onExportClicked: () -> Unit,
    onEditEntry: (DailyCountEntry) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onEditTracker: (TrackerEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Selected view period: 0: Grand Cumulative, 1: Daily Log, 2: Month Log, 3: Year Log, 4: Matrix Grid
    var selectedPeriodTab by remember { mutableIntStateOf(0) }

    // Tracker filter: null means "All Names / Combined"
    var selectedTrackerFilterId by remember { mutableStateOf<Long?>(null) }

    val filteredSummary = if (selectedTrackerFilterId != null) {
        summaries.find { it.tracker.id == selectedTrackerFilterId }
    } else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Grand Cumulative Hero Card (Always visible at top)
        GrandCumulativeHeroCard(
            grandStats = grandStats,
            onExportClicked = onExportClicked
        )

        // Period Selection Tabs (Cumulative, Daily Log, Month Log, Year Log, Matrix)
        SecondaryTabRow(
            selectedTabIndex = selectedPeriodTab,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Tab(
                selected = selectedPeriodTab == 0,
                onClick = { selectedPeriodTab = 0 },
                text = { Text("Cumulative", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_log_cumulative")
            )
            Tab(
                selected = selectedPeriodTab == 1,
                onClick = { selectedPeriodTab = 1 },
                text = { Text("Daily", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_log_daily")
            )
            Tab(
                selected = selectedPeriodTab == 2,
                onClick = { selectedPeriodTab = 2 },
                text = { Text("Monthly", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_log_monthly")
            )
            Tab(
                selected = selectedPeriodTab == 3,
                onClick = { selectedPeriodTab = 3 },
                text = { Text("Yearly", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_log_yearly")
            )
            Tab(
                selected = selectedPeriodTab == 4,
                onClick = { selectedPeriodTab = 4 },
                text = { Text("Matrix", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_log_matrix")
            )
        }

        // Name / Tracker Filter Selector Chips
        if (selectedPeriodTab != 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Name:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedTrackerFilterId == null,
                            onClick = { selectedTrackerFilterId = null },
                            label = { Text("All Names (Combined)", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("filter_chip_all_names")
                        )
                    }

                    items(summaries, key = { it.tracker.id }) { summary ->
                        val isSelected = selectedTrackerFilterId == summary.tracker.id
                        val color = Color(summary.tracker.colorHex)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTrackerFilterId = summary.tracker.id },
                            label = { Text(summary.tracker.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, CircleShape)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, color) else null,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("filter_chip_${summary.tracker.name}")
                        )
                    }
                }
            }
        }

        // Render the Active Log Tab Content
        when (selectedPeriodTab) {
            // 0: Grand Cumulative Breakdown & Name Contributions
            0 -> {
                GrandCumulativeContributionView(
                    grandStats = grandStats,
                    summaries = summaries,
                    onEditTracker = onEditTracker
                )
            }

            // 1: Daily Log
            1 -> {
                DailyLogSection(
                    summaries = summaries,
                    filteredSummary = filteredSummary,
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry
                )
            }

            // 2: Monthly Log
            2 -> {
                MonthlyLogSection(
                    summaries = summaries,
                    filteredSummary = filteredSummary
                )
            }

            // 3: Yearly Log
            3 -> {
                YearlyLogSection(
                    summaries = summaries,
                    filteredSummary = filteredSummary
                )
            }

            // 4: Matrix Grid (Cross Comparison)
            4 -> {
                MatrixGridSection(
                    matrix = matrix
                )
            }
        }
    }
}

// ==========================================
// 1. GRAND CUMULATIVE HERO CARD
// ==========================================
@Composable
fun GrandCumulativeHeroCard(
    grandStats: GrandCumulativeStats,
    onExportClicked: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "GRAND CUMULATIVE TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "All Counters Combined",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onExportClicked,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("export_grand_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Big Grand Total Counter Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${grandStats.allTimeTotal}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontFamily = FontFamily.Monospace
                )

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${grandStats.activeTrackersCount} Names Active",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

            // Timeframe metrics row (Today, This Month, This Year)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    label = "TODAY",
                    value = "+${grandStats.todayTotal}",
                    subtitle = "vs y'day ${grandStats.yesterdayTotal}"
                )

                MetricColumn(
                    label = "THIS MONTH",
                    value = "${grandStats.thisMonthTotal}",
                    subtitle = DateUtils.formatToMonthShortDisplay(DateUtils.thisMonthIso())
                )

                MetricColumn(
                    label = "THIS YEAR",
                    value = "${grandStats.thisYearTotal}",
                    subtitle = DateUtils.thisYearIso()
                )
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    subtitle: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

// ==========================================
// 2. GRAND CUMULATIVE CONTRIBUTION BREAKDOWN
// ==========================================
@Composable
fun GrandCumulativeContributionView(
    grandStats: GrandCumulativeStats,
    summaries: List<TrackerSummary>,
    onEditTracker: (TrackerEntity) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "NAME-WISE CUMULATIVE CONTRIBUTION",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        grandStats.contributions.forEach { item ->
            val color = Color(item.tracker.colorHex)

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contribution_card_${item.tracker.name}"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, color.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(color, CircleShape)
                            )
                            Text(
                                text = item.tracker.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = color.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.tracker.unit,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${item.totalCount}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = color,
                                fontFamily = FontFamily.Monospace
                            )

                            IconButton(
                                onClick = { onEditTracker(item.tracker) },
                                modifier = Modifier.size(28.dp).testTag("edit_tracker_name_${item.tracker.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name & Unit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Progress Bar showing percentage of grand total
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Share of Grand Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.1f%%", item.percentageOfGrandTotal),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = color
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (item.percentageOfGrandTotal / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = color.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    // Mini summary breakdown for this name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Today: +${item.todayCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "This Month: ${item.thisMonthCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. DAILY LOG SECTION
// ==========================================
@Composable
fun DailyLogSection(
    summaries: List<TrackerSummary>,
    filteredSummary: TrackerSummary?,
    onEditEntry: (DailyCountEntry) -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (filteredSummary != null) "DAILY LOG: ${filteredSummary.tracker.name.uppercase()}" else "DAILY LOG: ALL NAMES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        if (filteredSummary != null) {
            // Specific Tracker's daily aggregates
            if (filteredSummary.dailyAggregates.isEmpty()) {
                EmptyLogPlaceholder("No daily counts recorded for ${filteredSummary.tracker.name}")
            } else {
                filteredSummary.dailyAggregates.forEach { daily ->
                    DailyLogCard(
                        dateString = daily.dateString,
                        totalCount = daily.totalCount,
                        unit = filteredSummary.tracker.unit,
                        trackerColor = Color(filteredSummary.tracker.colorHex),
                        trackerName = filteredSummary.tracker.name,
                        entries = daily.entries,
                        onEditEntry = onEditEntry,
                        onDeleteEntry = onDeleteEntry
                    )
                }
            }
        } else {
            // All trackers combined daily
            val allDates = summaries
                .flatMap { it.dailyAggregates }
                .map { it.dateString }
                .distinct()
                .sortedDescending()

            if (allDates.isEmpty()) {
                EmptyLogPlaceholder("No daily counts recorded yet")
            } else {
                allDates.forEach { date ->
                    val daySum = summaries.sumOf { s ->
                        s.dailyAggregates.find { it.dateString == date }?.totalCount ?: 0L
                    }

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = DateUtils.formatToPrettyDisplay(date),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = DateUtils.formatToFullDisplay(date),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Total: $daySum",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Individual names breakdown for this day
                            summaries.forEach { s ->
                                val count = s.dailyAggregates.find { it.dateString == date }?.totalCount ?: 0L
                                val color = Color(s.tracker.colorHex)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, CircleShape)
                                        )
                                        Text(
                                            text = s.tracker.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Text(
                                        text = "$count ${s.tracker.unit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (count > 0) color else MaterialTheme.colorScheme.outline,
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
fun DailyLogCard(
    dateString: String,
    totalCount: Long,
    unit: String,
    trackerColor: Color,
    trackerName: String,
    entries: List<DailyCountEntry>,
    onEditEntry: (DailyCountEntry) -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, trackerColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.formatToPrettyDisplay(dateString),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = DateUtils.formatToShortDisplay(dateString),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$totalCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = trackerColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tally Marks Visualizer for this day
            if (totalCount > 0) {
                TallyMarksVisualizer(
                    count = totalCount,
                    color = trackerColor,
                    maxClustersToShow = 6
                )
            }

            if (entries.isNotEmpty()) {
                Text(
                    text = if (expanded) "Hide ${entries.size} entry details ▲" else "View ${entries.size} individual logs ▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = trackerColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { expanded = !expanded }
                )

                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        entries.forEach { entry ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (entry.countValue >= 0) "+${entry.countValue}" else "${entry.countValue}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = trackerColor
                                        )
                                        if (entry.note.isNotBlank()) {
                                            Text(
                                                text = entry.note,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = { onEditEntry(entry) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteEntry(entry.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
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
    }
}

// ==========================================
// 4. MONTHLY LOG SECTION
// ==========================================
@Composable
fun MonthlyLogSection(
    summaries: List<TrackerSummary>,
    filteredSummary: TrackerSummary?
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (filteredSummary != null) "MONTHLY LOG: ${filteredSummary.tracker.name.uppercase()}" else "MONTHLY LOG: ALL NAMES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        if (filteredSummary != null) {
            if (filteredSummary.monthlyAggregates.isEmpty()) {
                EmptyLogPlaceholder("No monthly records for ${filteredSummary.tracker.name}")
            } else {
                filteredSummary.monthlyAggregates.forEach { monthAgg ->
                    MonthCard(
                        monthAgg = monthAgg,
                        unit = filteredSummary.tracker.unit,
                        trackerColor = Color(filteredSummary.tracker.colorHex)
                    )
                }
            }
        } else {
            // All Trackers Combined Monthly
            val allMonths = summaries
                .flatMap { it.monthlyAggregates }
                .map { it.monthString }
                .distinct()
                .sortedDescending()

            if (allMonths.isEmpty()) {
                EmptyLogPlaceholder("No monthly records yet")
            } else {
                allMonths.forEach { monthIso ->
                    val monthGrandSum = summaries.sumOf { s ->
                        s.monthlyAggregates.find { it.monthString == monthIso }?.totalCount ?: 0L
                    }

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = DateUtils.formatToMonthDisplay(monthIso),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Total: $monthGrandSum",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Name by Name in this month
                            summaries.forEach { s ->
                                val m = s.monthlyAggregates.find { it.monthString == monthIso }
                                val count = m?.totalCount ?: 0L
                                val color = Color(s.tracker.colorHex)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, CircleShape)
                                        )
                                        Text(
                                            text = s.tracker.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Text(
                                        text = "$count ${s.tracker.unit} (${m?.activeDaysCount ?: 0} days active)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (count > 0) color else MaterialTheme.colorScheme.outline,
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
fun MonthCard(
    monthAgg: MonthlyAggregate,
    unit: String,
    trackerColor: Color
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, trackerColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthAgg.displayMonth,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "${monthAgg.totalCount} $unit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = trackerColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Active Days: ${monthAgg.activeDaysCount}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                val avg = if (monthAgg.activeDaysCount > 0) monthAgg.totalCount.toDouble() / monthAgg.activeDaysCount else 0.0
                Text(
                    text = String.format("Daily Avg: %.1f", avg),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = trackerColor
                )
            }
        }
    }
}

// ==========================================
// 5. YEARLY LOG SECTION
// ==========================================
@Composable
fun YearlyLogSection(
    summaries: List<TrackerSummary>,
    filteredSummary: TrackerSummary?
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (filteredSummary != null) "YEARLY LOG: ${filteredSummary.tracker.name.uppercase()}" else "YEARLY LOG: ALL NAMES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        if (filteredSummary != null) {
            if (filteredSummary.yearlyAggregates.isEmpty()) {
                EmptyLogPlaceholder("No yearly records for ${filteredSummary.tracker.name}")
            } else {
                filteredSummary.yearlyAggregates.forEach { yearAgg ->
                    YearCard(
                        yearAgg = yearAgg,
                        unit = filteredSummary.tracker.unit,
                        trackerColor = Color(filteredSummary.tracker.colorHex)
                    )
                }
            }
        } else {
            // All Trackers Combined Yearly
            val allYears = summaries
                .flatMap { it.yearlyAggregates }
                .map { it.yearString }
                .distinct()
                .sortedDescending()

            if (allYears.isEmpty()) {
                EmptyLogPlaceholder("No yearly records yet")
            } else {
                allYears.forEach { yearIso ->
                    val yearGrandSum = summaries.sumOf { s ->
                        s.yearlyAggregates.find { it.yearString == yearIso }?.totalCount ?: 0L
                    }

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Year $yearIso",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Annual Total: $yearGrandSum",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Name-by-name in this year
                            summaries.forEach { s ->
                                val y = s.yearlyAggregates.find { it.yearString == yearIso }
                                val count = y?.totalCount ?: 0L
                                val color = Color(s.tracker.colorHex)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, CircleShape)
                                        )
                                        Text(
                                            text = s.tracker.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Text(
                                        text = "$count ${s.tracker.unit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (count > 0) color else MaterialTheme.colorScheme.outline,
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
fun YearCard(
    yearAgg: YearlyAggregate,
    unit: String,
    trackerColor: Color
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, trackerColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Year ${yearAgg.yearString}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "${yearAgg.totalCount} $unit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = trackerColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Monthly breakdown tags
            Text(
                text = "Monthly Progression:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(yearAgg.monthlyAggregates) { m ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = trackerColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, trackerColor.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = DateUtils.formatToMonthShortDisplay(m.monthString),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${m.totalCount}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = trackerColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. MATRIX GRID SECTION
// ==========================================
@Composable
fun MatrixGridSection(
    matrix: ComparisonMatrix
) {
    val scrollState = rememberScrollState()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "CROSS-TRACKER MATRIX COMPARISON",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            // Scrollable Matrix Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NAME",
                            modifier = Modifier.width(110.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        matrix.dates.forEach { date ->
                            Text(
                                text = DateUtils.formatToShortDisplay(date),
                                modifier = Modifier.width(60.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "TOTAL",
                            modifier = Modifier.width(75.dp),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Data Rows
                    matrix.rows.forEach { row ->
                        val trackerColor = Color(row.tracker.colorHex)
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.width(110.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(trackerColor, CircleShape)
                                )
                                Text(
                                    text = row.tracker.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            matrix.dates.forEach { date ->
                                val count = row.countsByDate[date] ?: 0L
                                Text(
                                    text = if (count > 0) "$count" else "-",
                                    modifier = Modifier.width(60.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (count > 0) trackerColor else MaterialTheme.colorScheme.outline,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Text(
                                text = "${row.cumulativeTotal}",
                                modifier = Modifier.width(75.dp),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = trackerColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Grand Totals Row
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY SUM",
                            modifier = Modifier.width(110.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )

                        matrix.dates.forEach { date ->
                            val dailySum = matrix.dailyTotals[date] ?: 0L
                            Text(
                                text = if (dailySum > 0) "$dailySum" else "-",
                                modifier = Modifier.width(60.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "${matrix.grandTotal}",
                            modifier = Modifier.width(75.dp),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLogPlaceholder(message: String) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
