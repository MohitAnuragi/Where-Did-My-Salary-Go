package com.wheredidmysalarygo.wheredidmysalarygo.billing

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SubscriptionValidationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val billingRepository: BillingRepository,
    private val subscriptionManager: SubscriptionManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            billingRepository.restorePurchases()
            
            Result.success()
        } catch (e: Exception) {

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "subscription_validation_work"
    }
}

