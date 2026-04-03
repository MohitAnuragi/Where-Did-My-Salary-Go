package com.wheredidmysalarygo.wheredidmysalarygo.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.wheredidmysalarygo.wheredidmysalarygo.R
import com.wheredidmysalarygo.wheredidmysalarygo.domain.model.Expense


object NotificationHelper {

    private const val CHANNEL_ID = "expense_reminders"
    private const val CHANNEL_NAME = "Expense Reminders"
    private const val CHANNEL_DESCRIPTION = "Gentle reminders for upcoming fixed expenses"

    private const val NOTIFICATION_ID_SALARY_SUMMARY = 1001
    private const val NOTIFICATION_ID_MONTH_END = 1002


    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                // No sound by default - calm notifications
                setSound(null, null)
                enableVibration(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun sendExpenseReminder(
        context: Context,
        expense: Expense,
        currencySymbol: String,
        daysUntilDue: Int
    ) {
        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted, skip notification
                return
            }
        }

        val dueDateText = when (daysUntilDue) {
            0 -> "is due today"
            1 -> "is due tomorrow"
            else -> "is due in $daysUntilDue days"
        }

        val formattedAmount = formatCurrencyForNotification(expense.amount, currencySymbol)

        // Notification text following the calm, neutral tone
        val title = "Upcoming expense"
        val body = "${expense.name} of $formattedAmount $dueDateText."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You may want to create a custom icon
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                expense.id.toInt(), // Use expense ID as notification ID
                notification
            )
        } catch (e: SecurityException) {
            // Handle permission denial gracefully
            e.printStackTrace()
        }
    }


    fun sendSalarySummaryNotification(
        context: Context,
        totalCommitted: Double,
        currencySymbol: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val formattedAmount = formatCurrencyForNotification(totalCommitted, currencySymbol)
        val title = "Your salary is in"
        val body = "$formattedAmount already committed."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_SALARY_SUMMARY,
                notification
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    fun sendMonthEndNotification(
        context: Context,
        freeToSpend: Double,
        currencySymbol: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val formattedAmount = formatCurrencyForNotification(freeToSpend, currencySymbol)
        val title = "Monthly snapshot"
        val body = "This month, you had $formattedAmount free to spend."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_MONTH_END,
                notification
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    private fun formatCurrencyForNotification(amount: Double, currencySymbol: String): String {
        return if (amount >= 1000) {
            "$currencySymbol${String.format("%,.0f", amount)}"
        } else {
            "$currencySymbol${String.format("%.0f", amount)}"
        }
    }


    fun cancelNotification(context: Context, expenseId: Long) {
        NotificationManagerCompat.from(context).cancel(expenseId.toInt())
    }


    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }


    fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}

