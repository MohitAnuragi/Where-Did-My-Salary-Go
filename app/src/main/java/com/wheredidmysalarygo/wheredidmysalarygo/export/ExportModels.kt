package com.wheredidmysalarygo.wheredidmysalarygo.export


data class MonthlyExportData(
    val month: String, // Format: "MMM yyyy" (e.g., "Feb 2026")
    val salary: Double,
    val totalFixedExpenses: Double,
    val remainingAmount: Double,
    val expenses: List<ExpenseExportData>
)


data class ExpenseExportData(
    val name: String,
    val amount: Double,
    val dueDate: Int?
)


enum class SubscriptionPlan(val exportMonths: Int) {
    FREE(0),
    PRO_1_MONTH(1),
    PRO_3_MONTH(1),
    PRO_6_MONTH(3),
    PRO_1_YEAR(6);

    companion object {
        fun fromProStatus(isPro: Boolean): SubscriptionPlan {

            return if (isPro) PRO_1_MONTH else FREE
        }
    }
}

