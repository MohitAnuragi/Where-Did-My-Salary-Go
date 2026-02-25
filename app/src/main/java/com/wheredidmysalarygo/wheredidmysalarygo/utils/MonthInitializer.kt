package com.wheredidmysalarygo.wheredidmysalarygo.utils

import android.annotation.SuppressLint
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity.MonthlySummaryEntity
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.MonthlySummaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MonthInitializer @Inject constructor(
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val salaryRepository: SalaryRepository,
    private val expenseRepository: ExpenseRepository
) {


    suspend fun initializeCurrentMonth() {
        val currentMonth = getCurrentMonth()
        val existingSummary = monthlySummaryRepository.getMonthlySummary(currentMonth).first()

        if (existingSummary == null) {
            // Get base salary from preferences
            val baseSalary = salaryRepository.getSalary().first()

            // Create new month record
            val newMonthlySummary = MonthlySummaryEntity(
                month = currentMonth,
                salaryAmount = baseSalary,
                totalFixedExpenses = 0.0,
                remainingAmount = baseSalary
            )

            monthlySummaryRepository.insertOrUpdateMonthlySummary(newMonthlySummary)
        }
    }


    suspend fun updateMonthlySummary(month: String) {
        val summary = monthlySummaryRepository.getMonthlySummary(month).first() ?: return
        val expenses = expenseRepository.getAllExpenses().first()

        // Filter expenses for this month
        val monthExpenses = expenses.filter { it.month == month }
        val totalFixed = monthExpenses.sumOf { it.amount }
        val remaining = summary.salaryAmount - totalFixed

        val updatedSummary = summary.copy(
            totalFixedExpenses = totalFixed,
            remainingAmount = remaining
        )

        monthlySummaryRepository.insertOrUpdateMonthlySummary(updatedSummary)
    }

    companion object {
        @SuppressLint("NewApi") // Desugaring enabled in build.gradle
        private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM")

        @SuppressLint("NewApi") // Desugaring enabled in build.gradle
        fun getCurrentMonth(): String {
            return YearMonth.now().format(MONTH_FORMATTER)
        }

        @SuppressLint("NewApi") // Desugaring enabled in build.gradle
        fun formatMonthDisplay(month: String): String {
            return try {
                val yearMonth = YearMonth.parse(month, MONTH_FORMATTER)
                yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            } catch (_: Exception) {
                month
            }
        }
    }
}

