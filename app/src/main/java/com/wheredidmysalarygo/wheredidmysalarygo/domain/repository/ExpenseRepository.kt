package com.wheredidmysalarygo.wheredidmysalarygo.domain.repository

import com.wheredidmysalarygo.wheredidmysalarygo.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)
    fun getTotalExpenses(): Flow<Double>
}

