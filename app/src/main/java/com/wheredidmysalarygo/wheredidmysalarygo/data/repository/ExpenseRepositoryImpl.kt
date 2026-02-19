package com.wheredidmysalarygo.wheredidmysalarygo.data.repository

import com.wheredidmysalarygo.wheredidmysalarygo.data.local.dao.ExpenseDao
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity.ExpenseEntity
import com.wheredidmysalarygo.wheredidmysalarygo.domain.model.Expense
import com.wheredidmysalarygo.wheredidmysalarygo.domain.model.ExpenseFrequency
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExpenseById(id: Long): Expense? {
        return expenseDao.getExpenseById(id)?.toDomain()
    }

    override suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
    }

    override suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    override fun getTotalExpenses(): Flow<Double> {
        return expenseDao.getTotalExpensesFlow().map { it ?: 0.0 }
    }

    // Mapper functions: Entity <-> Domain
    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = this.id,
            name = this.name,
            amount = this.amount,
            dueDate = this.dueDate,
            frequency = ExpenseFrequency.valueOf(this.frequency),
            month = this.month
        )
    }

    private fun Expense.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = this.id,
            name = this.name,
            amount = this.amount,
            dueDate = this.dueDate,
            frequency = this.frequency.name,
            month = this.month
        )
    }
}

