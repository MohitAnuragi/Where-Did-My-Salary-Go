package com.wheredidmysalarygo.wheredidmysalarygo.utils

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {


    fun format(amount: Double, countryConfig: CountryConfig): String {
        return try {
            val locale = getLocaleForCountry(countryConfig.countryCode)
            val formatter = NumberFormat.getCurrencyInstance(locale)

            // For some currencies, we need to set the currency explicitly
            try {
                formatter.currency = Currency.getInstance(countryConfig.currencyCode)
            } catch (e: Exception) {
                // If currency code is not recognized, use fallback
            }

            formatter.format(amount)
        } catch (e: Exception) {
            // Fallback to simple formatting
            formatSimple(amount, countryConfig.currencySymbol)
        }
    }


    private fun getLocaleForCountry(countryCode: String): Locale {
        return when (countryCode) {
            "IN" -> Locale("en", "IN")      // India
            "US" -> Locale("en", "US")      // United States
            "GB" -> Locale("en", "GB")      // United Kingdom
            "CA" -> Locale("en", "CA")      // Canada
            "AU" -> Locale("en", "AU")      // Australia
            "BD" -> Locale("bn", "BD")      // Bangladesh
            "BR" -> Locale("pt", "BR")      // Brazil
            "FR" -> Locale("fr", "FR")      // France
            "DE" -> Locale("de", "DE")      // Germany
            "IT" -> Locale("it", "IT")      // Italy
            "JP" -> Locale("ja", "JP")      // Japan
            "MY" -> Locale("ms", "MY")      // Malaysia
            "NL" -> Locale("nl", "NL")      // Netherlands
            "NZ" -> Locale("en", "NZ")      // New Zealand
            "RU" -> Locale("ru", "RU")      // Russia
            "SG" -> Locale("en", "SG")      // Singapore
            "ZA" -> Locale("en", "ZA")      // South Africa
            "KR" -> Locale("ko", "KR")      // South Korea
            "LK" -> Locale("si", "LK")      // Sri Lanka
            "CH" -> Locale("de", "CH")      // Switzerland
            else -> Locale("en", "IN")      // Default to India
        }
    }


    fun formatSimple(amount: Double, symbol: String): String {
        return "$symbol${String.format("%,.2f", amount)}"
    }


    fun formatInteger(amount: Double, countryConfig: CountryConfig): String {
        // For currencies with large denominations (JPY, KRW), don't show decimals
        val formattedAmount = when (countryConfig.currencyCode) {
            "JPY", "KRW" -> String.format("%,d", amount.toInt())
            else -> String.format("%,.0f", amount)
        }
        return "${countryConfig.currencySymbol}$formattedAmount"
    }


    fun formatCompact(amount: Double, countryConfig: CountryConfig): String {
        return when {
            amount >= 10_000_000 -> "${countryConfig.currencySymbol}${String.format("%.1f", amount / 1_000_000)}M"
            amount >= 100_000 -> "${countryConfig.currencySymbol}${String.format("%.1f", amount / 100_000)}L"
            amount >= 1_000 -> "${countryConfig.currencySymbol}${String.format("%.1f", amount / 1_000)}K"
            else -> formatInteger(amount, countryConfig)
        }
    }
}
