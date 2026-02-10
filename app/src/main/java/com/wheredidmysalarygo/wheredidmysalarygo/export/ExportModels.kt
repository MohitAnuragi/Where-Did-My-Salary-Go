package com.wheredidmysalarygo.wheredidmysalarygo.export

/**
 * Export models - Simple DTOs for CSV export
 * NO Room entities, NO internal IDs
 */

/**
 * Represents a monthly summary for export
 */
data class MonthlyExportData(
    val month: String, // Format: "MMM yyyy" (e.g., "Feb 2026")
    val salary: Double,
    val totalFixedExpenses: Double,
    val remainingAmount: Double,
    val expenses: List<ExpenseExportData>
)

/**
 * Represents a single expense for export
 */
data class ExpenseExportData(
    val name: String,
    val amount: Double,
    val dueDate: Int? // Day of month (1-31), null if not set
)

/**
 * Subscription plans with export limits
 */
enum class SubscriptionPlan(val exportMonths: Int) {
    FREE(0),           // No export
    PRO_1_MONTH(1),    // Last 1 month
    PRO_6_MONTH(3),    // Last 3 months
    PRO_1_YEAR(6);     // Last 6 months

    companion object {
        fun fromProStatus(isPro: Boolean): SubscriptionPlan {
            // For now, default Pro users to PRO_1_MONTH
            // Can be extended when real subscription tiers are implemented
            return if (isPro) PRO_1_MONTH else FREE
        }
    }
}

