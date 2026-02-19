package com.wheredidmysalarygo.wheredidmysalarygo.ui.snapshot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheredidmysalarygo.wheredidmysalarygo.domain.model.Expense
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfig
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfigProvider
import com.wheredidmysalarygo.wheredidmysalarygo.utils.MonthInitializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SnapshotUiState(
    val salary: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val expenseCount: Int = 0,
    val expenses: List<Expense> = emptyList(),
    val committedPercent: Float = 0f,
    val countryConfig: CountryConfig = CountryConfigProvider.getConfig("IN"),
    val currentMonth: String = "",
    val isLoading: Boolean = true,
    val isProUser: Boolean = false
)

@HiltViewModel
class SnapshotViewModel @Inject constructor(
    private val salaryRepository: SalaryRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<SnapshotUiState> = combine(
        salaryRepository.getSalary(),
        expenseRepository.getAllExpenses(),
        salaryRepository.getCountryCode()
    ) { salary, allExpenses, countryCode ->
        val currentMonth = MonthInitializer.getCurrentMonth()

        // Filter expenses for current month only
        val expenses = allExpenses.filter { it.month == currentMonth }

        val totalExpenses = expenses.sumOf { it.amount }
        val remainingBalance = salary - totalExpenses
        val committedPercent = if (salary > 0) {
            ((totalExpenses / salary) * 100).toFloat()
        } else {
            0f
        }

        SnapshotUiState(
            salary = salary,
            totalExpenses = totalExpenses,
            remainingBalance = remainingBalance,
            expenseCount = expenses.size,
            expenses = expenses,
            committedPercent = committedPercent,
            countryConfig = CountryConfigProvider.getConfig(countryCode),
            currentMonth = MonthInitializer.formatMonthDisplay(currentMonth),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SnapshotUiState()
    )
}

