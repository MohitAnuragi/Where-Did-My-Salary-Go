package com.wheredidmysalarygo.wheredidmysalarygo.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheredidmysalarygo.wheredidmysalarygo.billing.SubscriptionManager
import com.wheredidmysalarygo.wheredidmysalarygo.billing.SubscriptionStatus
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.UserPreferencesManager
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.export.ExportManager
import com.wheredidmysalarygo.wheredidmysalarygo.export.ExportResult
import com.wheredidmysalarygo.wheredidmysalarygo.export.SubscriptionPlan
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfig
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfigProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val darkMode: Boolean = false,
    val countryConfig: CountryConfig = CountryConfigProvider.getConfig("IN"),
    val isProUser: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val salarySummaryEnabled: Boolean = true,
    val monthEndSnapshotEnabled: Boolean = true,
    val dueDateRemindersEnabled: Boolean = true,
    val remindDaysBefore: Int = 1,
    val isExporting: Boolean = false,
    val salaryValidationError: String? = null,
    val navigateToProScreen: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val salaryRepository: SalaryRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val exportManager: ExportManager,
    private val subscriptionManager: SubscriptionManager
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
                userPreferencesManager.darkModeFlow,
                salaryRepository.getCountryCode(),
                subscriptionManager.subscriptionStatusFlow,
                userPreferencesManager.notificationsEnabledFlow,
                userPreferencesManager.salarySummaryEnabledFlow,
                userPreferencesManager.monthEndSnapshotEnabledFlow,
                userPreferencesManager.dueDateRemindersEnabledFlow,
                userPreferencesManager.remindDaysBeforeFlow
            ) { flows ->
                val salary = flows[0] as Double
                val creditDate = flows[1] as Int?
                val darkMode = flows[2] as Boolean
                val countryCode = flows[3] as String
                val subscriptionStatus = flows[4] as SubscriptionStatus
                val notificationsEnabled = flows[5] as Boolean
                val salarySummaryEnabled = flows[6] as Boolean
                val monthEndSnapshotEnabled = flows[7] as Boolean
                val dueDateRemindersEnabled = flows[8] as Boolean
                val remindDaysBefore = flows[9] as Int

                SettingsUiState(
                    currentSalary = salary,
                    currentCreditDate = creditDate,
                    salaryInput = salary.toInt().toString(),
                    creditDateInput = creditDate?.toString() ?: "",
                    darkMode = darkMode,
                    countryConfig = CountryConfigProvider.getConfig(countryCode),
                    isProUser = subscriptionStatus == SubscriptionStatus.ACTIVE ||
                               subscriptionStatus == SubscriptionStatus.CANCELED,
                    notificationsEnabled = notificationsEnabled,
                    salarySummaryEnabled = salarySummaryEnabled,
                    monthEndSnapshotEnabled = monthEndSnapshotEnabled,
                    dueDateRemindersEnabled = dueDateRemindersEnabled,
                    remindDaysBefore = remindDaysBefore
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onSalaryInputChange(input: String) {
        val validationError = validateSalaryInput(input)
        _uiState.value = _uiState.value.copy(
            salaryInput = input,
            errorMessage = null,
            salaryValidationError = validationError
        )
    }

    private fun validateSalaryInput(input: String): String? {
        if (input.isEmpty()) return null

        val salary = input.toDoubleOrNull() ?: return "Please enter a valid number"

        return when {
            salary < 500 -> "Salary must be at least ₹500"
            salary > 100_000_000 -> "Salary cannot exceed ₹10 crore"
            else -> null
        }
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

        if (salaryText.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your monthly salary")
            return
        }

        val salary = salaryText.toDoubleOrNull()
        if (salary == null || salary <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid salary amount")
            return
        }

        // Validate salary range
        if (salary < 500) {
            _uiState.value = _uiState.value.copy(errorMessage = "Salary must be at least ₹500")
            return
        }

        if (salary > 100_000_000) {
            _uiState.value = _uiState.value.copy(errorMessage = "Salary cannot exceed ₹10 crore")
            return
        }

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

    // Pro notification controls (only work if user is Pro)
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isProUser) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Notifications are a Pro feature. Upgrade to Pro to enable."
                    )
                    return@launch
                }

                userPreferencesManager.setNotificationsEnabled(enabled)

                // TODO: Schedule/cancel notifications based on enabled state
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update notifications: ${e.message}"
                )
            }
        }
    }

    fun toggleSalarySummary(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesManager.setSalarySummaryEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update setting: ${e.message}"
                )
            }
        }
    }

    fun toggleMonthEndSnapshot(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesManager.setMonthEndSnapshotEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update setting: ${e.message}"
                )
            }
        }
    }

    fun toggleDueDateReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesManager.setDueDateRemindersEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update setting: ${e.message}"
                )
            }
        }
    }

    fun updateRemindDaysBefore(days: Int) {
        viewModelScope.launch {
            try {
                userPreferencesManager.setRemindDaysBefore(days)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update reminder days: ${e.message}"
                )
            }
        }
    }

    /**
     * Export data to CSV
     * Enforces subscription plan limits
     */
    fun exportData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true)

                // Check if user is Pro using SubscriptionManager
                val isProUser = subscriptionManager.isProUser()

                if (!isProUser) {
                    // User is not Pro - trigger navigation to Pro screen
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        navigateToProScreen = true
                    )
                    return@launch
                }

                // Get subscription plan from SubscriptionManager
                val productId = subscriptionManager.getProductId()
                val subscriptionPlan = when (productId) {
                    "pro_1_month" -> SubscriptionPlan.PRO_1_MONTH
                    "pro_3_months" -> SubscriptionPlan.PRO_3_MONTH
                    "pro_6_months" -> SubscriptionPlan.PRO_6_MONTH
                    "pro_1_year" -> SubscriptionPlan.PRO_1_YEAR
                    else -> SubscriptionPlan.PRO_1_MONTH // Default for Pro users
                }

                // Export data
                when (val result = exportManager.exportData(subscriptionPlan)) {
                    is ExportResult.Success -> {
                        // Share the file
                        exportManager.shareFile(result.file)
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            successMessage = "Data exported successfully"
                        )
                    }
                    is ExportResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            errorMessage = result.message
                        )
                    }
                    is ExportResult.NotAllowed -> {

                        exportManager.showUpgradePrompt()
                        _uiState.value = _uiState.value.copy(isExporting = false)
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = "Export failed: ${e.message}"
                )
            }
        }
    }

    fun onNavigateToProScreenHandled() {
        _uiState.value = _uiState.value.copy(navigateToProScreen = false)
    }
}
