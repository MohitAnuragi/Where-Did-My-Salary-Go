package com.wheredidmysalarygo.wheredidmysalarygo.domain.repository

import kotlinx.coroutines.flow.Flow

interface SalaryRepository {
    fun getSalary(): Flow<Double>
    suspend fun setSalary(amount: Double)
    fun getSalaryCreditDate(): Flow<Int?>
    suspend fun setSalaryCreditDate(date: Int?)
    fun getCountryCode(): Flow<String>
    suspend fun setCountryCode(countryCode: String)
}

