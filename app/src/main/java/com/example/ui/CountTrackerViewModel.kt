package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ComparisonMatrix
import com.example.data.DailyCountEntry
import com.example.data.TrackerEntity
import com.example.data.TrackerRepository
import com.example.data.TrackerSummary
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiNotification(
    val message: String,
    val actionLabel: String? = null
)

class CountTrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = TrackerRepository(database.trackerDao(), database.countEntryDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedTrackerId = MutableStateFlow<Long?>(null)
    val selectedTrackerId: StateFlow<Long?> = _selectedTrackerId.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    val rawSummaries: StateFlow<List<TrackerSummary>> = repository.trackerSummaries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredSummaries: StateFlow<List<TrackerSummary>> = combine(
        repository.trackerSummaries,
        _searchQuery,
        _selectedCategory
    ) { summaries, query, cat ->
        summaries.filter { summary ->
            val matchesQuery = query.isBlank() || summary.tracker.name.contains(query, ignoreCase = true)
            val matchesCat = cat == "All" || summary.tracker.category.equals(cat, ignoreCase = true)
            matchesQuery && matchesCat
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val comparisonMatrix: StateFlow<ComparisonMatrix> = repository.comparisonMatrix
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ComparisonMatrix(emptyList(), emptyList(), emptyMap(), 0L)
        )

    init {
        viewModelScope.launch {
            database.seedInitialData()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectTracker(id: Long?) {
        _selectedTrackerId.value = id
    }

    fun clearNotification() {
        _notification.value = null
    }

    fun addTracker(
        name: String,
        colorHex: Long = 0xFF4F46E5,
        category: String = "General",
        unit: String = "units"
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addTracker(name, colorHex, category, unit)
            _notification.value = UiNotification("Tracker '$name' created")
        }
    }

    fun updateTracker(tracker: TrackerEntity) {
        viewModelScope.launch {
            repository.updateTracker(tracker)
            _notification.value = UiNotification("Tracker updated")
        }
    }

    fun deleteTracker(tracker: TrackerEntity) {
        viewModelScope.launch {
            repository.deleteTracker(tracker)
            if (_selectedTrackerId.value == tracker.id) {
                _selectedTrackerId.value = null
            }
            _notification.value = UiNotification("Tracker '${tracker.name}' deleted")
        }
    }

    fun addCount(
        trackerId: Long,
        dateInput: String,
        countValue: Long,
        note: String = ""
    ) {
        val isoDate = DateUtils.parseAnyInputToIso(dateInput) ?: DateUtils.todayIso()
        viewModelScope.launch {
            repository.addCountEntry(
                trackerId = trackerId,
                dateString = isoDate,
                countValue = countValue,
                note = note
            )
            _notification.value = UiNotification("Added $countValue for ${DateUtils.formatToShortDisplay(isoDate)}")
        }
    }

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    fun toggleHaptic() {
        _hapticEnabled.value = !_hapticEnabled.value
    }

    fun tallyClick(trackerId: Long, dateString: String, delta: Long) {
        viewModelScope.launch {
            repository.quickIncrement(trackerId, delta, dateString)
            val prefix = if (delta > 0) "+$delta" else "$delta"
            _notification.value = UiNotification("$prefix recorded for ${DateUtils.formatToShortDisplay(dateString)}")
        }
    }

    fun resetCountForDate(trackerId: Long, dateString: String) {
        viewModelScope.launch {
            repository.resetCountsForDate(trackerId, dateString)
            _notification.value = UiNotification("Reset count for ${DateUtils.formatToShortDisplay(dateString)}")
        }
    }

    fun setCountForDate(trackerId: Long, dateString: String, targetCount: Long) {
        viewModelScope.launch {
            repository.setCountForDate(trackerId, dateString, targetCount)
            _notification.value = UiNotification("Set count to $targetCount for ${DateUtils.formatToShortDisplay(dateString)}")
        }
    }

    fun quickIncrement(trackerId: Long, delta: Long) {
        viewModelScope.launch {
            repository.quickIncrement(trackerId, delta)
            _notification.value = UiNotification("+$delta added to today's count")
        }
    }

    fun updateCountEntry(entry: DailyCountEntry, newCount: Long, newDate: String, newNote: String) {
        val isoDate = DateUtils.parseAnyInputToIso(newDate) ?: entry.dateString
        viewModelScope.launch {
            repository.updateCountEntry(
                entry.copy(
                    countValue = newCount,
                    dateString = isoDate,
                    note = newNote
                )
            )
            _notification.value = UiNotification("Entry updated")
        }
    }

    fun deleteCountEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteCountEntry(entryId)
            _notification.value = UiNotification("Entry deleted")
        }
    }

    fun clearTrackerHistory(trackerId: Long) {
        viewModelScope.launch {
            repository.clearAllCountsForTracker(trackerId)
            _notification.value = UiNotification("Count history cleared")
        }
    }

    fun generateExportSummary(matrix: ComparisonMatrix): String {
        val sb = StringBuilder()
        sb.appendLine("📊 COUNT TRACKER SUMMARY")
        sb.appendLine("Generated: ${DateUtils.formatToPrettyDisplay(DateUtils.todayIso())}")
        sb.appendLine("===============================")
        sb.appendLine()

        matrix.rows.forEach { row ->
            sb.appendLine("👉 ${row.tracker.name.uppercase()}:")
            matrix.dates.forEach { date ->
                val count = row.countsByDate[date] ?: 0L
                if (count > 0) {
                    sb.appendLine("   • ${DateUtils.formatToShortDisplay(date)}: $count")
                }
            }
            sb.appendLine("   ▶ Cumulative Total: ${row.cumulativeTotal}")
            sb.appendLine()
        }

        sb.appendLine("===============================")
        sb.appendLine("GRAND CUMULATIVE TOTAL: ${matrix.grandTotal}")
        return sb.toString()
    }

    fun generateCsvExport(matrix: ComparisonMatrix): String {
        val sb = StringBuilder()
        val dateHeaders = matrix.dates.map { DateUtils.formatToShortDisplay(it) }
        sb.append("Tracker Name,").append(dateHeaders.joinToString(",")).appendLine(",Cumulative Total")

        matrix.rows.forEach { row ->
            val dateValues = matrix.dates.map { (row.countsByDate[it] ?: 0L).toString() }
            sb.append("\"${row.tracker.name}\",")
                .append(dateValues.joinToString(","))
                .appendLine(",${row.cumulativeTotal}")
        }

        val totalDailyValues = matrix.dates.map { (matrix.dailyTotals[it] ?: 0L).toString() }
        sb.append("\"DAILY TOTAL\",")
            .append(totalDailyValues.joinToString(","))
            .appendLine(",${matrix.grandTotal}")

        return sb.toString()
    }
}
