package com.example.data

import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DailyAggregate(
    val dateString: String,
    val totalCount: Long,
    val entries: List<DailyCountEntry>
)

data class TrackerSummary(
    val tracker: TrackerEntity,
    val cumulativeTotal: Long,
    val todayTotal: Long,
    val yesterdayTotal: Long,
    val dailyAggregates: List<DailyAggregate>,
    val allEntries: List<DailyCountEntry>
)

data class MatrixRow(
    val tracker: TrackerEntity,
    val countsByDate: Map<String, Long>,
    val cumulativeTotal: Long
)

data class ComparisonMatrix(
    val dates: List<String>,
    val rows: List<MatrixRow>,
    val dailyTotals: Map<String, Long>,
    val grandTotal: Long
)

class TrackerRepository(
    private val trackerDao: TrackerDao,
    private val countEntryDao: CountEntryDao
) {
    val allTrackers: Flow<List<TrackerEntity>> = trackerDao.getAllTrackers()
    val allEntries: Flow<List<DailyCountEntry>> = countEntryDao.getAllEntries()

    val trackerSummaries: Flow<List<TrackerSummary>> = combine(
        trackerDao.getAllTrackers(),
        countEntryDao.getAllEntries()
    ) { trackers, entries ->
        val today = DateUtils.todayIso()
        val yesterday = DateUtils.yesterdayIso()

        val entriesByTracker = entries.groupBy { it.trackerId }

        trackers.map { tracker ->
            val trackerEntries = entriesByTracker[tracker.id] ?: emptyList()
            val cumulativeTotal = trackerEntries.sumOf { it.countValue }
            val todayTotal = trackerEntries.filter { it.dateString == today }.sumOf { it.countValue }
            val yesterdayTotal = trackerEntries.filter { it.dateString == yesterday }.sumOf { it.countValue }

            val dailyAggregates = trackerEntries
                .groupBy { it.dateString }
                .map { (date, dEntries) ->
                    DailyAggregate(
                        dateString = date,
                        totalCount = dEntries.sumOf { it.countValue },
                        entries = dEntries
                    )
                }
                .sortedByDescending { it.dateString }

            TrackerSummary(
                tracker = tracker,
                cumulativeTotal = cumulativeTotal,
                todayTotal = todayTotal,
                yesterdayTotal = yesterdayTotal,
                dailyAggregates = dailyAggregates,
                allEntries = trackerEntries
            )
        }
    }

    val comparisonMatrix: Flow<ComparisonMatrix> = combine(
        trackerDao.getAllTrackers(),
        countEntryDao.getAllEntries()
    ) { trackers, entries ->
        // Distinct dates sorted chronologically
        val distinctDates = entries.map { it.dateString }.distinct().sorted()
        val matrixRows = mutableListOf<MatrixRow>()
        val dailyTotals = mutableMapOf<String, Long>()
        var grandTotal = 0L

        trackers.forEach { tracker ->
            val trackerEntries = entries.filter { it.trackerId == tracker.id }
            val countsByDate = trackerEntries.groupBy { it.dateString }
                .mapValues { (_, dateEntries) -> dateEntries.sumOf { it.countValue } }
            val cumulative = trackerEntries.sumOf { it.countValue }
            grandTotal += cumulative

            matrixRows.add(
                MatrixRow(
                    tracker = tracker,
                    countsByDate = countsByDate,
                    cumulativeTotal = cumulative
                )
            )
        }

        distinctDates.forEach { date ->
            dailyTotals[date] = entries.filter { it.dateString == date }.sumOf { it.countValue }
        }

        ComparisonMatrix(
            dates = distinctDates,
            rows = matrixRows,
            dailyTotals = dailyTotals,
            grandTotal = grandTotal
        )
    }

    suspend fun addTracker(
        name: String,
        colorHex: Long = 0xFF4F46E5,
        category: String = "General",
        unit: String = "units",
        targetDaily: Long = 0
    ): Long {
        return trackerDao.insertTracker(
            TrackerEntity(
                name = name.trim(),
                colorHex = colorHex,
                category = category.trim().ifEmpty { "General" },
                unit = unit.trim().ifEmpty { "units" },
                targetDaily = targetDaily
            )
        )
    }

    suspend fun updateTracker(tracker: TrackerEntity) {
        trackerDao.updateTracker(tracker)
    }

    suspend fun deleteTracker(tracker: TrackerEntity) {
        trackerDao.deleteTracker(tracker)
    }

    suspend fun addCountEntry(
        trackerId: Long,
        dateString: String,
        countValue: Long,
        note: String = ""
    ): Long {
        return countEntryDao.insertEntry(
            DailyCountEntry(
                trackerId = trackerId,
                dateString = dateString,
                countValue = countValue,
                note = note.trim()
            )
        )
    }

    suspend fun updateCountEntry(entry: DailyCountEntry) {
        countEntryDao.updateEntry(entry)
    }

    suspend fun deleteCountEntry(entryId: Long) {
        countEntryDao.deleteEntryById(entryId)
    }

    suspend fun quickIncrement(trackerId: Long, delta: Long, dateString: String = DateUtils.todayIso()) {
        addCountEntry(
            trackerId = trackerId,
            dateString = dateString,
            countValue = delta,
            note = if (delta > 0) "+$delta" else "$delta"
        )
    }

    suspend fun resetCountsForDate(trackerId: Long, dateString: String) {
        countEntryDao.deleteEntriesForTrackerAndDate(trackerId, dateString)
    }

    suspend fun setCountForDate(trackerId: Long, dateString: String, targetCount: Long) {
        countEntryDao.deleteEntriesForTrackerAndDate(trackerId, dateString)
        if (targetCount != 0L) {
            addCountEntry(
                trackerId = trackerId,
                dateString = dateString,
                countValue = targetCount,
                note = "Set to $targetCount"
            )
        }
    }

    suspend fun clearAllCountsForTracker(trackerId: Long) {
        countEntryDao.deleteAllEntriesForTracker(trackerId)
    }
}
