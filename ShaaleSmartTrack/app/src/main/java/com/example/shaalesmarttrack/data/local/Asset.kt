package com.example.shaalesmarttrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,         // e.g. "Computer", "Projector", "Furniture"
    val assetTag: String,         // unique identifier label
    val location: String,         // e.g. "Class 5-A"
    val status: String,           // "Working" | "Needs Repair" | "Damaged" | "Missing"
    val condition: String,        // "Good" | "Fair" | "Poor"
    val lastChecked: Long = System.currentTimeMillis(),
    val notes: String = ""
)
