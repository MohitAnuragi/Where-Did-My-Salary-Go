package com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MonthlySummaryEntity - Represents one calendar month's salary tracking
 *
 * Each month is independent - January and February data coexist separately
 * Format: yyyy-MM (e.g., "2026-02")
 */
@Entity(tableName = "monthly_summary")
data class MonthlySummaryEntity(
    @PrimaryKey
    val month: String, // yyyy-MM format
    val salaryAmount: Double,
    val totalFixedExpenses: Double,
    val remainingAmount: Double
)

