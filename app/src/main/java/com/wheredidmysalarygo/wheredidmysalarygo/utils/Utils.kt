package com.wheredidmysalarygo.wheredidmysalarygo.utils

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    fun formatCurrency(amount: Double): String {
        // Use device's default locale for currency formatting
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return currencyFormatter.format(amount)
    }
}

object DateUtils {
    fun isValidDayOfMonth(day: Int): Boolean {
        return day in 1..31
    }

    fun getCurrentDayOfMonth(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    fun daysUntilDue(dueDate: Int): Int {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        return if (dueDate >= today) {
            dueDate - today
        } else {
            // Calculate days in current month and add remaining days
            val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            (maxDays - today) + dueDate
        }
    }

    fun isDueSoon(dueDate: Int, thresholdDays: Int = 3): Boolean {
        val daysUntil = daysUntilDue(dueDate)
        return daysUntil in 0..thresholdDays
    }
}

