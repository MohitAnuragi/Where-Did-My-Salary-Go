package com.wheredidmysalarygo.wheredidmysalarygo.domain.model

data class Expense(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDate: Int, // Day of month (1-31)
    val frequency: ExpenseFrequency = ExpenseFrequency.MONTHLY,
    val month: String // yyyy-MM format
)

enum class ExpenseFrequency {
    MONTHLY
}

