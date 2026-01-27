package com.wheredidmysalarygo.wheredidmysalarygo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.dao.ExpenseDao
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "where_did_my_salary_go_db"
    }
}

