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
 * MonthEndWorker: Sends month-end snapshot notification
 *
 * Triggers: Last day of the month
 * Frequency: Once per month
 * Example: "This month, you had ₹23,801 free to spend."
 */
@HiltWorker
class MonthEndWorker @AssistedInject constructor(
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
            val monthEndEnabled = userPreferencesManager.monthEndSnapshotEnabledFlow.first()

            if (!isPro || !notificationsEnabled || !monthEndEnabled) {
                return Result.success()
            }

            // Calculate free money
            val salary = salaryRepository.getSalary().first()
            val expenses = expenseRepository.getAllExpenses().first()
            val totalCommitted = expenses.sumOf { it.amount }
            val freeToSpend = salary - totalCommitted

            // Get country config for currency formatting
            val countryCode = salaryRepository.getCountryCode().first()
            val countryConfig = CountryConfigProvider.getConfig(countryCode)

            // Send notification
            NotificationHelper.sendMonthEndNotification(
                context = applicationContext,
                freeToSpend = freeToSpend,
                currencySymbol = countryConfig.currencySymbol
            )

            // Reschedule for next month
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationScheduler.scheduleMonthEndNotification(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

