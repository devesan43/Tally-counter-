package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TrackerEntity::class, DailyCountEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao
    abstract fun countEntryDao(): CountEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "count_tracker_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default demo data
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).seedInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        if (trackerDao().getTrackerCount() == 0) {
            val t1Id = trackerDao().insertTracker(
                TrackerEntity(
                    name = "Name-1",
                    colorHex = 0xFF4F46E5, // Indigo
                    category = "Primary",
                    unit = "units"
                )
            )
            val t2Id = trackerDao().insertTracker(
                TrackerEntity(
                    name = "Name-2",
                    colorHex = 0xFF0D9488, // Teal
                    category = "Primary",
                    unit = "units"
                )
            )

            // Seed user's exact example entries:
            // Name-1: 25/08: 100, 26/08: 300, 27/08: 100 (cumulative 500)
            // Name-2: 25/08: 200, 26/08: 100 (cumulative 300)
            val entries = listOf(
                DailyCountEntry(
                    trackerId = t1Id,
                    dateString = "2026-08-25",
                    countValue = 100,
                    note = "Initial entry"
                ),
                DailyCountEntry(
                    trackerId = t1Id,
                    dateString = "2026-08-26",
                    countValue = 300,
                    note = "Peak daily progress"
                ),
                DailyCountEntry(
                    trackerId = t1Id,
                    dateString = "2026-08-27",
                    countValue = 100,
                    note = "Follow-up count"
                ),
                DailyCountEntry(
                    trackerId = t2Id,
                    dateString = "2026-08-25",
                    countValue = 200,
                    note = "Opening count"
                ),
                DailyCountEntry(
                    trackerId = t2Id,
                    dateString = "2026-08-26",
                    countValue = 100,
                    note = "Midweek session"
                )
            )
            countEntryDao().insertAll(entries)
        }
    }
}
