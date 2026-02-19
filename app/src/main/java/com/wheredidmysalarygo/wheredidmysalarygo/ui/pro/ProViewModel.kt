package com.wheredidmysalarygo.wheredidmysalarygo.ui.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.UserPreferencesManager
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.utils.PricingProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SubscriptionPlanType {
    ONE_MONTH,
    SIX_MONTHS,
    ONE_YEAR,
    FIVE_YEARS
}

data class ProUiState(
    val isProUser: Boolean = false,
    val selectedPlan: SubscriptionPlanType? = null,
    val countryCode: String = "IN",
    val pricing: PricingProvider.PlanPricing = PricingProvider.getPricing("IN"),
    val isLoading: Boolean = false,
    val showSuccessToast: Boolean = false
)

@HiltViewModel
class ProViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val salaryRepository: SalaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProUiState())
    val uiState: StateFlow<ProUiState> = _uiState.asStateFlow()

    init {
        loadProStatus()
    }

    private fun loadProStatus() {
        viewModelScope.launch {
            combine(
                userPreferencesManager.isProUserFlow,
                salaryRepository.getCountryCode()
            ) { isProUser, countryCode ->
                _uiState.value = _uiState.value.copy(
                    isProUser = isProUser,
                    countryCode = countryCode,
                    pricing = PricingProvider.getPricing(countryCode)
                )
            }.collect()
        }
    }

    fun selectPlan(plan: SubscriptionPlanType) {
        _uiState.value = _uiState.value.copy(selectedPlan = plan)
    }

    fun subscribeToPlan(plan: SubscriptionPlanType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Save Pro status
            userPreferencesManager.setProUser(true)

            // Save selected plan
            val planName = when (plan) {
                SubscriptionPlanType.ONE_MONTH -> "PRO_1_MONTH"
                SubscriptionPlanType.SIX_MONTHS -> "PRO_6_MONTH"
                SubscriptionPlanType.ONE_YEAR -> "PRO_1_YEAR"
                SubscriptionPlanType.FIVE_YEARS -> "PRO_5_YEAR"
            }
            userPreferencesManager.setSubscriptionPlan(planName)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isProUser = true,
                showSuccessToast = true
            )
        }
    }

    fun dismissToast() {
        _uiState.value = _uiState.value.copy(showSuccessToast = false)
    }
}

