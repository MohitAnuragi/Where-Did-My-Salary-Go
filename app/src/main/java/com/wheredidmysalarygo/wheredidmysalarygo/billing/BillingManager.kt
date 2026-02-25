package com.wheredidmysalarygo.wheredidmysalarygo.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        // Product IDs must match what you configure in Google Play Console
        val PRODUCT_IDS = listOf(
            "pro_1_month",
            "pro_3_months",
            "pro_6_months",
            "pro_1_year"
        )
    }

    private var billingClient: BillingClient? = null
    private var purchaseUpdateCallback: ((List<Purchase>) -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var connectionRetryCount = 0
    private val maxRetries = 3


    fun startConnection(onConnected: () -> Unit) {
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .build()
        }

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    connectionRetryCount = 0 // Reset retry count on success
                    onConnected()
                } else {
                    // Connection failed - retry with exponential backoff
                    retryConnection(onConnected)
                }
            }

            override fun onBillingServiceDisconnected() {
                // Connection lost - retry with exponential backoff
                retryConnection(onConnected)
            }
        })
    }


    private fun retryConnection(onConnected: () -> Unit) {
        if (connectionRetryCount < maxRetries) {
            connectionRetryCount++
            val delayMillis = (1000L * (1 shl (connectionRetryCount - 1))) // Exponential: 1s, 2s, 4s

            coroutineScope.launch {
                kotlinx.coroutines.delay(delayMillis)
                startConnection(onConnected)
            }
        }
    }

    fun queryProductDetails(onResult: (Map<String, ProductDetails>) -> Unit) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onResult(emptyMap())
            return
        }

        val productList = PRODUCT_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        coroutineScope.launch {
            client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val detailsMap = productDetailsList.associateBy { it.productId }
                    onResult(detailsMap)
                } else {
                    onResult(emptyMap())
                }
            }
        }
    }


    fun queryPurchases(onResult: (List<Purchase>) -> Unit) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onResult(emptyList())
            return
        }

        coroutineScope.launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            client.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onResult(purchases)
                } else {
                    onResult(emptyList())
                }
            }
        }
    }


    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val client = billingClient
        if (client == null || !client.isReady) {
            return
        }

        // Get the offer token (required for subscriptions)
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken

        if (offerToken == null) {
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        coroutineScope.launch {
            client.launchBillingFlow(activity, billingFlowParams)
        }
    }


    fun acknowledgePurchase(purchase: Purchase, onComplete: (Boolean) -> Unit) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onComplete(false)
            return
        }

        if (purchase.isAcknowledged) {
            // Already acknowledged
            onComplete(true)
            return
        }

        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        coroutineScope.launch {
            client.acknowledgePurchase(acknowledgeParams) { billingResult ->
                onComplete(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            }
        }
    }


    fun setPurchaseUpdateCallback(callback: (List<Purchase>) -> Unit) {
        this.purchaseUpdateCallback = callback
    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let { purchaseList ->
                    purchaseList.forEach { purchase ->
                        handlePurchase(purchase)
                    }
                    purchaseUpdateCallback?.invoke(purchaseList)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // User canceled - do nothing
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // Already owned - restore purchases
                queryPurchases { existingPurchases ->
                    purchaseUpdateCallback?.invoke(existingPurchases)
                }
            }
            else -> {


            }
        }
    }


    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase) { success ->
                    if (success) {
                        purchaseUpdateCallback?.invoke(listOf(purchase))
                    }
                }
            }
        }
    }


    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
    }
}