package com.example.shaalesmarttrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_checks")
data class HealthCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetId: Int,
    val assetName: String,
    val checkedBy: String,
    val result: String,           // "Pass" | "Fail" | "Needs Attention"
    val notes: String = "",
    val checkedAt: Long = System.currentTimeMillis()
)
