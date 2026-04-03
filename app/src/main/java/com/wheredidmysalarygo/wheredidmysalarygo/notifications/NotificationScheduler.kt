package com.wheredidmysalarygo.wheredidmysalarygo.notifications

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit


object NotificationScheduler {

    private const val SALARY_SUMMARY_WORK = "salary_summary_notification"
    private const val MONTH_END_WORK = "month_end_notification"
    private const val DUE_DATE_PREFIX = "due_date_notification_"


    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleSalarySummaryNotification(context: Context, creditDate: Int) {
        val targetDateTime = calculateNextSalaryCreditDateTime(creditDate)
        val initialDelay = Duration.between(LocalDateTime.now(), targetDateTime).toMinutes()

        if (initialDelay <= 0) {
            // Already passed this month, schedule for next month
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SalarySummaryWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(SALARY_SUMMARY_WORK)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SALARY_SUMMARY_WORK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleMonthEndNotification(context: Context) {
        val targetDateTime = calculateMonthEndDateTime()
        val initialDelay = Duration.between(LocalDateTime.now(), targetDateTime).toMinutes()

        if (initialDelay <= 0) {
            // Already passed, schedule for next month
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MonthEndWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(MONTH_END_WORK)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MONTH_END_WORK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleDueDateReminder(
        context: Context,
        expenseId: Long,
        dueDate: Int,
        daysBeforeReminder: Int
    ) {
        val targetDateTime = calculateReminderDateTime(dueDate, daysBeforeReminder)
        val initialDelay = Duration.between(LocalDateTime.now(), targetDateTime).toMinutes()

        if (initialDelay <= 0) {
            // Already passed this month, don't schedule
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val inputData = workDataOf(
            "expense_id" to expenseId,
            "due_date" to dueDate,
            "days_until_due" to daysBeforeReminder
        )

        val workRequest = OneTimeWorkRequestBuilder<DueDateReminderWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("$DUE_DATE_PREFIX$expenseId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "$DUE_DATE_PREFIX$expenseId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }


    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(SALARY_SUMMARY_WORK)
            cancelUniqueWork(MONTH_END_WORK)
            cancelAllWorkByTag(DUE_DATE_PREFIX)
        }
    }


    fun cancelDueDateReminder(context: Context, expenseId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("$DUE_DATE_PREFIX$expenseId")
    }

    // Helper functions to calculate notification times

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateNextSalaryCreditDateTime(creditDate: Int): LocalDateTime {
        val today = LocalDate.now()
        val notificationTime = LocalTime.of(9, 0) // 9 AM

        val targetDate = if (today.dayOfMonth >= creditDate) {
            // Salary day passed, schedule for next month
            today.plusMonths(1).withDayOfMonth(creditDate)
        } else {
            // Salary day upcoming this month
            today.withDayOfMonth(creditDate)
        }

        return LocalDateTime.of(targetDate, notificationTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateMonthEndDateTime(): LocalDateTime {
        val today = LocalDate.now()
        val notificationTime = LocalTime.of(20, 0) // 8 PM on last day

        val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        return if (today.dayOfMonth == today.lengthOfMonth()) {
            // Already last day, schedule for next month
            today.plusMonths(1).withDayOfMonth(today.plusMonths(1).lengthOfMonth())
                .atTime(notificationTime)
        } else {
            LocalDateTime.of(lastDayOfMonth, notificationTime)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateReminderDateTime(dueDate: Int, daysBeforeReminder: Int): LocalDateTime {
        val today = LocalDate.now()
        val notificationTime = LocalTime.of(9, 0) // 9 AM

        val dueDateThisMonth = today.withDayOfMonth(dueDate)
        val reminderDate = dueDateThisMonth.minusDays(daysBeforeReminder.toLong())

        return if (reminderDate.isBefore(today) || reminderDate.isEqual(today)) {
            // Already passed this month, schedule for next month
            today.plusMonths(1).withDayOfMonth(dueDate).minusDays(daysBeforeReminder.toLong())
                .atTime(notificationTime)
        } else {
            LocalDateTime.of(reminderDate, notificationTime)
        }
    }
}

