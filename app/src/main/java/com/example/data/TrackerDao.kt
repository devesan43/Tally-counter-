package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Query("SELECT * FROM trackers ORDER BY createdAt ASC")
    fun getAllTrackers(): Flow<List<TrackerEntity>>

    @Query("SELECT * FROM trackers WHERE id = :id")
    suspend fun getTrackerById(id: Long): TrackerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: TrackerEntity): Long

    @Update
    suspend fun updateTracker(tracker: TrackerEntity)

    @Delete
    suspend fun deleteTracker(tracker: TrackerEntity)

    @Query("DELETE FROM trackers WHERE id = :id")
    suspend fun deleteTrackerById(id: Long)

    @Query("SELECT COUNT(*) FROM trackers")
    suspend fun getTrackerCount(): Int
}
