package com.wheredidmysalarygo.wheredidmysalarygo.utils

/**
 * PricingProvider - Provides country-specific pricing for Pro subscriptions
 *
 * No external APIs, hardcoded pricing
 * Clean and simple
 */
object PricingProvider {

    data class PlanPricing(
        val monthlyPrice: Double,
        val sixMonthPrice: Double,
        val yearlyPrice: Double,
        val fiveYearPrice: Double,
        val currencySymbol: String,
        val currencyCode: String
    )

    /**
     * Get pricing based on country code
     *
     * @param countryCode ISO country code (e.g., "IN", "US")
     * @return PlanPricing for the country
     */
    fun getPricing(countryCode: String): PlanPricing {
        return when (countryCode.uppercase()) {
            "IN" -> PlanPricing(
                monthlyPrice = 49.0,
                sixMonthPrice = 199.0,
                yearlyPrice = 349.0,
                fiveYearPrice = 999.0,
                currencySymbol = "₹",
                currencyCode = "INR"
            )
            else -> PlanPricing(
                monthlyPrice = 1.99,
                sixMonthPrice = 5.99,
                yearlyPrice = 9.99,
                fiveYearPrice = 24.99,
                currencySymbol = "$",
                currencyCode = "USD"
            )
        }
    }

    /**
     * Format price with currency symbol
     */
    fun formatPrice(price: Double, currencySymbol: String): String {
        return "$currencySymbol${String.format("%.2f", price)}"
    }
}

