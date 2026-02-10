package com.wheredidmysalarygo.wheredidmysalarygo.export

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * CsvExporter - Generates CSV files from export data
 *
 * Rules:
 * - UTF-8 encoding
 * - Headers included
 * - Comma-separated
 * - Empty values allowed
 */
object CsvExporter {

    /**
     * Generate CSV file from monthly export data
     *
     * @param data List of monthly summaries
     * @param outputFile File to write CSV to
     * @return Success status
     */
    fun generateCsv(data: List<MonthlyExportData>, outputFile: File): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                // Write CSV header
                writer.append("Month,Salary,Total Fixed Expenses,Remaining Amount,Expense Name,Expense Amount,Due Date\n")

                // Write data rows
                if (data.isEmpty()) {
                    // Empty export - headers only
                    return@use
                }

                data.forEach { monthData ->
                    if (monthData.expenses.isEmpty()) {
                        // Month with no expenses - write summary only
                        writer.append(
                            "${monthData.month}," +
                            "${formatAmount(monthData.salary)}," +
                            "${formatAmount(monthData.totalFixedExpenses)}," +
                            "${formatAmount(monthData.remainingAmount)}," +
                            ",,\n" // Empty expense columns
                        )
                    } else {
                        // Write first expense with month summary
                        val firstExpense = monthData.expenses.first()
                        writer.append(
                            "${monthData.month}," +
                            "${formatAmount(monthData.salary)}," +
                            "${formatAmount(monthData.totalFixedExpenses)}," +
                            "${formatAmount(monthData.remainingAmount)}," +
                            "${escapeCSV(firstExpense.name)}," +
                            "${formatAmount(firstExpense.amount)}," +
                            "${firstExpense.dueDate ?: ""}\n"
                        )

                        // Write remaining expenses (without repeating month summary)
                        monthData.expenses.drop(1).forEach { expense ->
                            writer.append(
                                ",,,,${escapeCSV(expense.name)}," +
                                "${formatAmount(expense.amount)}," +
                                "${expense.dueDate ?: ""}\n"
                            )
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Generate file name based on current date
     * Format: WhereDidMySalaryGo_Export_Feb_2026.csv
     */
    fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("MMM_yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        return "WhereDidMySalaryGo_Export_$dateString.csv"
    }

    /**
     * Format amount with 2 decimal places
     */
    private fun formatAmount(amount: Double): String {
        return String.format(Locale.US, "%.2f", amount)
    }

    /**
     * Escape CSV special characters
     */
    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}

