package com.wheredidmysalarygo.wheredidmysalarygo.data.repository

import com.wheredidmysalarygo.wheredidmysalarygo.data.local.UserPreferencesManager
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SalaryRepositoryImpl @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : SalaryRepository {

    override fun getSalary(): Flow<Double> {
        return userPreferencesManager.salaryFlow
    }

    override suspend fun setSalary(amount: Double) {
        userPreferencesManager.setSalary(amount)
    }

    override fun getSalaryCreditDate(): Flow<Int?> {
        return userPreferencesManager.salaryCreditDateFlow
    }

    override suspend fun setSalaryCreditDate(date: Int?) {
        userPreferencesManager.setSalaryCreditDate(date)
    }

    override fun getCountryCode(): Flow<String> {
        return userPreferencesManager.countryCodeFlow
    }

    override suspend fun setCountryCode(countryCode: String) {
        userPreferencesManager.setCountryCode(countryCode)
    }
}

