package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.DailyCountEntry
import com.example.util.DateUtils

@Composable
fun EditCountEntryDialog(
    entry: DailyCountEntry,
    onDismiss: () -> Unit,
    onConfirm: (newCount: Long, newDate: String, newNote: String) -> Unit,
    onDelete: () -> Unit
) {
    var countText by remember { mutableStateOf(entry.countValue.toString()) }
    var dateInput by remember { mutableStateOf(DateUtils.formatToShortDisplay(entry.dateString)) }
    var note by remember { mutableStateOf(entry.note) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Count Entry", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = countText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                            countText = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Count Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateInput,
                    onValueChange = {
                        dateInput = it
                        errorMessage = null
                    },
                    label = { Text("Date (DD/MM or YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description") },
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_entry_btn")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = {
                        val countVal = countText.toLongOrNull()
                        if (countVal == null || countVal <= 0) {
                            errorMessage = "Please enter a valid count number."
                            return@Button
                        }
                        val resolvedDate = DateUtils.parseAnyInputToIso(dateInput)
                        if (resolvedDate == null) {
                            errorMessage = "Invalid date format."
                            return@Button
                        }
                        onConfirm(countVal, resolvedDate, note)
                    },
                    modifier = Modifier.testTag("save_edit_entry_btn")
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
