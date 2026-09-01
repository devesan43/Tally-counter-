package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyCountEntry
import com.example.data.TrackerEntity
import com.example.data.TrackerSummary
import androidx.compose.material.icons.filled.TouchApp
import com.example.ui.components.AddCountDialog
import com.example.ui.components.AddTrackerDialog
import com.example.ui.components.ComparisonMatrixView
import com.example.ui.components.EditCountEntryDialog
import com.example.ui.components.ExportDialog
import com.example.ui.components.MultiPeriodLogsView
import com.example.ui.components.TallyClickerScreen
import com.example.ui.components.TrackerCard
import com.example.ui.components.TrackerDetailSheet
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CountTrackerViewModel) {
    val rawSummaries by viewModel.rawSummaries.collectAsStateWithLifecycle()
    val summaries by viewModel.filteredSummaries.collectAsStateWithLifecycle()
    val matrix by viewModel.comparisonMatrix.collectAsStateWithLifecycle()
    val grandCumulativeStats by viewModel.grandCumulativeStats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTrackerId by viewModel.selectedTrackerId.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(
                message = it.message,
                actionLabel = it.actionLabel,
                duration = SnackbarDuration.Short
            )
            viewModel.clearNotification()
        }
    }

    var currentTab by remember { mutableIntStateOf(0) } // 0: Tally, 1: Trackers, 2: Matrix, 3: Timeline
    var showAddTrackerDialog by remember { mutableStateOf(false) }
    var trackerToEdit by remember { mutableStateOf<TrackerEntity?>(null) }
    var showAddCountDialog by remember { mutableStateOf(false) }
    var addCountTrackerIdTarget by remember { mutableStateOf<Long?>(null) }
    var entryToEdit by remember { mutableStateOf<DailyCountEntry?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    val selectedSummaryForDetail = rawSummaries.find { it.tracker.id == selectedTrackerId }

    val categories = remember(rawSummaries) {
        listOf("All") + rawSummaries.map { it.tracker.category }.distinct().filter { it.isNotBlank() }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(28.dp)) },
                    label = { Text("Tally", fontSize = 14.sp, fontWeight = if (currentTab == 0) FontWeight.Black else FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_tally")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(28.dp)) },
                    label = { Text("Trackers", fontSize = 14.sp, fontWeight = if (currentTab == 1) FontWeight.Black else FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_trackers")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(28.dp)) },
                    label = { Text("Logs", fontSize = 14.sp, fontWeight = if (currentTab == 2) FontWeight.Black else FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_matrix")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(28.dp)) },
                    label = { Text("Timeline", fontSize = 14.sp, fontWeight = if (currentTab == 3) FontWeight.Black else FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_timeline")
                )
            }
        },
        floatingActionButton = {
            if (currentTab != 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        addCountTrackerIdTarget = null
                        showAddCountDialog = true
                    },
                    modifier = Modifier.testTag("fab_add_count"),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Count", modifier = Modifier.size(24.dp)) },
                    text = { Text("Log Count", fontWeight = FontWeight.Black, fontSize = 16.sp) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // App Bar & Date Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Count Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = DateUtils.formatToPrettyDisplay(DateUtils.todayIso()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { showAddTrackerDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("header_add_tracker_btn"),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "New Name",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Name", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }

                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("header_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Data",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            when (currentTab) {
                // Tab 0: Tally Counter Experience
                0 -> {
                    TallyClickerScreen(
                        summaries = rawSummaries,
                        viewModel = viewModel,
                        onAddNewTracker = { showAddTrackerDialog = true },
                        onEditTracker = { trackerToEdit = it },
                        onDeleteTracker = { viewModel.deleteTrackerById(it) },
                        onOpenExport = { showExportDialog = true },
                        soundEnabled = soundEnabled,
                        hapticEnabled = hapticEnabled
                    )
                }

                // Tab 1: Trackers List Overview
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Grand Cumulative Banner
                        item {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL CUMULATIVE COUNT",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.8f),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "${matrix.grandTotal}",
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Surface(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "${rawSummaries.size} Trackers",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap card for details",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Search & Filter Bar
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_tracker_input"),
                                    placeholder = { Text("Search by name (e.g. Name-1)...") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = "Search")
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp)
                                )

                                if (categories.size > 2) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(categories) { cat ->
                                            FilterChip(
                                                selected = selectedCategory == cat,
                                                onClick = { viewModel.setSelectedCategory(cat) },
                                                label = { Text(cat) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Trackers List
                        if (summaries.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "No trackers found",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Tap 'New Name' above to create a tracker like Name-1 or Name-2.",
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(summaries, key = { it.tracker.id }) { summary ->
                                TrackerCard(
                                    summary = summary,
                                    onQuickAdd = { delta ->
                                        viewModel.quickIncrement(summary.tracker.id, delta)
                                    },
                                    onOpenAddCount = {
                                        addCountTrackerIdTarget = summary.tracker.id
                                        showAddCountDialog = true
                                    },
                                    onOpenDetails = {
                                        viewModel.selectTracker(summary.tracker.id)
                                    },
                                    onEditTracker = { tracker ->
                                        trackerToEdit = tracker
                                    },
                                    onDeleteTracker = { tracker ->
                                        viewModel.deleteTracker(tracker)
                                    },
                                    onClearHistory = {
                                        viewModel.clearTrackerHistory(summary.tracker.id)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Tab 2: Multi-Period Logs (Grand Cumulative, Daily Log, Month Log, Year Log, Matrix)
                2 -> {
                    MultiPeriodLogsView(
                        summaries = rawSummaries,
                        grandStats = grandCumulativeStats,
                        matrix = matrix,
                        onExportClicked = { showExportDialog = true },
                        onEditEntry = { entryToEdit = it },
                        onDeleteEntry = { viewModel.deleteCountEntry(it) },
                        onEditTracker = { trackerToEdit = it }
                    )
                }

                // Tab 3: Activity Timeline (All Entries Chronological)
                3 -> {
                    val allEntries = rawSummaries.flatMap { summary ->
                        summary.allEntries.map { entry -> entry to summary.tracker }
                    }.sortedByDescending { it.first.dateString + it.first.timestamp.toString() }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "All Count Activity (${allEntries.size} logs)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        if (allEntries.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = "No count logs recorded yet.",
                                        modifier = Modifier.padding(24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(allEntries, key = { it.first.id }) { (entry, tracker) ->
                                val trackerColor = Color(tracker.colorHex)
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { entryToEdit = entry },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(trackerColor, CircleShape)
                                            )
                                            Column {
                                                Text(
                                                    text = tracker.name,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                Text(
                                                    text = "${DateUtils.formatToFullDisplay(entry.dateString)}${if (entry.note.isNotBlank()) " • " + entry.note else ""}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (entry.countValue >= 0) "+${entry.countValue}" else "${entry.countValue}",
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = trackerColor
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

    // Dialogs & Bottom Sheet Management
    if (showAddTrackerDialog) {
        AddTrackerDialog(
            onDismiss = { showAddTrackerDialog = false },
            onConfirm = { name, colorHex, category, unit ->
                viewModel.addTracker(name, colorHex, category, unit)
                showAddTrackerDialog = false
            }
        )
    }

    trackerToEdit?.let { tracker ->
        AddTrackerDialog(
            initialTracker = tracker,
            onDismiss = { trackerToEdit = null },
            onConfirm = { name, colorHex, category, unit ->
                viewModel.updateTracker(
                    tracker.copy(
                        name = name,
                        colorHex = colorHex,
                        category = category,
                        unit = unit
                    )
                )
                trackerToEdit = null
            }
        )
    }

    if (showAddCountDialog) {
        AddCountDialog(
            trackers = rawSummaries,
            initialTrackerId = addCountTrackerIdTarget,
            onDismiss = {
                showAddCountDialog = false
                addCountTrackerIdTarget = null
            },
            onConfirm = { trackerId, dateString, countValue, note ->
                viewModel.addCount(trackerId, dateString, countValue, note)
                showAddCountDialog = false
                addCountTrackerIdTarget = null
            }
        )
    }

    entryToEdit?.let { entry ->
        EditCountEntryDialog(
            entry = entry,
            onDismiss = { entryToEdit = null },
            onConfirm = { newCount, newDate, newNote ->
                viewModel.updateCountEntry(entry, newCount, newDate, newNote)
                entryToEdit = null
            },
            onDelete = {
                viewModel.deleteCountEntry(entry.id)
                entryToEdit = null
            }
        )
    }

    selectedSummaryForDetail?.let { summary ->
        TrackerDetailSheet(
            summary = summary,
            onDismiss = { viewModel.selectTracker(null) },
            onAddEntry = {
                addCountTrackerIdTarget = summary.tracker.id
                showAddCountDialog = true
            },
            onEditEntry = { entry ->
                entryToEdit = entry
            },
            onDeleteEntry = { entry ->
                viewModel.deleteCountEntry(entry.id)
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            matrix = matrix,
            summaryText = viewModel.generateExportSummary(matrix),
            csvText = viewModel.generateCsvExport(matrix),
            onDismiss = { showExportDialog = false }
        )
    }
}
