package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrackerEntity
import com.example.data.TrackerSummary
import com.example.ui.CountTrackerViewModel
import com.example.util.DateUtils
import com.example.util.SoundHelper
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyClickerScreen(
    summaries: List<TrackerSummary>,
    viewModel: CountTrackerViewModel,
    onAddNewTracker: () -> Unit,
    onOpenExport: () -> Unit,
    soundEnabled: Boolean,
    hapticEnabled: Boolean
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Active tracker selection
    var selectedTrackerId by remember(summaries) {
        mutableStateOf(summaries.firstOrNull()?.tracker?.id ?: 1L)
    }

    // Ensure selectedTrackerId points to a valid tracker if available
    val activeSummary = summaries.find { it.tracker.id == selectedTrackerId }
        ?: summaries.firstOrNull()

    // Selected date for tally counting
    var selectedDate by remember { mutableStateOf(DateUtils.todayIso()) }

    // Step size (+1, +5, +10, +50, +100)
    var stepSize by remember { mutableLongStateOf(1L) }

    // Dialog state for manual count input
    var showManualSetDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Button press scale animation
    val scaleAnim = remember { Animatable(1f) }

    // Count for selected date and cumulative count
    val dateCount = activeSummary?.dailyAggregates?.find { it.dateString == selectedDate }?.totalCount ?: 0L
    val cumulativeCount = activeSummary?.cumulativeTotal ?: 0L
    val trackerColor = activeSummary?.let { Color(it.tracker.colorHex) } ?: MaterialTheme.colorScheme.primary

    // Function to trigger click feedback & action
    fun performTally(delta: Long) {
        if (activeSummary == null) return
        coroutineScope.launch {
            scaleAnim.animateTo(
                0.92f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh)
            )
            scaleAnim.animateTo(
                1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
        if (hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (soundEnabled) {
            SoundHelper.playClick()
        }
        viewModel.tallyClick(activeSummary.tracker.id, selectedDate, delta)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Tracker Switcher Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT COUNTER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleSound() },
                            modifier = Modifier.size(32.dp).testTag("tally_sound_toggle")
                        ) {
                            Icon(
                                imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Toggle Sound",
                                tint = if (soundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleHaptic() },
                            modifier = Modifier.size(32.dp).testTag("tally_haptic_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Toggle Haptic",
                                tint = if (hapticEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenExport,
                            modifier = Modifier.size(32.dp).testTag("tally_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export Matrix",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Horizontal scroll list of all tracker names (Name-1, Name-2, etc.)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(summaries, key = { it.tracker.id }) { item ->
                        val isSelected = item.tracker.id == activeSummary?.tracker?.id
                        val itemColor = Color(item.tracker.colorHex)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTrackerId = item.tracker.id },
                            label = {
                                Text(
                                    text = item.tracker.name,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(itemColor, CircleShape)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = itemColor.copy(alpha = 0.18f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = if (isSelected) BorderStroke(1.8.dp, itemColor) else null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("tally_chip_${item.tracker.name}")
                        )
                    }

                    item {
                        FilledTonalButton(
                            onClick = onAddNewTracker,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("tally_add_name_chip"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Name", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Date Selector Pills (Today, Yesterday, Pick Date)
        item {
            val today = DateUtils.todayIso()
            val yesterday = DateUtils.yesterdayIso()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today chip
                FilterChip(
                    selected = selectedDate == today,
                    onClick = { selectedDate = today },
                    label = { Text("Today (${DateUtils.formatToShortDisplay(today)})") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("date_chip_today")
                )

                // Yesterday chip
                FilterChip(
                    selected = selectedDate == yesterday,
                    onClick = { selectedDate = yesterday },
                    label = { Text("Yesterday") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("date_chip_yesterday")
                )

                // Custom date picker chip
                val isCustomDate = selectedDate != today && selectedDate != yesterday
                FilterChip(
                    selected = isCustomDate,
                    onClick = {
                        val cal = Calendar.getInstance()
                        val parsed = DateUtils.parseIsoToCalendar(selectedDate)
                        val year = parsed?.get(Calendar.YEAR) ?: cal.get(Calendar.YEAR)
                        val month = parsed?.get(Calendar.MONTH) ?: cal.get(Calendar.MONTH)
                        val day = parsed?.get(Calendar.DAY_OF_MONTH) ?: cal.get(Calendar.DAY_OF_MONTH)

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                selectedDate = DateUtils.formatFromYMD(y, m + 1, d)
                            },
                            year,
                            month,
                            day
                        ).show()
                    },
                    label = {
                        Text(
                            text = if (isCustomDate) DateUtils.formatToShortDisplay(selectedDate) else "Pick Date..."
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("date_chip_custom")
                )
            }
        }

        // The Main Tactile Tally Counter Unit
        item {
            if (activeSummary != null) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header info: Active Name & Units
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
                                        .background(trackerColor, CircleShape)
                                )
                                Text(
                                    text = activeSummary.tracker.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Surface(
                                color = trackerColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = activeSummary.tracker.unit.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = trackerColor
                                )
                            }
                        }

                        // Digital Display Bezel (Odometer / LED look)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF1E293B),
                                            Color(0xFF0F172A)
                                        )
                                    )
                                )
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${DateUtils.formatToPrettyDisplay(selectedDate).uppercase()} COUNT",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 1.sp
                                    )

                                    Text(
                                        text = "CUMULATIVE",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 1.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    // Main date count with flip transition
                                    AnimatedContent(
                                        targetState = dateCount,
                                        transitionSpec = {
                                            if (targetState > initialState) {
                                                (slideInVertically { height -> height } + fadeIn())
                                                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                                            } else {
                                                (slideInVertically { height -> -height } + fadeIn())
                                                    .togetherWith(slideOutVertically { height -> height } + fadeOut())
                                            }
                                        },
                                        label = "date_count_anim"
                                    ) { targetCount ->
                                        Text(
                                            text = "$targetCount",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 44.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF38BDF8) // Bright Cyan LED
                                        )
                                    }

                                    // Cumulative total
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$cumulativeCount",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4ADE80) // Emerald Green LED
                                        )
                                        Text(
                                            text = "All-time total",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }

                        // Visual Tally Marks (groups of 5 strokes |||| / )
                        if (dateCount > 0) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    TallyMarksVisualizer(
                                        count = dateCount,
                                        color = trackerColor
                                    )
                                    Text(
                                        text = "$dateCount tally mark${if (dateCount == 1L) "" else "s"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // GIANT TACTILE CLICKER PAD
                        Box(
                            modifier = Modifier
                                .size(175.dp)
                                .scale(scaleAnim.value)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    ambientColor = trackerColor,
                                    spotColor = trackerColor
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            trackerColor.copy(alpha = 0.95f),
                                            trackerColor
                                        )
                                    )
                                )
                                .clickable {
                                    performTally(stepSize)
                                }
                                .testTag("tally_main_clicker_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Count +$stepSize",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "+$stepSize",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "TAP TO COUNT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Step Selector Chips (+1, +5, +10, +50, +100)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "STEP INCREMENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                            ) {
                                listOf(1L, 5L, 10L, 50L, 100L).forEach { step ->
                                    val isSelected = stepSize == step
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { stepSize = step },
                                        label = {
                                            Text(
                                                text = "+$step",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("step_chip_$step")
                                    )
                                }
                            }
                        }

                        // Action Controls: Decrement (-step), Reset Date (↺), Manual Set (✏️)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decrement
                            OutlinedButton(
                                onClick = { performTally(-stepSize) },
                                shape = RoundedCornerShape(12.dp),
                                enabled = dateCount > 0,
                                modifier = Modifier.testTag("tally_decrement_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Subtract $stepSize",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("-$stepSize", fontWeight = FontWeight.Bold)
                            }

                            // Manual Exact Value Input
                            FilledTonalButton(
                                onClick = { showManualSetDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("tally_set_exact_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Set Value",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set Value", fontWeight = FontWeight.Bold)
                            }

                            // Reset Date Count
                            OutlinedIconButton(
                                onClick = { showResetConfirmDialog = true },
                                modifier = Modifier.testTag("tally_reset_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Date Count",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // Multi-Tracker Quick Clickers Section (Name-1, Name-2, etc. at a glance)
        if (summaries.size > 1) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ALL COUNTERS (${summaries.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    summaries.forEach { item ->
                        val itemColor = Color(item.tracker.colorHex)
                        val itemDateCount = item.dailyAggregates.find { it.dateString == selectedDate }?.totalCount ?: 0L

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTrackerId = item.tracker.id },
                            shape = RoundedCornerShape(16.dp),
                            border = if (item.tracker.id == activeSummary?.tracker?.id)
                                BorderStroke(2.dp, itemColor)
                            else
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(itemColor, CircleShape)
                                    )
                                    Column {
                                        Text(
                                            text = item.tracker.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Today: ${item.todayTotal} • Total: ${item.cumulativeTotal}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = itemColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "$itemDateCount",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black,
                                            color = itemColor,
                                            fontSize = 16.sp
                                        )
                                    }

                                    // Direct +1 and +10 quick clickers
                                    FilledTonalIconButton(
                                        onClick = {
                                            if (soundEnabled) SoundHelper.playClick()
                                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.tallyClick(item.tracker.id, selectedDate, 1)
                                        },
                                        modifier = Modifier.size(34.dp).testTag("quick_plus1_${item.tracker.name}")
                                    ) {
                                        Text("+1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    FilledTonalIconButton(
                                        onClick = {
                                            if (soundEnabled) SoundHelper.playClick()
                                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.tallyClick(item.tracker.id, selectedDate, 10)
                                        },
                                        modifier = Modifier.size(34.dp).testTag("quick_plus10_${item.tracker.name}")
                                    ) {
                                        Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Set Count Dialog
    if (showManualSetDialog && activeSummary != null) {
        var inputCountText by remember { mutableStateOf(dateCount.toString()) }
        AlertDialog(
            onDismissRequest = { showManualSetDialog = false },
            title = {
                Text("Set Exact Count", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the target count for ${activeSummary.tracker.name} on ${DateUtils.formatToShortDisplay(selectedDate)}:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = inputCountText,
                        onValueChange = { inputCountText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_count_input"),
                        label = { Text("Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputCountText.toLongOrNull() ?: 0L
                        viewModel.setCountForDate(activeSummary.tracker.id, selectedDate, parsed)
                        showManualSetDialog = false
                    },
                    modifier = Modifier.testTag("manual_count_confirm_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualSetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog && activeSummary != null) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Reset Date Count?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will reset count for ${activeSummary.tracker.name} on ${DateUtils.formatToPrettyDisplay(selectedDate)} back to 0."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCountForDate(activeSummary.tracker.id, selectedDate)
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("reset_confirm_btn")
                ) {
                    Text("Reset to 0")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
