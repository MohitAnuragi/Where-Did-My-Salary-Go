package com.wheredidmysalarygo.wheredidmysalarygo.data.repository

import com.wheredidmysalarygo.wheredidmysalarygo.data.local.dao.MonthlySummaryDao
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity.MonthlySummaryEntity
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.MonthlySummaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MonthlySummaryRepositoryImpl @Inject constructor(
    private val monthlySummaryDao: MonthlySummaryDao
) : MonthlySummaryRepository {

    override fun getMonthlySummary(month: String): Flow<MonthlySummaryEntity?> {
        return monthlySummaryDao.getMonthlySummary(month)
    }

    override fun getAllMonthlySummaries(): Flow<List<MonthlySummaryEntity>> {
        return monthlySummaryDao.getAllMonthlySummaries()
    }

    override suspend fun insertOrUpdateMonthlySummary(summary: MonthlySummaryEntity) {
        monthlySummaryDao.insertMonthlySummary(summary)
    }

    override suspend fun deleteMonthlySummary(month: String) {
        monthlySummaryDao.deleteMonthlySummary(month)
    }
}

