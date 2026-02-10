package com.wheredidmysalarygo.wheredidmysalarygo.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfig
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfigProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val selectedCountry: CountryConfig = CountryConfigProvider.getConfig(CountryConfigProvider.getDefaultCountryCode()),
    val salaryInput: String = "",
    val creditDateInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigationEvent: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val salaryRepository: SalaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onSalaryInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            salaryInput = input,
            errorMessage = null
        )
    }

    fun onCountrySelected(countryConfig: CountryConfig) {
        _uiState.value = _uiState.value.copy(
            selectedCountry = countryConfig,
            errorMessage = null
        )
    }

    fun onCreditDateInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            creditDateInput = input,
            errorMessage = null
        )
    }

    fun saveSalary() {
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

        // Save to DataStore
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                salaryRepository.setCountryCode(_uiState.value.selectedCountry.countryCode)
                salaryRepository.setSalary(salary)
                if (creditDate != null) {
                    salaryRepository.setSalaryCreditDate(creditDate)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    navigationEvent = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }

    fun onNavigationComplete() {
        _uiState.value = _uiState.value.copy(navigationEvent = false)
    }
}

