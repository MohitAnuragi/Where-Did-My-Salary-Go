package com.wheredidmysalarygo.wheredidmysalarygo.ui.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheredidmysalarygo.wheredidmysalarygo.domain.model.Expense
import com.wheredidmysalarygo.wheredidmysalarygo.domain.model.ExpenseFrequency
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfig
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfigProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseUiState(
    val name: String = "",
    val amount: String = "",
    val dueDate: String = "",
    val countryConfig: CountryConfig = CountryConfigProvider.getConfig("IN"),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successEvent: Boolean = false
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val salaryRepository: SalaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        loadCountryConfig()
    }

    private fun loadCountryConfig() {
        viewModelScope.launch {
            val countryCode = salaryRepository.getCountryCode().first()
            val countryConfig = CountryConfigProvider.getConfig(countryCode)
            _uiState.value = _uiState.value.copy(countryConfig = countryConfig)
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            errorMessage = null,
            // Smart suggestions
            amount = getSmartSuggestion(name) ?: _uiState.value.amount
        )
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount,
            errorMessage = null
        )
    }

    fun onDueDateChange(dueDate: String) {
        _uiState.value = _uiState.value.copy(
            dueDate = dueDate,
            errorMessage = null
        )
    }

    fun addExpense() {
        val name = _uiState.value.name.trim()
        val amountText = _uiState.value.amount.trim()
        val dueDateText = _uiState.value.dueDate.trim()

        // Validation
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter expense name")
            return
        }

        if (amountText.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter amount")
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid amount")
            return
        }

        if (dueDateText.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter due date")
            return
        }

        val dueDate = dueDateText.toIntOrNull()
        if (dueDate == null || dueDate !in 1..31) {
            _uiState.value = _uiState.value.copy(errorMessage = "Due date must be between 1-31")
            return
        }

        // Save expense
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val expense = Expense(
                    name = name,
                    amount = amount,
                    dueDate = dueDate,
                    frequency = ExpenseFrequency.MONTHLY
                )
                expenseRepository.insertExpense(expense)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successEvent = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }

    fun onSuccessEventHandled() {
        _uiState.value = _uiState.value.copy(successEvent = false)
    }

    // Smart suggestion for common expenses
    private fun getSmartSuggestion(name: String): String? {
        val lowerName = name.lowercase()
        return when {
            "netflix" in lowerName -> "199"
            "prime" in lowerName && "amazon" in lowerName -> "299"
            "prime" in lowerName -> "299"
            "spotify" in lowerName -> "119"
            "youtube" in lowerName && "premium" in lowerName -> "129"
            "hotstar" in lowerName || "disney" in lowerName -> "299"
            "gym" in lowerName -> "1000"
            else -> null
        }
    }
}

