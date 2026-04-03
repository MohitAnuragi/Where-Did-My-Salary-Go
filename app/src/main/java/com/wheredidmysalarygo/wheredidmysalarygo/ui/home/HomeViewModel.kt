package com.wheredidmysalarygo.wheredidmysalarygo.ui.home

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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val salary: Double = 0.0,
    val expenses: List<Expense> = emptyList(),
    val totalFixedExpenses: Double = 0.0,
    val freeToSpend: Double = 0.0,
    val committedPercent: Float = 0f,
    val countryConfig: CountryConfig = CountryConfigProvider.getConfig("IN"),
    val currentMonth: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val salaryRepository: SalaryRepository,
    private val expenseRepository: ExpenseRepository,
    private val monthInitializer: MonthInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        initializeMonth()
        loadData()
    }

    private fun initializeMonth() {
        viewModelScope.launch {
            monthInitializer.initializeCurrentMonth()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val currentMonth = MonthInitializer.getCurrentMonth()

            combine(
                salaryRepository.getSalary(),
                expenseRepository.getAllExpenses(),
                salaryRepository.getCountryCode()
            ) { salary, allExpenses, countryCode ->
                // Filter expenses for current month only
                val expenses = allExpenses.filter { it.month == currentMonth }

                val totalFixed = expenses.sumOf { it.amount }
                val freeToSpend = salary - totalFixed
                val committedPercent = if (salary > 0) {
                    ((totalFixed / salary) * 100).toFloat()
                } else {
                    0f
                }

                HomeUiState(
                    salary = salary,
                    expenses = expenses,
                    totalFixedExpenses = totalFixed,
                    freeToSpend = freeToSpend,
                    committedPercent = committedPercent,
                    countryConfig = CountryConfigProvider.getConfig(countryCode),
                    currentMonth = MonthInitializer.formatMonthDisplay(currentMonth),
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
                // Update monthly summary
                monthInitializer.updateMonthlySummary(currentMonth)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadData()
    }
}

