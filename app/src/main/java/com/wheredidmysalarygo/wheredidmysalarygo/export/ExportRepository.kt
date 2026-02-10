package com.wheredidmysalarygo.wheredidmysalarygo.export

import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ExportRepository - Fetches filtered data from Room based on subscription plan
 *
 * Enforces export range limits
 * Works completely offline
 */
class ExportRepository @Inject constructor(
    private val salaryRepository: SalaryRepository,
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Get export data for the allowed number of months
     *
     * @param numberOfMonths How many months to export (based on subscription plan)
     * @return List of monthly export data, filtered by date range
     */
    suspend fun getExportData(numberOfMonths: Int): List<MonthlyExportData> {
        if (numberOfMonths <= 0) {
            return emptyList()
        }

        val salary = salaryRepository.getSalary().first()
        val allExpenses = expenseRepository.getAllExpenses().first()

        // Calculate date range (calendar months)
        val endDate = Calendar.getInstance()
        val startDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -(numberOfMonths - 1))
            set(Calendar.DAY_OF_MONTH, 1) // Start from 1st of month
        }

        // Generate monthly summaries
        val monthlyData = mutableListOf<MonthlyExportData>()
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        val currentCal = Calendar.getInstance().apply {
            timeInMillis = startDate.timeInMillis
        }

        while (currentCal.timeInMillis <= endDate.timeInMillis) {
            val monthString = dateFormat.format(currentCal.time)

            // Calculate totals for this month
            val totalFixed = allExpenses.sumOf { it.amount }
            val remaining = salary - totalFixed

            // Map expenses to export format
            val expenseExports = allExpenses.map { expense ->
                ExpenseExportData(
                    name = expense.name,
                    amount = expense.amount,
                    dueDate = expense.dueDate
                )
            }

            monthlyData.add(
                MonthlyExportData(
                    month = monthString,
                    salary = salary,
                    totalFixedExpenses = totalFixed,
                    remainingAmount = remaining,
                    expenses = expenseExports
                )
            )

            // Move to next month
            currentCal.add(Calendar.MONTH, 1)
        }

        return monthlyData
    }
}

