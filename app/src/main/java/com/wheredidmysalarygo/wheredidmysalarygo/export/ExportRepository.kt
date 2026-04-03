package com.wheredidmysalarygo.wheredidmysalarygo.export

import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.MonthlySummaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class ExportRepository @Inject constructor(
    private val salaryRepository: SalaryRepository,
    private val expenseRepository: ExpenseRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository
) {

    suspend fun getExportData(numberOfMonths: Int): List<MonthlyExportData> {
        if (numberOfMonths <= 0) {
            return emptyList()
        }

        val allExpenses = expenseRepository.getAllExpenses().first()
        val dateFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
        val monthFormat = DateTimeFormatter.ofPattern("yyyy-MM")


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

