package com.wheredidmysalarygo.wheredidmysalarygo.utils

import java.text.NumberFormat
import java.util.*

/**
 * Utility for formatting currency based on country configuration
 * Keeps DB values as plain numbers, formats only at UI level
 */
object CurrencyFormatter {

    /**
     * Format amount with currency symbol based on country config
     */
    fun format(amount: Double, countryConfig: CountryConfig): String {
        return try {
            val locale = when (countryConfig.countryCode) {
                "IN" -> Locale("en", "IN")
                "US" -> Locale("en", "US")
                "GB" -> Locale("en", "GB")
                "CA" -> Locale("en", "CA")
                "AU" -> Locale("en", "AU")
                else -> Locale("en", "IN")
            }

            val formatter = NumberFormat.getCurrencyInstance(locale)
            formatter.format(amount)
        } catch (e: Exception) {
            // Fallback to simple formatting
            "${countryConfig.currencySymbol}${String.format("%.2f", amount)}"
        }
    }

    /**
     * Format amount with custom symbol (for simple display)
     */
    fun formatSimple(amount: Double, symbol: String): String {
        return "$symbol${String.format("%,.2f", amount)}"
    }

    /**
     * Format amount as integer (for whole numbers)
     */
    fun formatInteger(amount: Double, countryConfig: CountryConfig): String {
        return "${countryConfig.currencySymbol}${amount.toInt()}"
    }
}

