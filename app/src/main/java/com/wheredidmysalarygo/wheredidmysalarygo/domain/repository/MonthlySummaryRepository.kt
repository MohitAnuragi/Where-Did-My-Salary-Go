package com.wheredidmysalarygo.wheredidmysalarygo.domain.repository

import com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity.MonthlySummaryEntity
import kotlinx.coroutines.flow.Flow

interface MonthlySummaryRepository {
    fun getMonthlySummary(month: String): Flow<MonthlySummaryEntity?>
    fun getAllMonthlySummaries(): Flow<List<MonthlySummaryEntity>>
    suspend fun insertOrUpdateMonthlySummary(summary: MonthlySummaryEntity)
    suspend fun deleteMonthlySummary(month: String)
}

