package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrackerSummary
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCountDialog(
    trackers: List<TrackerSummary>,
    initialTrackerId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (trackerId: Long, dateString: String, count: Long, note: String) -> Unit
) {
    var selectedTrackerId by remember {
        mutableStateOf(initialTrackerId ?: trackers.firstOrNull()?.tracker?.id ?: 0L)
    }
    val selectedSummary = trackers.find { it.tracker.id == selectedTrackerId } ?: trackers.firstOrNull()

    var countText by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(DateUtils.todayIso()) }
    var note by remember { mutableStateOf("") }
    var isTrackerDropdownOpen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val todayIso = DateUtils.todayIso()
    val yesterdayIso = DateUtils.yesterdayIso()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Manual Add Daily Count",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tracker Selection Dropdown
                Text(
                    text = "Select Tracker Name",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTrackerDropdownOpen = true }
                            .testTag("tracker_selector_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSummary?.tracker?.name ?: "Select a Tracker",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isTrackerDropdownOpen,
                        onDismissRequest = { isTrackerDropdownOpen = false },
                        modifier = Modifier.fillMaxWidth(0.75f)
                    ) {
                        trackers.forEach { summary ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(summary.tracker.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            "Cumulative: ${summary.cumulativeTotal}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedTrackerId = summary.tracker.id
                                    isTrackerDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Date Selection with Quick Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Date (e.g. 25/08, 26/08, 27/08)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = dateInput == todayIso,
                            onClick = { dateInput = todayIso },
                            label = { Text("Today (${DateUtils.formatToShortDisplay(todayIso)})") }
                        )
                        FilterChip(
                            selected = dateInput == yesterdayIso,
                            onClick = { dateInput = yesterdayIso },
                            label = { Text("Yesterday (${DateUtils.formatToShortDisplay(yesterdayIso)})") }
                        )
                    }

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = {
                            dateInput = it
                            errorMessage = null
                        },
                        label = { Text("Date (DD/MM or YYYY-MM-DD)") },
                        placeholder = { Text("e.g. 25/08 or 2026-08-25") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("count_date_input")
                    )
                }

                // Count Number Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Count Number",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = countText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                                countText = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Enter Count Value (e.g. 100, 300)") },
                        placeholder = { Text("e.g. 100") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("count_value_input")
                    )

                    // Quick Step Preset Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10L, 50L, 100L, 200L, 300L, 500L).forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    val currentVal = countText.toLongOrNull() ?: 0L
                                    countText = (currentVal + preset).toString()
                                },
                                label = { Text("+$preset", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // Note / Memo
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description (Optional)") },
                    placeholder = { Text("e.g. Morning batch, Session count") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val countValue = countText.toLongOrNull()
                    if (selectedTrackerId == 0L) {
                        errorMessage = "Please select a valid tracker."
                        return@Button
                    }
                    if (countValue == null || countValue <= 0) {
                        errorMessage = "Please enter a valid count number greater than 0."
                        return@Button
                    }
                    val resolvedDate = DateUtils.parseAnyInputToIso(dateInput)
                    if (resolvedDate == null) {
                        errorMessage = "Please enter a valid date format (e.g. 25/08 or 2026-08-25)."
                        return@Button
                    }

                    onConfirm(selectedTrackerId, resolvedDate, countValue, note)
                },
                modifier = Modifier.testTag("submit_count_btn")
            ) {
                Text("Add Count Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
