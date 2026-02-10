package com.wheredidmysalarygo.wheredidmysalarygo.notifications

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.UserPreferencesManager
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CountryConfigProvider
import com.wheredidmysalarygo.wheredidmysalarygo.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * SalarySummaryWorker: Sends "Salary is in" notification once per month
 *
 * Triggers: After salary credit date
 * Frequency: Once per month
 * Example: "Your salary is in. ₹16,199 already committed."
 */
@HiltWorker
class SalarySummaryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val salaryRepository: SalaryRepository,
    private val expenseRepository: ExpenseRepository,
    private val userPreferencesManager: UserPreferencesManager
) : CoroutineWorker(appContext, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            // Verify Pro status and notification settings
            val isPro = userPreferencesManager.isProUserFlow.first()
            val notificationsEnabled = userPreferencesManager.notificationsEnabledFlow.first()
            val salarySummaryEnabled = userPreferencesManager.salarySummaryEnabledFlow.first()

            if (!isPro || !notificationsEnabled || !salarySummaryEnabled) {
                return Result.success()
            }

            // Get salary and expenses
            val expenses = expenseRepository.getAllExpenses().first()
            val totalCommitted = expenses.sumOf { it.amount }

            // Get country config for currency formatting
            val countryCode = salaryRepository.getCountryCode().first()
            val countryConfig = CountryConfigProvider.getConfig(countryCode)

            // Send notification
            NotificationHelper.sendSalarySummaryNotification(
                context = applicationContext,
                totalCommitted = totalCommitted,
                currencySymbol = countryConfig.currencySymbol
            )

            // Reschedule for next month
            val creditDate = salaryRepository.getSalaryCreditDate().first()
            if (creditDate != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationScheduler.scheduleSalarySummaryNotification(applicationContext, creditDate)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

