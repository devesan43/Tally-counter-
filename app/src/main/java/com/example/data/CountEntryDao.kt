package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CountEntryDao {
    @Query("SELECT * FROM count_entries ORDER BY dateString DESC, timestamp DESC")
    fun getAllEntries(): Flow<List<DailyCountEntry>>

    @Query("SELECT * FROM count_entries WHERE trackerId = :trackerId ORDER BY dateString DESC, timestamp DESC")
    fun getEntriesForTracker(trackerId: Long): Flow<List<DailyCountEntry>>

    @Query("SELECT * FROM count_entries WHERE trackerId = :trackerId AND dateString = :dateString ORDER BY timestamp DESC")
    fun getEntriesForTrackerAndDate(trackerId: Long, dateString: String): Flow<List<DailyCountEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DailyCountEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DailyCountEntry>)

    @Update
    suspend fun updateEntry(entry: DailyCountEntry)

    @Delete
    suspend fun deleteEntry(entry: DailyCountEntry)

    @Query("DELETE FROM count_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long)

    @Query("DELETE FROM count_entries WHERE trackerId = :trackerId")
    suspend fun deleteAllEntriesForTracker(trackerId: Long)

    @Query("DELETE FROM count_entries WHERE trackerId = :trackerId AND dateString = :dateString")
    suspend fun deleteEntriesForTrackerAndDate(trackerId: Long, dateString: String)

    @Query("SELECT COALESCE(SUM(countValue), 0) FROM count_entries WHERE trackerId = :trackerId")
    fun getCumulativeCount(trackerId: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(countValue), 0) FROM count_entries WHERE trackerId = :trackerId AND dateString = :dateString")
    fun getDailyCount(trackerId: Long, dateString: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(countValue), 0) FROM count_entries")
    fun getGrandTotalCount(): Flow<Long>
}
