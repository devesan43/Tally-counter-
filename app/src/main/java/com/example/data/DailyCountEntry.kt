package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "count_entries",
    foreignKeys = [
        ForeignKey(
            entity = TrackerEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["trackerId"]),
        Index(value = ["dateString"]),
        Index(value = ["trackerId", "dateString"])
    ]
)
data class DailyCountEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val dateString: String, // format "YYYY-MM-DD" e.g. "2026-08-25"
    val countValue: Long,   // e.g. 100, 300, etc.
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
