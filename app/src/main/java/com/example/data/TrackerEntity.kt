package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trackers")
data class TrackerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: Long = 0xFF4F46E5, // Default indigo
    val category: String = "General",
    val unit: String = "counts",
    val targetDaily: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
