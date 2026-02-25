package com.wheredidmysalarygo.wheredidmysalarygo.export

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


object CsvExporter {

    fun generateCsv(data: List<MonthlyExportData>, outputFile: File): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                // Write CSV header
                writer.append("Month,Salary,Total Fixed Expenses,Remaining Amount,Expense Name,Expense Amount,Due Date\n")

                // Write data rows
                if (data.isEmpty()) {
                    return@use
                }

                data.forEach { monthData ->
                    if (monthData.expenses.isEmpty()) {

                        writer.append(
                            "${monthData.month}," +
                            "${formatAmount(monthData.salary)}," +
                            "${formatAmount(monthData.totalFixedExpenses)}," +
                            "${formatAmount(monthData.remainingAmount)}," +
                            ",,\n" // Empty expense columns
                        )
                    } else {
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


    fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("MMM_yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        return "WhereDidMySalaryGo_Export_$dateString.csv"
    }


    private fun formatAmount(amount: Double): String {
        return String.format(Locale.US, "%.2f", amount)
    }


    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}

