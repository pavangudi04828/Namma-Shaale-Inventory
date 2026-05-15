package com.example.shaalesmarttrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class Issue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetId: Int,
    val assetName: String,
    val description: String,
    val severity: String,         // "Low" | "Medium" | "High"
    val status: String,           // "Open" | "In Progress" | "Resolved"
    val reportedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)
