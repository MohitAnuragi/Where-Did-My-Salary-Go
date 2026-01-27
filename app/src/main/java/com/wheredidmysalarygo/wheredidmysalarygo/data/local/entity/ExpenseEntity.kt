package com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDate: Int, // Day of month (1-31)
    val frequency: String = "MONTHLY"
)

