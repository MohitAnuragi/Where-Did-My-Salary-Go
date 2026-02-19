package com.wheredidmysalarygo.wheredidmysalarygo.export

import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.MonthlySummaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ExportRepository - Fetches filtered data from Room based on subscription plan
 *
 * Enforces export range limits
 * Works completely offline
 */
class ExportRepository @Inject constructor(
    private val salaryRepository: SalaryRepository,
    private val expenseRepository: ExpenseRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository
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

        val allExpenses = expenseRepository.getAllExpenses().first()
        val dateFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
        val monthFormat = DateTimeFormatter.ofPattern("yyyy-MM")

        // Get the list of months to export
        val monthsToExport = mutableListOf<String>()
        var currentMonth = YearMonth.now()

        for (i in 0 until numberOfMonths) {
            monthsToExport.add(currentMonth.format(monthFormat))
            currentMonth = currentMonth.minusMonths(1)
        }

        // Build monthly data
        val monthlyData = mutableListOf<MonthlyExportData>()

        for (month in monthsToExport) {
            val monthSummary = monthlySummaryRepository.getMonthlySummary(month).first()

            // If month doesn't exist, skip it
            if (monthSummary == null) continue

            val monthExpenses = allExpenses.filter { it.month == month }

            val expenseExports = monthExpenses.map { expense ->
                ExpenseExportData(
                    name = expense.name,
                    amount = expense.amount,
                    dueDate = expense.dueDate
                )
            }

            val yearMonth = YearMonth.parse(month, monthFormat)
            monthlyData.add(
                MonthlyExportData(
                    month = yearMonth.format(dateFormat),
                    salary = monthSummary.salaryAmount,
                    totalFixedExpenses = monthSummary.totalFixedExpenses,
                    remainingAmount = monthSummary.remainingAmount,
                    expenses = expenseExports
                )
            )
        }

        return monthlyData
    }
}

