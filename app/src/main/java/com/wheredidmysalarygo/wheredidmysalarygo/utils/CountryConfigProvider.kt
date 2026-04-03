package com.wheredidmysalarygo.wheredidmysalarygo.utils


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
            currencySymbol = "C$",
            defaultPayFrequency = PayFrequency.BI_WEEKLY,
            expenseExamples = listOf("Rent", "Car Loan", "Netflix", "Utilities", "Groceries")
        ),
        "AU" to CountryConfig(
            countryCode = "AU",
            countryName = "Australia",
            currencyCode = "AUD",
            currencySymbol = "A$",
            defaultPayFrequency = PayFrequency.BI_WEEKLY,
            expenseExamples = listOf("Rent", "Car Loan", "Netflix", "Utilities", "Groceries")
        ),
        "BD" to CountryConfig(
            countryCode = "BD",
            countryName = "Bangladesh",
            currencyCode = "BDT",
            currencySymbol = "৳",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Loan", "Internet", "Electricity", "Groceries")
        ),
        "BR" to CountryConfig(
            countryCode = "BR",
            countryName = "Brazil",
            currencyCode = "BRL",
            currencySymbol = "R$",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Aluguel", "Empréstimo", "Netflix", "Luz", "Compras")
        ),
        "FR" to CountryConfig(
            countryCode = "FR",
            countryName = "France",
            currencyCode = "EUR",
            currencySymbol = "€",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Loyer", "Prêt", "Netflix", "Électricité", "Courses")
        ),
        "DE" to CountryConfig(
            countryCode = "DE",
            countryName = "Germany",
            currencyCode = "EUR",
            currencySymbol = "€",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Miete", "Kredit", "Netflix", "Strom", "Lebensmittel")
        ),
        "IT" to CountryConfig(
            countryCode = "IT",
            countryName = "Italy",
            currencyCode = "EUR",
            currencySymbol = "€",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Affitto", "Prestito", "Netflix", "Elettricità", "Spesa")
        ),
        "JP" to CountryConfig(
            countryCode = "JP",
            countryName = "Japan",
            currencyCode = "JPY",
            currencySymbol = "¥",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Loan", "Netflix", "Utilities", "Groceries")
        ),
        "MY" to CountryConfig(
            countryCode = "MY",
            countryName = "Malaysia",
            currencyCode = "MYR",
            currencySymbol = "RM",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Loan", "Netflix", "Utilities", "Groceries")
        ),
        "NL" to CountryConfig(
            countryCode = "NL",
            countryName = "Netherlands",
            currencyCode = "EUR",
            currencySymbol = "€",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Huur", "Lening", "Netflix", "Energie", "Boodschappen")
        ),
        "NZ" to CountryConfig(
            countryCode = "NZ",
            countryName = "New Zealand",
            currencyCode = "NZD",
            currencySymbol = "NZ$",
            defaultPayFrequency = PayFrequency.BI_WEEKLY,
            expenseExamples = listOf("Rent", "Loan", "Netflix", "Utilities", "Groceries")
        ),
        "RU" to CountryConfig(
            countryCode = "RU",
            countryName = "Russia",
            currencyCode = "RUB",
            currencySymbol = "₽",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Loan", "Netflix", "Utilities", "Groceries")
        ),
        "SG" to CountryConfig(
            countryCode = "SG",
            countryName = "Singapore",
            currencyCode = "SGD",
            currencySymbol = "S$",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Loan", "Netflix", "Utilities", "Groceries")
        ),
        "ZA" to CountryConfig(
            countryCode = "ZA",
            countryName = "South Africa",
            currencyCode = "ZAR",
            currencySymbol = "R",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Loan", "Netflix", "Utilities", "Groceries")
        ),
        "KR" to CountryConfig(
            countryCode = "KR",
            countryName = "South Korea",
            currencyCode = "KRW",
            currencySymbol = "₩",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Loan", "Netflix", "Utilities", "Groceries")
        ),
        "LK" to CountryConfig(
            countryCode = "LK",
            countryName = "Sri Lanka",
            currencyCode = "LKR",
            currencySymbol = "Rs",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Rent", "Loan", "Internet", "Electricity", "Groceries")
        ),
        "CH" to CountryConfig(
            countryCode = "CH",
            countryName = "Switzerland",
            currencyCode = "CHF",
            currencySymbol = "CHF",
            defaultPayFrequency = PayFrequency.MONTHLY,
            expenseExamples = listOf("Miete", "Kredit", "Netflix", "Strom", "Lebensmittel")
        )
    )


    fun getConfig(countryCode: String): CountryConfig {
        return configs[countryCode] ?: configs["IN"]!!
    }


    fun getAllCountries(): List<CountryConfig> {
        return configs.values.toList().sortedBy { it.countryName }
    }


    fun getDefaultCountryCode(): String {
        val deviceCountry = java.util.Locale.getDefault().country
        return if (configs.containsKey(deviceCountry)) {
            deviceCountry
        } else {
            "IN"
        }
    }
}

