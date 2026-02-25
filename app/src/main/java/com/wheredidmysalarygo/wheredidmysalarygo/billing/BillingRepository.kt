package com.wheredidmysalarygo.wheredidmysalarygo.billing

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BillingRepository @Inject constructor(
    private val billingManager: BillingManager,
    private val subscriptionManager: SubscriptionManager
) {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus.NONE)
    val subscriptionStatus: Flow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: Flow<Map<String, ProductDetails>> = _productDetails.asStateFlow()


    fun initialize() {
        billingManager.startConnection {
            // Once connected, validate offline first, then restore from Play Store
            repositoryScope.launch {
                // STEP 1: Validate offline expiry (revenue protection)
                subscriptionManager.validateOfflineExpiry()

                // STEP 2: Override with online Play Store query
                restorePurchases()
            }
            queryProductDetails()
        }
    }


    suspend fun validateSubscriptionStatus() {
        val wasExpired = subscriptionManager.validateOfflineExpiry()

        if (wasExpired) {
            _subscriptionStatus.value = SubscriptionStatus.EXPIRED
        }

        restorePurchases()
    }


    private fun queryProductDetails() {
        billingManager.queryProductDetails { details ->
            _productDetails.value = details
        }
    }


    fun restorePurchases() {
        billingManager.queryPurchases { purchases ->
            if (purchases.isNotEmpty()) {
                // Find the most recent active subscription
                val activePurchase = purchases.firstOrNull { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (activePurchase != null) {
                    // Determine status based on autoRenewing
                    val status = if (activePurchase.isAutoRenewing) {
                        SubscriptionStatus.ACTIVE
                    } else {
                        SubscriptionStatus.CANCELED
                    }

                    // Save subscription status in coroutine scope
                    repositoryScope.launch {
                        subscriptionManager.saveSubscription(
                            productId = activePurchase.products.firstOrNull() ?: "",
                            purchaseToken = activePurchase.purchaseToken,
                            purchaseTime = activePurchase.purchaseTime,
                            isAcknowledged = activePurchase.isAcknowledged,
                            autoRenewing = activePurchase.isAutoRenewing
                        )
                    }
                    _subscriptionStatus.value = status
                } else {
                    // No active purchase - mark as expired
                    repositoryScope.launch {
                        subscriptionManager.clearSubscription()
                    }
                    _subscriptionStatus.value = SubscriptionStatus.EXPIRED
                }
            } else {
                // No purchases found - mark as none
                repositoryScope.launch {
                    subscriptionManager.clearSubscription()
                }
                _subscriptionStatus.value = SubscriptionStatus.NONE
            }
        }
    }


    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val details = _productDetails.value[productId]
        if (details != null) {
            billingManager.launchBillingFlow(activity, details)
        }
    }


    fun handlePurchaseUpdate(purchases: List<Purchase>) {
        val activePurchase = purchases.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        if (activePurchase != null) {
            // Determine status based on autoRenewing
            val status = if (activePurchase.isAutoRenewing) {
                SubscriptionStatus.ACTIVE
            } else {
                SubscriptionStatus.CANCELED
            }

            // Acknowledge purchase if not already acknowledged
            if (!activePurchase.isAcknowledged) {
                billingManager.acknowledgePurchase(activePurchase) { success ->
                    if (success) {
                        // Save subscription status in coroutine scope
                        repositoryScope.launch {
                            subscriptionManager.saveSubscription(
                                productId = activePurchase.products.firstOrNull() ?: "",
                                purchaseToken = activePurchase.purchaseToken,
                                purchaseTime = activePurchase.purchaseTime,
                                isAcknowledged = true,
                                autoRenewing = activePurchase.isAutoRenewing
                            )
                        }
                        _subscriptionStatus.value = status
                    }
                }
            } else {
                // Already acknowledged, just update status
                repositoryScope.launch {
                    subscriptionManager.saveSubscription(
                        productId = activePurchase.products.firstOrNull() ?: "",
                        purchaseToken = activePurchase.purchaseToken,
                        purchaseTime = activePurchase.purchaseTime,
                        isAcknowledged = true,
                        autoRenewing = activePurchase.isAutoRenewing
                    )
                }
                _subscriptionStatus.value = status
            }
        }
    }


    suspend fun isProUser(): Boolean {
        val status = subscriptionManager.getSubscriptionStatus()
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.CANCELED
    }


    suspend fun getCurrentProductId(): String? {
        return subscriptionManager.getProductId()
    }
}