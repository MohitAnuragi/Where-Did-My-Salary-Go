package com.wheredidmysalarygo.wheredidmysalarygo.utils

/**
 * Represents country-specific configuration
 */
data class CountryConfig(
    val countryCode: String,
    val countryName: String,
    val currencyCode: String,
    val currencySymbol: String,
    val defaultPayFrequency: PayFrequency = PayFrequency.MONTHLY,
    val expenseExamples: List<String>
)

enum class PayFrequency {
    MONTHLY,
    BI_WEEKLY,
    WEEKLY
}

