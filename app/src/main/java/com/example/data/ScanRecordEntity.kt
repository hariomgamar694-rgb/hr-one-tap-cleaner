package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val junkFoundBytes: Long,
    val junkCleanedBytes: Long,
    val healthScore: Int,
    val itemsCleanedCount: Int,
    val summaryNotes: String
)
