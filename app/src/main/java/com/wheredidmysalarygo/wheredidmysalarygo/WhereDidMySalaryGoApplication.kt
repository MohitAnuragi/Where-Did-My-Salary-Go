package com.wheredidmysalarygo.wheredidmysalarygo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wheredidmysalarygo.wheredidmysalarygo.billing.SubscriptionValidationWorker
import com.wheredidmysalarygo.wheredidmysalarygo.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class WhereDidMySalaryGoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize notification channel for reminders
        NotificationHelper.createNotificationChannel(this)

        // Schedule subscription validation worker
        scheduleSubscriptionValidation()
    }

    private fun scheduleSubscriptionValidation() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val subscriptionValidationWork = PeriodicWorkRequestBuilder<SubscriptionValidationWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // flex interval
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SubscriptionValidationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            subscriptionValidationWork
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
