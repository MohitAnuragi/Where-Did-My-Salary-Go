package com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "monthly_summary")
data class MonthlySummaryEntity(
    @PrimaryKey
    val month: String, // yyyy-MM format
    val salaryAmount: Double,
    val totalFixedExpenses: Double,
    val remainingAmount: Double
)

