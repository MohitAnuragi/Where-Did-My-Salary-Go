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


@HiltWorker
class DueDateReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val salaryRepository: SalaryRepository,
    private val userPreferencesManager: UserPreferencesManager
) : CoroutineWorker(appContext, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            // Verify Pro status and notification settings
            val isPro = userPreferencesManager.isProUserFlow.first()
            val notificationsEnabled = userPreferencesManager.notificationsEnabledFlow.first()
            val dueDateEnabled = userPreferencesManager.dueDateRemindersEnabledFlow.first()

            if (!isPro || !notificationsEnabled || !dueDateEnabled) {
                return Result.success()
            }

            // Get expense details from input data
            val expenseId = inputData.getLong("expense_id", -1)
            val dueDate = inputData.getInt("due_date", -1)
            val daysUntilDue = inputData.getInt("days_until_due", 1)

            if (expenseId == -1L || dueDate == -1) {
                return Result.failure()
            }

            // Get the expense
            val expense = expenseRepository.getAllExpenses().first()
                .find { it.id == expenseId } ?: return Result.failure()

            // Get country config for currency formatting
            val countryCode = salaryRepository.getCountryCode().first()
            val countryConfig = CountryConfigProvider.getConfig(countryCode)

            // Send notification
            NotificationHelper.sendExpenseReminder(
                context = applicationContext,
                expense = expense,
                currencySymbol = countryConfig.currencySymbol,
                daysUntilDue = daysUntilDue
            )

            // Reschedule for next month
            val remindDaysBefore = userPreferencesManager.remindDaysBeforeFlow.first()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationScheduler.scheduleDueDateReminder(
                    context = applicationContext,
                    expenseId = expenseId,
                    dueDate = dueDate,
                    daysBeforeReminder = remindDaysBefore
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

