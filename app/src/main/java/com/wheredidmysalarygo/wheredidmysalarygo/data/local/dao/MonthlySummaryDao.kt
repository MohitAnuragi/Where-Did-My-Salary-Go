package com.wheredidmysalarygo.wheredidmysalarygo.data.local.dao

import androidx.room.*
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity.MonthlySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlySummaryDao {

    @Query("SELECT * FROM monthly_summary WHERE month = :month")
    fun getMonthlySummary(month: String): Flow<MonthlySummaryEntity?>

    @Query("SELECT * FROM monthly_summary ORDER BY month DESC")
    fun getAllMonthlySummaries(): Flow<List<MonthlySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlySummary(summary: MonthlySummaryEntity)

    @Update
    suspend fun updateMonthlySummary(summary: MonthlySummaryEntity)

    @Query("DELETE FROM monthly_summary WHERE month = :month")
    suspend fun deleteMonthlySummary(month: String)
}

