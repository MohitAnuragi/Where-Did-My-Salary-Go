package com.wheredidmysalarygo.wheredidmysalarygo.ui.pro

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.UserPreferencesManager
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesManager: UserPreferencesManager,
    private val salaryRepository: SalaryRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val isProUser: Flow<Boolean> = userPreferencesManager.isProUserFlow

    /**
     * Activate Pro subscription (temporary - no real payment)
     * Shows Toast and enables Pro features
     */
    fun activatePro() {
        viewModelScope.launch {
            try {
                // Set Pro flag
                userPreferencesManager.setProUser(true)

                // Enable notifications by default
                userPreferencesManager.setNotificationsEnabled(true)

                // Schedule all Pro notifications
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    scheduleAllNotifications()
                }

                // Show success toast
                Toast.makeText(
                    context,
                    "Pro subscription enabled",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to activate Pro: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Schedule all Pro notifications based on user data
     */
    private suspend fun scheduleAllNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        try {
            // Schedule salary summary notification
            val creditDate = salaryRepository.getSalaryCreditDate().first()
            if (creditDate != null) {
                NotificationScheduler.scheduleSalarySummaryNotification(context, creditDate)
            }

            // Schedule month-end notification
            NotificationScheduler.scheduleMonthEndNotification(context)

            // Schedule due date reminders for all expenses
            val expenses = expenseRepository.getAllExpenses().first()
            val remindDaysBefore = userPreferencesManager.remindDaysBeforeFlow.first()

            expenses.forEach { expense ->
                if (expense.dueDate != null) {
                    NotificationScheduler.scheduleDueDateReminder(
                        context = context,
                        expenseId = expense.id,
                        dueDate = expense.dueDate,
                        daysBeforeReminder = remindDaysBefore
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deactivate Pro (for testing purposes)
     */
    fun deactivatePro() {
        viewModelScope.launch {
            try {
                // Set Pro flag to false
                userPreferencesManager.setProUser(false)

                // Disable notifications
                userPreferencesManager.setNotificationsEnabled(false)

                // Cancel all scheduled notifications
                NotificationScheduler.cancelAllNotifications(context)

                Toast.makeText(
                    context,
                    "Pro subscription deactivated",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to deactivate Pro: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

