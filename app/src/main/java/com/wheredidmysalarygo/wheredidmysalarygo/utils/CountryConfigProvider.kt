package com.wheredidmysalarygo.wheredidmysalarygo.utils

/**
 * Single source of truth for country-specific configurations
 * Easy to extend by adding new countries
 */
object CountryConfigProvider {

    private val configs = mapOf(
        "IN" to CountryConfig(
            countryCode = "IN",
            countryName = "India",
            currencyCode = "INR",
            currencySymbol = "₹",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "EMI", "Netflix", "Electricity", "Groceries")
        ),
        "US" to CountryConfig(
            countryCode = "US",
            countryName = "United States",
            currencyCode = "USD",
            currencySymbol = "$",
            defaultPayFrequency = PayFrequency.BI_WEEKLY,
            expenseExamples = listOf("Rent", "Car Loan", "Netflix", "Utilities", "Groceries")
        ),
        "GB" to CountryConfig(
            countryCode = "GB",
            countryName = "United Kingdom",
            currencyCode = "GBP",
            currencySymbol = "£",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Council Tax", "Netflix", "Utilities", "Groceries")
        ),
        "CA" to CountryConfig(
            countryCode = "CA",
            countryName = "Canada",
            currencyCode = "CAD",
            currencySymbol = "$",
            defaultPayFrequency = PayFrequency.BI_WEEKLY,
            expenseExamples = listOf("Rent", "Car Loan", "Netflix", "Utilities", "Groceries")
        ),
        "AU" to CountryConfig(
            countryCode = "AU",
            countryName = "Australia",
            currencyCode = "AUD",
            currencySymbol = "$",
            defaultPayFrequency = PayFrequency.BI_WEEKLY,
            expenseExamples = listOf("Rent", "Car Loan", "Netflix", "Utilities", "Groceries")
        )
    )

    /**
     * Get country configuration by country code
     * Returns India as default if country not found
     */
    fun getConfig(countryCode: String): CountryConfig {
        return configs[countryCode] ?: configs["IN"]!!
    }

    /**
     * Get all supported countries
     */
    fun getAllCountries(): List<CountryConfig> {
        return configs.values.toList().sortedBy { it.countryName }
    }

    /**
     * Get default country based on device locale
     */
    fun getDefaultCountryCode(): String {
        val deviceCountry = java.util.Locale.getDefault().country
        return if (configs.containsKey(deviceCountry)) {
            deviceCountry
        } else {
            "IN" // Default to India
        }
    }
}

