package com.example.shaalesmarttrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repairs")
data class Repair(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetId: Int,
    val assetName: String,
    val description: String,
    val status: String,           // "Pending" | "In Progress" | "Completed"
    val cost: Double = 0.0,
    val technicianName: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
