package com.example.shaalesmarttrack.data.local

import androidx.room.*

@Dao
interface AppDao {

    // ========== ASSETS ==========
    @Query("SELECT * FROM assets ORDER BY name ASC")
    suspend fun getAllAssets(): List<Asset>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: Int): Asset?

    @Query("SELECT * FROM assets WHERE status = :status")
    suspend fun getAssetsByStatus(status: String): List<Asset>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: Asset): Long

    @Update
    suspend fun updateAsset(asset: Asset)

    @Delete
    suspend fun deleteAsset(asset: Asset)

    @Query("SELECT COUNT(*) FROM assets")
    suspend fun getTotalAssets(): Int

    @Query("SELECT COUNT(*) FROM assets WHERE status = 'Working'")
    suspend fun getWorkingCount(): Int

    @Query("SELECT COUNT(*) FROM assets WHERE status = 'Needs Repair'")
    suspend fun getNeedsRepairCount(): Int

    @Query("SELECT COUNT(*) FROM assets WHERE status = 'Damaged'")
    suspend fun getDamagedCount(): Int

    // ========== ISSUES ==========
    @Query("SELECT * FROM issues ORDER BY reportedAt DESC")
    suspend fun getAllIssues(): List<Issue>

    @Query("SELECT * FROM issues WHERE status = 'Open' ORDER BY reportedAt DESC")
    suspend fun getOpenIssues(): List<Issue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: Issue): Long

    @Update
    suspend fun updateIssue(issue: Issue)

    @Delete
    suspend fun deleteIssue(issue: Issue)

    @Query("SELECT COUNT(*) FROM issues WHERE status = 'Open'")
    suspend fun getOpenIssueCount(): Int

    // ========== REPAIRS ==========
    @Query("SELECT * FROM repairs ORDER BY startedAt DESC")
    suspend fun getAllRepairs(): List<Repair>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepair(repair: Repair): Long

    @Update
    suspend fun updateRepair(repair: Repair)

    @Delete
    suspend fun deleteRepair(repair: Repair)

    @Query("SELECT COUNT(*) FROM repairs WHERE status != 'Completed'")
    suspend fun getActiveRepairCount(): Int

    // ========== HEALTH CHECKS ==========
    @Query("SELECT * FROM health_checks ORDER BY checkedAt DESC")
    suspend fun getAllHealthChecks(): List<HealthCheck>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthCheck(check: HealthCheck): Long

    @Delete
    suspend fun deleteHealthCheck(check: HealthCheck)

    @Query("SELECT COUNT(*) FROM health_checks WHERE result = 'Pass'")
    suspend fun getPassCount(): Int

    @Query("SELECT COUNT(*) FROM health_checks WHERE result = 'Fail'")
    suspend fun getFailCount(): Int
}
