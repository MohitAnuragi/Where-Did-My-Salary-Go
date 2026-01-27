package com.wheredidmysalarygo.wheredidmysalarygo.domain.model

data class SalaryInfo(
    val monthlySalary: Double,
    val totalFixedExpenses: Double,
    val remainingAmount: Double,
    val commitmentPercentage: Float
) {
    companion object {
        fun calculate(salary: Double, expenses: List<Expense>): SalaryInfo {
            val totalExpenses = expenses.sumOf { it.amount }
            val remaining = salary - totalExpenses
            val percentage = if (salary > 0) ((totalExpenses / salary) * 100).toFloat() else 0f

            return SalaryInfo(
                monthlySalary = salary,
                totalFixedExpenses = totalExpenses,
                remainingAmount = remaining,
                commitmentPercentage = percentage
            )
        }
    }
}

