package com.example.shaalesmarttrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Asset::class,
        Issue::class,
        Repair::class,
        HealthCheck::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
