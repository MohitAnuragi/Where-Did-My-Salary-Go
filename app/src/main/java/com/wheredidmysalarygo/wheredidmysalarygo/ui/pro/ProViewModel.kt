package com.wheredidmysalarygo.wheredidmysalarygo.ui.pro

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.wheredidmysalarygo.wheredidmysalarygo.billing.BillingRepository
import com.wheredidmysalarygo.wheredidmysalarygo.billing.SubscriptionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionPlan(
    val id: String,
    val type: SubscriptionPlanType,
    val title: String,
    val price: String,
    val productDetails: ProductDetails?
)

enum class SubscriptionPlanType {
    ONE_MONTH,
    THREE_MONTHS,
    SIX_MONTHS,
    ONE_YEAR
}

data class ProUiState(
    val isProUser: Boolean = false,
    val selectedPlan: SubscriptionPlanType? = null,
    val subscriptionPlans: List<SubscriptionPlan> = emptyList(),
    val currentProductId: String? = null,
    val isLoading: Boolean = true,
    val showSuccessToast: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProUiState())
    val uiState: StateFlow<ProUiState> = _uiState.asStateFlow()

    init {
        initializeBilling()
        observeSubscriptionStatus()
    }

    private fun initializeBilling() {
        viewModelScope.launch {
            // Initialize billing client
            billingRepository.initialize()
        }
    }

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            combine(
                billingRepository.subscriptionStatus,
                billingRepository.productDetails
            ) { status, productDetailsMap ->
                val isProUser = status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.CANCELED
                val currentProductId = if (isProUser) {
                    billingRepository.getCurrentProductId()
                } else {
                    null
                }

                val plans = buildSubscriptionPlans(productDetailsMap)

                _uiState.value = _uiState.value.copy(
                    isProUser = isProUser,
                    currentProductId = currentProductId,
                    subscriptionPlans = plans,
                    isLoading = false
                )
            }.collect()
        }
    }

    private fun buildSubscriptionPlans(productDetailsMap: Map<String, ProductDetails>): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                id = "pro_1_month",
                type = SubscriptionPlanType.ONE_MONTH,
                title = "1 Month Pro",
                price = getFormattedPrice(productDetailsMap["pro_1_month"]) ?: "Loading...",
                productDetails = productDetailsMap["pro_1_month"]
            ),
            SubscriptionPlan(
                id = "pro_3_months",
                type = SubscriptionPlanType.THREE_MONTHS,
                title = "3 Months Pro",
                price = getFormattedPrice(productDetailsMap["pro_3_months"]) ?: "Loading...",
                productDetails = productDetailsMap["pro_3_months"]
            ),
            SubscriptionPlan(
                id = "pro_6_months",
                type = SubscriptionPlanType.SIX_MONTHS,
                title = "6 Months Pro",
                price = getFormattedPrice(productDetailsMap["pro_6_months"]) ?: "Loading...",
                productDetails = productDetailsMap["pro_6_months"]
            ),
            SubscriptionPlan(
                id = "pro_1_year",
                type = SubscriptionPlanType.ONE_YEAR,
                title = "1 Year Pro",
                price = getFormattedPrice(productDetailsMap["pro_1_year"]) ?: "Loading...",
                productDetails = productDetailsMap["pro_1_year"]
            )
        )
    }

    private fun getFormattedPrice(productDetails: ProductDetails?): String? {
        return productDetails?.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice
    }

    fun selectPlan(plan: SubscriptionPlanType) {
        _uiState.value = _uiState.value.copy(selectedPlan = plan)
    }

    fun subscribeToPlan(activity: Activity, plan: SubscriptionPlanType) {
        val productId = when (plan) {
            SubscriptionPlanType.ONE_MONTH -> "pro_1_month"
            SubscriptionPlanType.THREE_MONTHS -> "pro_3_months"
            SubscriptionPlanType.SIX_MONTHS -> "pro_6_months"
            SubscriptionPlanType.ONE_YEAR -> "pro_1_year"
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        // Launch billing flow
        billingRepository.launchPurchaseFlow(activity, productId)

        // Loading state will be updated when purchase callback fires
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            billingRepository.restorePurchases()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun dismissToast() {
        _uiState.value = _uiState.value.copy(showSuccessToast = false)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

