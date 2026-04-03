package com.wheredidmysalarygo.wheredidmysalarygo.billing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton



private val Context.subscriptionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "subscription_preferences"
)

@Singleton
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val SUBSCRIPTION_STATUS_KEY = stringPreferencesKey("subscription_status")
        private val PRODUCT_ID_KEY = stringPreferencesKey("product_id")
        private val PURCHASE_TOKEN_KEY = stringPreferencesKey("purchase_token")
        private val PURCHASE_TIME_KEY = longPreferencesKey("purchase_time")
        private val EXPIRY_TIME_KEY = longPreferencesKey("expiry_time")
        private val IS_ACKNOWLEDGED_KEY = booleanPreferencesKey("is_acknowledged")
        private val AUTO_RENEWING_KEY = booleanPreferencesKey("auto_renewing")
    }


    val subscriptionStatusFlow: Flow<SubscriptionStatus> =
        context.subscriptionDataStore.data.map { preferences ->
            val statusString = preferences[SUBSCRIPTION_STATUS_KEY] ?: SubscriptionStatus.NONE.name
            try {
                SubscriptionStatus.valueOf(statusString)
            } catch (e: IllegalArgumentException) {
                SubscriptionStatus.NONE
            }
        }


    val productIdFlow: Flow<String?> =
        context.subscriptionDataStore.data.map { preferences ->
            preferences[PRODUCT_ID_KEY]
        }


    suspend fun saveSubscription(
        productId: String,
        purchaseToken: String,
        purchaseTime: Long,
        isAcknowledged: Boolean,
        autoRenewing: Boolean
    ) {
        context.subscriptionDataStore.edit { preferences ->
            // Calculate expiry time based on product duration
            val expiryTime = calculateExpiryTime(productId, purchaseTime)

            // If subscription is not auto-renewing, mark as CANCELED (still active until expiry)
            val status = if (autoRenewing) {
                SubscriptionStatus.ACTIVE
            } else {
                SubscriptionStatus.CANCELED
            }

            preferences[SUBSCRIPTION_STATUS_KEY] = status.name
            preferences[PRODUCT_ID_KEY] = productId
            preferences[PURCHASE_TOKEN_KEY] = purchaseToken
            preferences[PURCHASE_TIME_KEY] = purchaseTime
            preferences[EXPIRY_TIME_KEY] = expiryTime
            preferences[IS_ACKNOWLEDGED_KEY] = isAcknowledged
            preferences[AUTO_RENEWING_KEY] = autoRenewing
        }
    }


    private fun calculateExpiryTime(productId: String, purchaseTime: Long): Long {
        val durationMillis = when (productId) {
            "pro_1_month" -> 30L * 24 * 60 * 60 * 1000   // 30 days in milliseconds
            "pro_3_months" -> 90L * 24 * 60 * 60 * 1000  // 90 days in milliseconds
            "pro_6_months" -> 180L * 24 * 60 * 60 * 1000 // 180 days in milliseconds
            "pro_1_year" -> 365L * 24 * 60 * 60 * 1000   // 365 days in milliseconds
            else -> 30L * 24 * 60 * 60 * 1000            // Default to 30 days
        }
        return purchaseTime + durationMillis
    }

    suspend fun clearSubscription() {
        context.subscriptionDataStore.edit { preferences ->
            preferences[SUBSCRIPTION_STATUS_KEY] = SubscriptionStatus.NONE.name
            preferences.remove(PRODUCT_ID_KEY)
            preferences.remove(PURCHASE_TOKEN_KEY)
            preferences.remove(PURCHASE_TIME_KEY)
            preferences.remove(EXPIRY_TIME_KEY)
            preferences.remove(IS_ACKNOWLEDGED_KEY)
            preferences.remove(AUTO_RENEWING_KEY)
        }
    }


    suspend fun getSubscriptionStatus(): SubscriptionStatus {
        return subscriptionStatusFlow.first()
    }


    suspend fun getProductId(): String? {
        return productIdFlow.first()
    }


    suspend fun updateSubscriptionStatus(status: SubscriptionStatus) {
        context.subscriptionDataStore.edit { preferences ->
            preferences[SUBSCRIPTION_STATUS_KEY] = status.name
        }
    }


    suspend fun isProUser(): Boolean {
        val status = getSubscriptionStatus()
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.CANCELED
    }


    suspend fun isAutoRenewing(): Boolean {
        return context.subscriptionDataStore.data.first()[AUTO_RENEWING_KEY] ?: false
    }


    suspend fun getPurchaseToken(): String? {
        return context.subscriptionDataStore.data.first()[PURCHASE_TOKEN_KEY]
    }


    suspend fun validateOfflineExpiry(): Boolean {
        val preferences = context.subscriptionDataStore.data.first()
        val statusString = preferences[SUBSCRIPTION_STATUS_KEY] ?: return false
        val status = try {
            SubscriptionStatus.valueOf(statusString)
        } catch (e: IllegalArgumentException) {
            return false
        }

        // Only validate if subscription is ACTIVE or CANCELED
        if (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.CANCELED) {
            val expiryTime = preferences[EXPIRY_TIME_KEY] ?: return false
            val currentTime = System.currentTimeMillis()

            // If current time is past expiry, mark as EXPIRED
            if (currentTime > expiryTime) {
                updateSubscriptionStatus(SubscriptionStatus.EXPIRED)
                return true // Expired
            }
        }

        return false // Not expired
    }


    suspend fun getExpiryTime(): Long? {
        return context.subscriptionDataStore.data.first()[EXPIRY_TIME_KEY]
    }
}
