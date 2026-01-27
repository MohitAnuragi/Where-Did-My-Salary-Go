package com.wheredidmysalarygo.wheredidmysalarygo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.UserPreferencesManager
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentSalary: Double = 0.0,
    val currentCreditDate: Int? = null,
    val salaryInput: String = "",
    val creditDateInput: String = "",
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val darkMode: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val salaryRepository: SalaryRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                salaryRepository.getSalary(),
                salaryRepository.getSalaryCreditDate(),
                userPreferencesManager.darkModeFlow
            ) { salary, creditDate, darkMode ->
                _uiState.value = _uiState.value.copy(
                    currentSalary = salary,
                    currentCreditDate = creditDate,
                    salaryInput = salary.toInt().toString(),
                    creditDateInput = creditDate?.toString() ?: "",
                    darkMode = darkMode
                )
            }.collect()
        }
    }

    fun onSalaryInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            salaryInput = input,
            errorMessage = null
        )
    }

    fun onCreditDateInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            creditDateInput = input,
            errorMessage = null
        )
    }

    fun enableEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = true,
            errorMessage = null,
            successMessage = null
        )
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            salaryInput = _uiState.value.currentSalary.toInt().toString(),
            creditDateInput = _uiState.value.currentCreditDate?.toString() ?: "",
            errorMessage = null
        )
    }

    fun saveSalarySettings() {
        val salaryText = _uiState.value.salaryInput.trim()
        val creditDateText = _uiState.value.creditDateInput.trim()

        // Validation
        if (salaryText.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your monthly salary")
            return
        }

        val salary = salaryText.toDoubleOrNull()
        if (salary == null || salary <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid salary amount")
            return
        }

        // Validate credit date if provided
        val creditDate: Int? = if (creditDateText.isNotEmpty()) {
            val date = creditDateText.toIntOrNull()
            if (date == null || date !in 1..31) {
                _uiState.value = _uiState.value.copy(errorMessage = "Credit date must be between 1-31")
                return
            }
            date
        } else {
            null
        }

        // Save settings
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                salaryRepository.setSalary(salary)
                salaryRepository.setSalaryCreditDate(creditDate)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isEditMode = false,
                    currentSalary = salary,
                    currentCreditDate = creditDate,
                    successMessage = "Settings saved successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesManager.setDarkMode(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to change theme: ${e.message}"
                )
            }
        }
    }
}

